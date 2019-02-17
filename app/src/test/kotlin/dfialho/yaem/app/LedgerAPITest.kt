package dfialho.yaem.app

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import dfialho.yaem.app.validators.ValidationError
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlinx.serialization.json.Json
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

            val account = Account(
                name = "My New Account",
                initialBalance = 10.0,
                startTimestamp = Instant.ofEpochMilli(1550250740735),
                id = "c1929c11-3caa-400c-bee4-fdad5f023759"
            )

            handleRequest(HttpMethod.Post, "/api/accounts") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBodyAsAccount(account)
            }

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

            val account = Account(
                name = "My New Account",
                initialBalance = 10.0,
                startTimestamp = Instant.ofEpochMilli(1550250740735),
                id = "c1929c11-3caa-400c-bee4-fdad5f023759"
            )

            handleRequest(HttpMethod.Post, "/api/accounts") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBodyAsAccount(account)
            }

            val transactionID = randomID()
            val transaction1 = Transaction(
                id = transactionID,
                amount = 10.5,
                description = "bananas",
                incomingAccount = account.id
            )
            val transaction2 = Transaction(
                id = transactionID,
                amount = 11.5,
                description = "strawberries",
                incomingAccount = account.id
            )

            handleRequest(HttpMethod.Post, "/api/ledger") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBodyAsTransaction(transaction1)
            }

            handleRequest(HttpMethod.Post, "/api/ledger") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBodyAsTransaction(transaction2)
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

            val account = Account(
                name = "My New Account",
                initialBalance = 10.0,
                startTimestamp = Instant.ofEpochMilli(1550250740735),
                id = "c1929c11-3caa-400c-bee4-fdad5f023759"
            )
            handleRequest(HttpMethod.Post, "/api/accounts") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBodyAsAccount(account)
            }

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
