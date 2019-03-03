package dfialho.yaem.app

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import dfialho.yaem.app.validators.ValidationError
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.withTestApplication
import kotlinx.serialization.json.Json
import org.junit.Test
import java.time.Instant

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
        handleCreateAccountRequest("{ invalid json }").apply {
            assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
        }
    }

    @Test
    fun `create transaction missing required name field`(): Unit = withTestApplication({ module(testing = true) }) {
        handleCreateAccountRequest(
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
}
