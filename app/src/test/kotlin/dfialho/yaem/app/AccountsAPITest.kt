package dfialho.yaem.app

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import dfialho.yaem.app.validators.ValidationError
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import java.time.Instant

class AccountsAPITest {

    @Test
    fun listAccountOnEmptyList(): Unit = withTestApplication({ module(testing = true) }) {
        handleListAccountsRequest().apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isJsonEmptyList(Account.serializer())
            }
        }
    }

    @Test
    fun getAccountOnEmptyList(): Unit = withTestApplication({ module(testing = true) }) {

        val accountID = "c1929c11-3caa-400c-bee4-fdad5f023759"
        handleGetAccountRequest(accountID).apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
                assertThat(response.content).isEqualTo("Account with ID '$accountID' was not found")
            }
        }
    }

    @Test
    fun getAccountUsingInvalidID(): Unit = withTestApplication({ module(testing = true) }) {

        val accountID = "c1929c11"
        handleGetAccountRequest(accountID).apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                assertThat(response.content).errorListContainsAll(
                    ValidationError("BASE-01", "Invalid ID string: $accountID")
                )
            }
        }
    }

    @Test
    fun createAccount(): Unit = withTestApplication({ module(testing = true) }) {

        val account = Account(
            name = "My New Account",
            initialBalance = 10.0,
            startTimestamp = Instant.ofEpochMilli(1550250740735),
            id = "c1929c11-3caa-400c-bee4-fdad5f023759"
        )

        handleCreateAccountRequest(account).apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Created)
                assertThat(response.content).isJsonEqualTo(Account.serializer(), account)
            }
        }

        handleListAccountsRequest().apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).jsonListContainsExactly(Account.serializer(), account)
            }
        }

        handleGetAccountRequest(account.id).apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isJsonEqualTo(Account.serializer(), account)
            }
        }
    }

    @Test
    fun createAccountWithInvalidID(): Unit = withTestApplication({ module(testing = true) }) {

        val accountID = "c1929c11"
        val account = Account(
            name = "My New Account",
            initialBalance = 10.0,
            startTimestamp = Instant.ofEpochMilli(1550250740735),
            id = accountID
        )

        handleCreateAccountRequest(account).apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                assertThat(response.content).errorListContainsAll(
                    ValidationError("BASE-01", "Invalid ID string: $accountID")
                )
            }
        }
    }

    @Test
    fun createAccountWithStringTimestamp(): Unit = withTestApplication({ module(testing = true) }) {

        handleCreateAccountRequest(
            body = """
                {
                  "name": "My Account",
                  "initialBalance": 10.0,
                  "startTimestamp": "timestamp",
                  "id": "a1929c11-3caa-400c-bee4-fdad5f023759"
                }
            """.trimIndent()
        ).apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                assertThat(response.content).errorListContainsAll(
                    ValidationError("BASE-02", "Failed to parse 'Account' from json")
                )
            }
        }
    }

    @Test
    fun createAccountWithIntegerInitialBalance(): Unit = withTestApplication({ module(testing = true) }) {

        handleCreateAccountRequest(
            body = """
                {
                  "name": "My Account",
                  "initialBalance": 10,
                  "startTimestamp": 1550250740735,
                  "id": "a1929c11-3caa-400c-bee4-fdad5f023759"
                }
            """.trimIndent()
        ).apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Created)
                assertThat(response.content).isJsonEqualTo(
                    Account.serializer(),
                    Account(
                        name = "My Account",
                        initialBalance = 10.0,
                        startTimestamp = Instant.ofEpochMilli(1550250740735),
                        id = "a1929c11-3caa-400c-bee4-fdad5f023759"
                    )
                )
            }
        }
    }

    @Test
    fun createAccountWithDuplicateID(): Unit = withTestApplication({ module(testing = true) }) {

        val commonID = "c1929c11-3caa-400c-bee4-fdad5f023759"
        val account1 = Account(
            name = "Account 1",
            initialBalance = 10.0,
            startTimestamp = Instant.ofEpochMilli(1550250740735),
            id = commonID
        )
        val account2 = Account(
            name = "Account 2",
            initialBalance = 10.0,
            startTimestamp = Instant.ofEpochMilli(1550250767151),
            id = commonID
        )

        handleCreateAccountRequest(account1).apply {
            assertThat(response.status()).isEqualTo(HttpStatusCode.Created)
        }

        handleCreateAccountRequest(account2).apply {
            assertThat(response.status()).isEqualTo(HttpStatusCode.Conflict)
        }

        handleListAccountsRequest().apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).jsonListContainsExactly(Account.serializer(), account1)
            }
        }

        handleGetAccountRequest(commonID).apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isJsonEqualTo(Account.serializer(), account1)
            }
        }
    }

    @Test
    fun `delete an existing account that account is not longer listed`() {
        withTestApplication({ module(testing = true) }) {

            val account = Account(
                name = "My New Account",
                initialBalance = 10.0,
                startTimestamp = Instant.ofEpochMilli(1550250740735),
                id = "c1929c11-3caa-400c-bee4-fdad5f023759"
            )
            val others = listOf(Account("Acc-1"), Account("Acc-2"))

            createAccount(account)
            others.forEach { createAccount(it) }

            handleRequest(HttpMethod.Delete, "/api/accounts/${account.id}").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Accepted)
            }

            handleRequest(HttpMethod.Get, "/api/accounts").apply {
                assertAll {
                    assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                    assertThat(response.content).jsonListContainsAll(Account.serializer(), *others.toTypedArray())
                }
            }
        }
    }

    @Test
    fun `delete a non-existing account no other accounts are affected`() {
        withTestApplication({ module(testing = true) }) {

            val account = Account(
                name = "My New Account",
                initialBalance = 10.0,
                startTimestamp = Instant.ofEpochMilli(1550250740735),
                id = "c1929c11-3caa-400c-bee4-fdad5f023759"
            )
            val others = listOf(Account("Acc-1"), Account("Acc-2"))

            others.forEach { createAccount(it) }

            handleDeleteAccountRequest(account.id).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }

            handleRequest(HttpMethod.Get, "/api/accounts").apply {
                assertAll {
                    assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                    assertThat(response.content).jsonListContainsAll(Account.serializer(), *others.toTypedArray())
                }
            }
        }
    }

    @Test
    fun `it is not possible to delete an account with transactions associated with it`() {
        withTestApplication({ module(testing = true) }) {
            val account = createAccount(Account("My Account"))
            val otherAccount = createAccount(Account("Other Account"))
            val transaction = createTransaction(incomingAccount = account.id)
            val otherTransaction = createTransaction(incomingAccount = otherAccount.id)

            handleDeleteAccountRequest(account.id).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Conflict)
            }

            assertAll {
                assertThat(listTransactions()).containsAll(transaction, otherTransaction)
                assertThat(listAccounts()).containsAll(account, otherAccount)
            }
        }
    }

    @Test
    fun `deleting an account when other accounts have transactions should succeed`() {
        withTestApplication({ module(testing = true) }) {
            val account = createAccount(Account("My Account"))
            val otherAccount = createAccount(Account("Other Account"))
            val otherTransaction = createTransaction(incomingAccount = otherAccount.id)

            handleDeleteAccountRequest(account.id).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Accepted)
            }

            assertAll {
                assertThat(listTransactions()).containsAll(otherTransaction)
                assertThat(listAccounts()).containsOnly(otherAccount)
            }
        }
    }
}
