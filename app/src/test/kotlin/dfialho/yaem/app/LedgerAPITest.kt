package dfialho.yaem.app

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.*
import dfialho.yaem.app.validators.ValidationError
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.withTestApplication
import kotlinx.serialization.json.Json
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class LedgerAPITest {

    @Test
    fun `creating a new transaction for non-existing account should respond with bad request`() {
        withTestApplication({ module(testing = true) }) {

            val nonExistingAccountID = randomID()
            val transaction = Transaction(
                amount = 10.5,
                description = "bananas",
                incomingAccount = nonExistingAccountID,
                timestamp = Instant.ofEpochMilli(1550395065330),
                id = randomID()
            )

            handleCreateTransactionRequest(transaction).apply {
                assertAll {
                    assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                    assertThat(response.content).errorListContainsAll(ValidationError.LedgerMissingAccount())
                }
            }
        }
    }

    @Test
    fun `creating a new transaction should respond with created`() {
        withTestApplication({ module(testing = true) }) {

            val account = createAccount(Account(id = "c1929c11-3caa-400c-bee4-fdad5f023759", name = "My New Account"))
            val transaction = Transaction(
                amount = 10.5,
                description = "bananas",
                incomingAccount = account.id,
                id = randomID()
            )

            handleCreateTransactionRequest(transaction).apply {
                assertAll {
                    assertThat(response.status()).isEqualTo(HttpStatusCode.Created)
                    assertThat(response.content).isJsonEqualTo(Transaction.serializer(), transaction)
                }
            }
        }
    }

    @Test
    fun `creating a transaction with an existing ID should respond with conflict`() {
        withTestApplication({ module(testing = true) }) {

            val account = createAccount(Account(id = "c1929c11-3caa-400c-bee4-fdad5f023759", name = "My New Account"))
            val existingTransaction = createTransaction(Transaction(10.5, "bananas", account.id, id = randomID()))
            val newTransaction = Transaction(11.5, "strawberries", account.id, id = existingTransaction.id)

            handleCreateTransactionRequest(newTransaction).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Conflict)
            }
        }
    }

    @Test
    fun `calling the get endpoint with non-existing ID should respond with not found`() {
        withTestApplication({ module(testing = true) }) {

            val nonExistingID = "c1929c11-3caa-400c-bee4-fdad5f023759"
            handleGetTransactionRequest(nonExistingID).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }
        }
    }

    @Test
    fun `calling list endpoint when no transactions were created should respond with an empty json list`() {
        withTestApplication({ module(testing = true) }) {

            handleListTransactionsRequest().apply {
                assertAll {
                    assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                    assertThat(response.content).isJsonEmptyList(Transaction.serializer())
                }
            }
        }
    }

    @Test
    fun `create transaction with optional fields not set should generate those fields`() {
        withTestApplication({ module(testing = true) }) {

            val account = createAccount(Account("My New Account"))

            handleCreateTransactionRequest(
                body = """
                {
                  "amount": 10.5,
                  "description": "bananas",
                  "incomingAccount": "${account.id}"
                }
            """.trimIndent()
            ).apply {
                val transaction = Json.parse(Transaction.serializer(), response.content ?: "")
                assertAll {
                    assertThat(response.status()).isEqualTo(HttpStatusCode.Created)
                    assertThat(transaction.amount).isEqualTo(10.5)
                    assertThat(transaction.description).isEqualTo("bananas")
                    assertThat(transaction.incomingAccount).isEqualTo(account.id)
                    assertThat(transaction.sendingAccount).isNull()
                    assertThat(transaction.id).isNotEmpty()
                    assertThat(transaction.timestamp).isNotNull()
                }
            }
        }
    }

    @Test
    fun `create transaction with invalid json`(): Unit = withTestApplication({ module(testing = true) }) {
        handleCreateTransactionRequest("{ invalid json }").apply {
            assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
        }
    }

    @Test
    fun `create transaction missing required name field`(): Unit = withTestApplication({ module(testing = true) }) {
        handleCreateTransactionRequest(
            body = """
                {
                  "amount": 10.5,
                  "description": "bananas"
                }
            """.trimIndent()
        ).apply {
            assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
        }
    }

    @Test
    fun `after deleting a transaction it is no longer available`() {
        withTestApplication({ module(testing = true) }) {

            val account = createAccount(Account("My account"))
            val transaction = createTransaction(randomTransaction(incomingAccount = account.id))
            createTransaction(randomTransaction(incomingAccount = account.id))
            createTransaction(randomTransaction(incomingAccount = account.id))

            handleDeleteTransactionRequest(transaction.id).apply {
                assertAll {
                    assertThat(response.status()).isEqualTo(HttpStatusCode.Accepted)
                }
            }

            assertThat(listTransactions()).doesNotContain(transaction)
        }
    }

    @Test
    fun `deleting a non-existing transaction responds with not found`() {
        withTestApplication({ module(testing = true) }) {

            val account = createAccount(Account("My account"))
            val transaction = randomTransaction(incomingAccount = account.id)
            val nonDeletedTransaction1 = createTransaction(randomTransaction(incomingAccount = account.id))
            val nonDeletedTransaction2 = createTransaction(randomTransaction(incomingAccount = account.id))

            handleDeleteTransactionRequest(transaction.id).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }

            assertThat(listTransactions()).containsOnly(nonDeletedTransaction1, nonDeletedTransaction2)
        }
    }

    @Test
    fun `after updating a transaction the response to getting includes the updated version`() {
        withTestApplication({ module(testing = true) }) {
            val account1 = createAccount(Account("Account 1"))
            val account2 = createAccount(Account("Account 2"))
            val transaction = createTransaction(
                Transaction(
                    amount = 10.5,
                    description = "bananas",
                    incomingAccount = account1.id
                )
            )

            val newVersion = Transaction(
                amount = 500.2,
                description = "some other description",
                incomingAccount = account2.id
            )

            val newVersionWithOriginalID = newVersion.copy(id = transaction.id)

            handleUpdateTransactionRequest(transaction.id, newVersion).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Accepted)
                assertThat(response.content).isJsonEqualTo(Transaction.serializer(), newVersionWithOriginalID)
            }

            assertThat(getTransaction(transaction.id)).isEqualTo(newVersionWithOriginalID)
        }
    }

    @Test
    fun `trying to update a transaction that does not exist responds with not found`() {
        withTestApplication({ module(testing = true) }) {
            val account1 = createAccount(Account("Account 1"))
            val account2 = createAccount(Account("Account 2"))
            val transaction = createTransaction(
                Transaction(
                    amount = 10.5,
                    description = "bananas",
                    incomingAccount = account1.id
                )
            )

            val newVersion = Transaction(
                amount = 500.2,
                description = "some other description",
                incomingAccount = account2.id,
                id = transaction.id
            )

            val nonExistingID = randomID()

            handleUpdateTransactionRequest(nonExistingID, newVersion).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }

            assertThat(getTransaction(transaction.id)).isEqualTo(transaction)
        }
    }

    @Test
    fun `trying to update a transaction to a non-existing incoming account should fail`() {
        withTestApplication({ module(testing = true) }) {
            val account1 = createAccount(Account("Account 1"))
            val transaction = createTransaction(
                Transaction(
                    incomingAccount = account1.id,
                    amount = 10.5,
                    description = "bananas"
                )
            )

            val newVersion = Transaction(
                incomingAccount = randomID(),
                amount = 500.2,
                description = "some other description"
            )

            handleUpdateTransactionRequest(transaction.id, newVersion).apply {
                assertAll {
                    assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                    assertThat(response.content).errorListContainsAll(ValidationError.LedgerMissingAccount())
                }
            }

            assertThat(getTransaction(transaction.id)).isEqualTo(transaction)
        }
    }
    @Test
    fun `trying to update a transaction to a non-existing sending account should fail`() {
        withTestApplication({ module(testing = true) }) {
            val account1 = createAccount(Account("Account 1"))
            val transaction = createTransaction(
                Transaction(
                    incomingAccount = account1.id,
                    amount = 10.5,
                    description = "bananas"
                )
            )

            val newVersion = Transaction(
                sendingAccount = randomID(),
                incomingAccount = transaction.incomingAccount,
                amount = 500.2,
                description = "some other description"
            )

            handleUpdateTransactionRequest(transaction.id, newVersion).apply {
                assertAll {
                    assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                    assertThat(response.content).errorListContainsAll(ValidationError.LedgerMissingAccount())
                }
            }

            assertThat(getTransaction(transaction.id)).isEqualTo(transaction)
        }
    }
}
