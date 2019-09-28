package dfialho.yaem.app

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.*
import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.randomID
import dfialho.yaem.app.repositories.DatabaseConfig
import dfialho.yaem.app.testutils.*
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
            assertThat(listAccounts()).isEmpty()
        }

    @Test
    fun `obtaining an account before any was created should respond with Not Found`(): Unit =
        withTestApplication({ app(dbConfig) }) {

            val accountID = "c1929c11-3caa-400c-bee4-fdad5f023759"
            handleGetAccountRequest(accountID).apply {
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

            val createdAccount = createAccount(account)

            assertAll {
                assertThat(createdAccount.ignoreId())
                    .isEqualTo(account.ignoreId())

                assertThat(getAccount(createdAccount.id))
                    .isEqualTo(createdAccount)
            }
        }


    @Test
    fun `getting account with an invalid ID should responds with Bad Request`(): Unit =
        withTestApplication({ app(dbConfig) }) {

            val accountID = "c1929c11"
            handleGetAccountRequest(accountID).apply {
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

            val createdAccount = createAccount(account)

            assertAll {
                assertThat(createdAccount.id)
                    .isNotEqualTo(account.id)
                assertThat(createdAccount.ignoreId())
                    .isEqualTo(account.ignoreId())
                assertThat(getAccount(createdAccount.id))
                    .isEqualTo(createdAccount)
            }
        }

    @Test
    fun `trying to create an account with invalid json should respond with Bad Request`(): Unit =
        withTestApplication({ app(dbConfig) }) {
            handleCreateAccountRequest("{ invalid json }").apply {
                assertAll {
                    assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                    assertThat(response.content).isErrorListWith(ValidationError.InvalidJson("Account"))
                }
            }
        }

    @Test
    fun `trying to create an account missing a required field should respond with Bad Request`(): Unit =
        withTestApplication({ app(dbConfig) }) {
            handleCreateAccountRequest(
                // Body is missing the 'name'
                body = """
                {
                  "initialBalance": 10.0,
                  "startTimestamp": "timestamp",
                  "id": "a1929c11-3caa-400c-bee4-fdad5f023759"
                }
            """.trimIndent()
            ).apply {
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
                .map { createAccount(it) }
                .toTypedArray()

            assertThat(listAccounts()).containsOnly(*createdAccounts)
        }

    @Test
    fun `after deleting an existing account that account is no longer listed`() {
        withTestApplication({ app(dbConfig) }) {

            val deletedAccount = createAccount(Account("Expense"))
            val others = (1..3)
                .map { Account("Acc-$it") }
                .map { createAccount(it) }
                .toTypedArray()

            deleteAccount(deletedAccount.id)

            assertThat(listAccounts()).containsOnly(*others)
        }
    }

    @Test
    fun `deleting a non-existing account does not affect other accounts`() {
        withTestApplication({ app(dbConfig) }) {

            val nonExistingID = randomID()
            val existingAccounts = (1..3)
                .map { Account("Acc-$it") }
                .map { createAccount(it) }
                .toTypedArray()

            handleDeleteAccountRequest(nonExistingID).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }

            assertThat(listAccounts()).containsOnly(*existingAccounts)
        }
    }

    @Test
    fun `trying to create an account with an existing name should respond with Conflict`() {
        withTestApplication({ app(dbConfig) }) {
            val existingAccount = createAccount(anyAccount())
            val account = Account(name = existingAccount.name)

            handleCreateAccountRequest(account).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Conflict)
                assertThat(response.content).isErrorListWith(ValidationError.AccountNameExists(account.name))
            }
        }
    }

    @Test
    fun `update an account`() {
        withTestApplication({ app(dbConfig) }) {
            val account = createAccount(
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

            handleUpdateAccountRequest(newVersion).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Accepted)
                assertThat(response.content).isJsonEqualTo(Account.serializer(), newVersion)
            }

            assertThat(getAccount(account.id)).isEqualTo(newVersion)
        }
    }

    @Test
    fun `trying to update an account that does not exist responds with Not Found`() {
        withTestApplication({ app(dbConfig) }) {
            val account = createAccount(
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

            handleUpdateAccountRequest(newVersion).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }

            assertThat(listAccounts()).containsOnly(account)
        }
    }

    @Test
    fun `trying to delete an account referenced by a transaction should respond with conflict`() {
        withTestApplication({ app(dbConfig) }) {
            val account = createAccount(anyAccount())
            createTransaction(anyTransaction(account.id))

            handleDeleteAccountRequest(account.id).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Conflict)
                assertThat(response.content).isErrorListWith(ValidationError.AccountReferences(account.id))
            }
        }
    }
}
