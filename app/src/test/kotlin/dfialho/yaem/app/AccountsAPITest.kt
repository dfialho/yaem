package dfialho.yaem.app

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.randomID
import dfialho.yaem.app.repositories.DatabaseConfig
import dfialho.yaem.app.testutils.ignoreId
import dfialho.yaem.app.testutils.isErrorListWith
import dfialho.yaem.app.testutils.isJsonEqualTo
import dfialho.yaem.app.testutils.resources.*
import dfialho.yaem.app.validators.ValidationError
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class AccountsAPITest {

    val dbConfig = DatabaseConfig(
        url = "jdbc:h2:mem:${UUID.randomUUID()};MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        driver = "org.h2.Driver"
    )

    @Test
    fun `listing accounts before any was created should respond with an empty list`(): Unit =
        withTestApplication({ app(dbConfig) }) {
            assertThat(list<Account>()).isEmpty()
        }

    @Test
    fun `obtaining an account before any was created should respond with Not Found`(): Unit =
        withTestApplication({ app(dbConfig) }) {

            val accountID = "c1929c11-3caa-400c-bee4-fdad5f023759"
            handleGetRequest<Account>(accountID).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
                assertThat(response.content).isErrorListWith(ValidationError.NotFound("account", accountID))
            }
        }

    @Test
    fun `create first account`(): Unit =
        withTestApplication({ app(dbConfig) }) {

            val account = Account(
                name = "My New Account",
                initialBalance = 10.0,
                startTimestamp = Instant.ofEpochMilli(1550250740735),
                id = "c1929c11-3caa-400c-bee4-fdad5f023759"
            )

            val createdAccount = create(account)

            assertAll {
                assertThat(createdAccount.ignoreId())
                    .isEqualTo(account.ignoreId())

                assertThat(get<Account>(createdAccount.id))
                    .isEqualTo(createdAccount)
            }
        }

    @Test
    fun `getting account with an invalid ID should responds with Bad Request`(): Unit =
        withTestApplication({ app(dbConfig) }) {

            val accountID = "c1929c11"
            handleGetRequest<Account>(accountID).apply {
                assertAll {
                    assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                    assertThat(response.content).isErrorListWith(ValidationError.InvalidID(accountID))
                }
            }
        }

    @Test
    fun `trying to create an account with an invalid ID should succeed because the ID is ignored`(): Unit =
        withTestApplication({ app(dbConfig) }) {

            val accountID = "c1929c11"
            val account = Account(
                name = "My New Account",
                initialBalance = 10.0,
                startTimestamp = Instant.ofEpochMilli(1550250740735),
                id = accountID
            )

            val createdAccount = create(account)

            assertAll {
                assertThat(createdAccount.id)
                    .isNotEqualTo(account.id)
                assertThat(createdAccount.ignoreId())
                    .isEqualTo(account.ignoreId())
                assertThat(get<Account>(createdAccount.id))
                    .isEqualTo(createdAccount)
            }
        }

    @Test
    fun `trying to create an account with invalid json should respond with Bad Request`(): Unit =
        withTestApplication({ app(dbConfig) }) {
            handleCreateRequest<Account> { "{ invalid json }" }.apply {
                assertAll {
                    assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                    assertThat(response.content).isErrorListWith(ValidationError.InvalidJson("Account"))
                }
            }
        }

    @Test
    fun `trying to create an account missing a required field should respond with Bad Request`(): Unit =
        withTestApplication({ app(dbConfig) }) {
            handleCreateRequest<Account> {
                // Body is missing the 'name'
                """
                {
                  "initialBalance": 10.0,
                  "startTimestamp": "timestamp",
                  "id": "a1929c11-3caa-400c-bee4-fdad5f023759"
                }
            """.trimIndent()
            }.apply {
                assertAll {
                    assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                    assertThat(response.content).isErrorListWith(ValidationError.InvalidJson("Account"))
                }
            }
        }

    @Test
    fun `after creating multiple accounts they are all listed`(): Unit =
        withTestApplication({ app(dbConfig) }) {
            val createdAccounts = (1..5)
                .map { Account("Acc-$it") }
                .map { create(it) }
                .toTypedArray()

            assertThat(list<Account>()).containsOnly(*createdAccounts)
        }

    @Test
    fun `after deleting an existing account that account is no longer listed`() {
        withTestApplication({ app(dbConfig) }) {

            val deletedAccount = create(Account("Expense"))
            val others = (1..3)
                .map { Account("Acc-$it") }
                .map { create(it) }
                .toTypedArray()

            delete<Account>(deletedAccount.id)

            assertThat(list<Account>()).containsOnly(*others)
        }
    }

    @Test
    fun `deleting a non-existing account does not affect other accounts`() {
        withTestApplication({ app(dbConfig) }) {

            val nonExistingID = randomID()
            val existingAccounts = (1..3)
                .map { Account("Acc-$it") }
                .map { create(it) }
                .toTypedArray()

            handleDeleteRequest<Account>(nonExistingID).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }

            assertThat(list<Account>()).containsOnly(*existingAccounts)
        }
    }

    @Test
    fun `trying to create an account with an existing name should respond with Conflict`() {
        withTestApplication({ app(dbConfig) }) {
            val existingAccount = create(anyAccount())
            val account = Account(name = existingAccount.name)

            handleCreateRequest(account).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Conflict)
                assertThat(response.content).isErrorListWith(ValidationError.AccountNameExists(account.name))
            }
        }
    }

    @Test
    fun `update an account`() {
        withTestApplication({ app(dbConfig) }) {
            val account = create(
                Account(
                    name = "My Account",
                    initialBalance = 10.5,
                    startTimestamp = Instant.parse("2011-12-03T10:15:30Z")
                )
            )

            val newVersion = Account(
                id = account.id,
                name = "New Account Name",
                initialBalance = account.initialBalance + 11.8,
                startTimestamp = account.startTimestamp.plus(1, ChronoUnit.DAYS)
            )

            handleUpdateRequest(newVersion).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Accepted)
                assertThat(response.content).isJsonEqualTo(Account.serializer(), newVersion)
            }

            assertThat(get<Account>(account.id)).isEqualTo(newVersion)
        }
    }

    @Test
    fun `trying to update an account that does not exist responds with Not Found`() {
        withTestApplication({ app(dbConfig) }) {
            val account = create(
                Account(
                    name = "My Account",
                    initialBalance = 10.5,
                    startTimestamp = Instant.parse("2011-12-03T10:15:30Z")
                )
            )

            val newVersion = Account(
                id = randomID(),
                name = "New Account Name",
                initialBalance = account.initialBalance + 11.8,
                startTimestamp = account.startTimestamp.plus(1, ChronoUnit.DAYS)
            )

            handleUpdateRequest(newVersion).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }

            assertThat(list<Account>()).containsOnly(account)
        }
    }

    @Test
    fun `trying to delete an account referenced by a transaction should respond with conflict`() {
        withTestApplication({ app(dbConfig) }) {
            val account = create(anyAccount())
            create(anyTransaction(account.id))

            handleDeleteRequest<Account>(account.id).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Conflict)
                assertThat(response.content).isErrorListWith(ValidationError.AccountReferences(account.id))
            }
        }
    }
}
