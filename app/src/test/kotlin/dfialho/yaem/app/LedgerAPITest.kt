package dfialho.yaem.app

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.fail
import dfialho.yaem.app.validators.ValidationError
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import org.junit.Test
import java.time.Instant

class LedgerAPITest {

    @Test
    fun `creating a new transaction for non-existing account should respond with bad request`() {
        withTestApplication({ module(testing = true) }) {
            val accountID = randomID()

            handleRequest(HttpMethod.Post, "/api/ledger") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBodyAsTransaction(
                    Transaction(
                        amount = 10.5,
                        description = "bananas",
                        incomingAccount = accountID,
                        timestamp = Instant.ofEpochMilli(1550395065330),
                        id = randomID()
                    )
                )
            }.apply {
                assertAll {
                    assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                    assertThat(response.content).errorListContainsAll(ValidationError.LedgerMissingAccount(accountID))
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

            handleRequest(HttpMethod.Post, "/api/ledger") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBodyAsTransaction(transaction)
            }.apply {
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
            val transaction = createTransaction(Transaction(10.5, "bananas", account.id, id = randomID()))

            handleRequest(HttpMethod.Post, "/api/ledger") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBodyAsTransaction(Transaction(11.5, "strawberries", account.id, id = transaction.id))
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Conflict)
            }
        }
    }

    @Test
    fun `calling the get endpoint with non-existing ID should respond with not found`() {
        withTestApplication({ module(testing = true) }) {

            val nonExistingID = "c1929c11-3caa-400c-bee4-fdad5f023759"
            handleRequest(HttpMethod.Get, "/api/ledger/$nonExistingID").apply {
                assertAll {
                    assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
                    assertThat(response.content).isEqualTo("Transaction with ID '$nonExistingID' was not found")
                }
            }
        }
    }

    @Test
    fun `calling list endpoint when no transactions were created should respond with an empty json list`() {
        withTestApplication({ module(testing = true) }) {

            handleRequest(HttpMethod.Get, "/api/ledger").apply {
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

            handleRequest(HttpMethod.Post, "/api/ledger") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    """
                {
                  "amount": 10.5,
                  "description": "bananas",
                  "incomingAccount": "${account.id}"
                }
            """.trimIndent()
                )
            }.apply {
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

    // TODO optional fields
}

fun TestApplicationRequest.setBodyAsTransaction(transaction: Transaction) {
    setBody(Json.stringify(Transaction.serializer(), transaction))
}

fun TestApplicationEngine.createTransaction(transaction: Transaction): Transaction {
    handleRequest(HttpMethod.Post, "/api/ledger") {
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBodyAsTransaction(transaction)
    }

    return transaction
}

fun TestApplicationEngine.createTransaction(
    incomingAccount: ID,
    id: ID = randomID(),
    amount: Double = 10.5,
    description: String = "",
    sendingAccount: ID? = null,
    timestamp: Instant = Instant.now()
): Transaction {
    return createTransaction(Transaction(amount, description, incomingAccount, sendingAccount, timestamp, id))
}

fun TestApplicationEngine.listTransactions(): List<Transaction> {

    return handleRequest(HttpMethod.Get, "/api/ledger").run {
        assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
        val responseBody = response.content

        if (responseBody != null) {
            Json.parse(Transaction.serializer().list, responseBody)
        } else {
            fail("Response is null, but was expected to contain a json list")
        }
    }
}