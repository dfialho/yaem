package dfialho.yaem.app

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.*
import dfialho.yaem.app.api.*
import dfialho.yaem.app.repositories.DatabaseConfig
import dfialho.yaem.app.validators.ValidationError
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.server.testing.contentType
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import java.time.Instant
import java.util.*

class LedgerAPITest {

    val dbConfig = DatabaseConfig(
        url = "jdbc:h2:mem:${UUID.randomUUID()};MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        driver = "org.h2.Driver"
    )

    @Test
    fun `creating a new transaction for non-existing account should respond with bad request`() {
        withTestApplication({ app(dbConfig) }) {

            val nonExistingAccountID = randomID()
            val transaction = OneWayTransaction(
                account = nonExistingAccountID,
                amount = 10.5,
                description = "bananas",
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
    fun `creating a new one-way transaction should respond with created`() {
        withTestApplication({ app(dbConfig) }) {

            val account = createAccount(Account(id = "c1929c11-3caa-400c-bee4-fdad5f023759", name = "My New Account"))
            val transaction = OneWayTransaction(
                account = account.id,
                amount = 10.5,
                description = "bananas",
                id = randomID()
            )

            handleCreateTransactionRequest(transaction).apply {
                assertAll {
                    assertThat(response.status()).isEqualTo(HttpStatusCode.Created)
                    assertThat(response.contentType()).isEqualTo(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                    assertThat(response.content).isJsonEqualTo(Transaction.serializer(), transaction)
                }
            }
        }
    }

    @Test
    fun `creating a new transfer should respond with created`() {
        withTestApplication({ app(dbConfig) }) {

            val account1 = createAccount(Account(name = "Account 1"))
            val account2 = createAccount(Account(name = "Account 2"))

            val transaction = Transfer(
                outgoingAccount = account1.id,
                incomingAccount = account2.id,
                amount = 10.5,
                description = "bananas",
                id = randomID()
            )

            handleCreateTransactionRequest(transaction).apply {
                assertAll {
                    assertThat(response.status()).isEqualTo(HttpStatusCode.Created)
                    assertThat(response.contentType()).isEqualTo(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                    assertThat(response.content).isJsonEqualTo(Transaction.serializer(), transaction)
                }
            }
        }
    }

    @Test
    fun `after creating multiple accounts calling the list endpoint should return all of them`() {
        withTestApplication({ app(dbConfig) }) {

            val account1 = createAccount(Account(name = "Account 1"))
            val account2 = createAccount(Account(name = "Account 2"))

            val transactions = arrayOf(
                createTransaction(randomOneWayTransaction(account1.id)),
                createTransaction(randomTransfer(account1.id, account2.id)),
                createTransaction(randomOneWayTransaction(account2.id)),
                createTransaction(randomTransfer(account2.id, account1.id))
            )

            handleListTransactionsRequest().apply {
                assertThat(response.contentType()).isEqualTo(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                assertThat(response.content).jsonListContainsOnly(Transaction.serializer(), *transactions)
            }
        }
    }

    @Test
    fun `creating a transaction with an existing ID should respond with conflict`() {
        withTestApplication({ app(dbConfig) }) {

            val account = createAccount(Account(id = "c1929c11-3caa-400c-bee4-fdad5f023759", name = "My New Account"))
            val existingTransaction = createTransaction(OneWayTransaction(account.id, 10.5, id = randomID()))
            val newTransaction = OneWayTransaction(account.id, 11.5, id = existingTransaction.id)

            handleCreateTransactionRequest(newTransaction).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Conflict)
            }
        }
    }

    @Test
    fun `calling the get endpoint with non-existing ID should respond with not found`() {
        withTestApplication({ app(dbConfig) }) {

            val nonExistingID = "c1929c11-3caa-400c-bee4-fdad5f023759"
            handleGetTransactionRequest(nonExistingID).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }
        }
    }

    @Test
    fun `calling list endpoint when no transactions were created should respond with an empty json list`() {
        withTestApplication({ app(dbConfig) }) {

            handleListTransactionsRequest().apply {
                assertAll {
                    assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                    assertThat(response.contentType()).isEqualTo(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                    assertThat(response.content).isJsonEmptyList(Transaction.serializer())
                }
            }
        }
    }

    @Test
    fun `create transaction with optional fields not set should generate those fields`() {
        withTestApplication({ app(dbConfig) }) {

            val account = createAccount(Account("My New Account"))

            handleCreateTransactionRequest(
                body = """
                {
                  "type": "OneWay",
                  "value": {
                    "amount": 10.5,
                    "description": "bananas",
                    "account": "${account.id}"
                  }
                }
            """.trimIndent()
            ).apply {
                println("bananas: " + response.content)
                val transaction = json.parse(Transaction.serializer(), response.content ?: "")

                assertAll {
                    assertThat(response.status()).isEqualTo(HttpStatusCode.Created)
                    assertThat(response.contentType()).isEqualTo(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                    assertThat(transaction).isInstanceOf(OneWayTransaction::class)
                    assertThat(transaction.amount).isEqualTo(10.5)
                    assertThat(transaction.description).isEqualTo("bananas")
                    assertThat(transaction.id).isNotEmpty()
                    assertThat(transaction.timestamp).isNotNull()
                }

                transaction as OneWayTransaction
                assertThat(transaction.account).isEqualTo(account.id)
            }
        }
    }

    @Test
    fun `create transaction with invalid json`(): Unit = withTestApplication({ app(dbConfig) }) {
        handleCreateTransactionRequest("{ invalid json }").apply {
            assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
        }
    }

    @Test
    fun `create transaction missing required name field`(): Unit = withTestApplication({ app(dbConfig) }) {
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
        withTestApplication({ app(dbConfig) }) {

            val account = createAccount(Account("My account"))
            val transaction = createTransaction(randomOneWayTransaction(account.id))
            createTransaction(randomOneWayTransaction(account.id))
            createTransaction(randomOneWayTransaction(account.id))

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
        withTestApplication({ app(dbConfig) }) {

            val account = createAccount(Account("My account"))
            val transaction = randomOneWayTransaction(account.id)
            val nonDeletedTransaction1 = createTransaction(randomOneWayTransaction(account.id))
            val nonDeletedTransaction2 = createTransaction(randomOneWayTransaction(account.id))

            handleDeleteTransactionRequest(transaction.id).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }

            assertThat(listTransactions()).containsOnly(nonDeletedTransaction1, nonDeletedTransaction2)
        }
    }

    @Test
    fun `after updating a transaction the response to getting includes the updated version`() {
        withTestApplication({ app(dbConfig) }) {
            val account1 = createAccount(Account("Account 1"))
            val account2 = createAccount(Account("Account 2"))
            val transaction = createTransaction(
                OneWayTransaction(
                    amount = 10.5,
                    description = "bananas",
                    account = account1.id
                )
            )

            val newVersion = OneWayTransaction(
                amount = 500.2,
                description = "some other description",
                account = account2.id
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
        withTestApplication({ app(dbConfig) }) {
            val account1 = createAccount(Account("Account 1"))
            val account2 = createAccount(Account("Account 2"))
            val transaction = createTransaction(
                OneWayTransaction(
                    amount = 10.5,
                    description = "bananas",
                    account = account1.id
                )
            )

            val newVersion = OneWayTransaction(
                amount = 500.2,
                description = "some other description",
                account = account2.id,
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
    fun `updating from one-way transaction to a transfer`() {
        withTestApplication({ app(dbConfig) }) {
            val account1 = createAccount(Account("Account 1"))
            val account2 = createAccount(Account("Account 2"))
            val transaction = createTransaction(
                OneWayTransaction(
                    amount = 10.5,
                    description = "bananas",
                    account = account1.id
                )
            )

            val newVersion = Transfer(
                amount = 500.2,
                description = "some other description",
                incomingAccount = account1.id,
                outgoingAccount = account2.id
            )

            val newVersionWithOriginalID = newVersion.copy(id = transaction.id)

            handleUpdateTransactionRequest(transaction.id, newVersion).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Accepted)
                assertThat(response.contentType()).isEqualTo(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                assertThat(response.content).isJsonEqualTo(Transaction.serializer(), newVersionWithOriginalID)
            }

            assertThat(getTransaction(transaction.id)).isEqualTo(newVersionWithOriginalID)
        }
    }

    @Test
    fun `updating from transfer to one-way`() {
        withTestApplication({ app(dbConfig) }) {
            val account1 = createAccount(Account("Account 1"))
            val account2 = createAccount(Account("Account 2"))
            val transaction = createTransaction(
                Transfer(
                    amount = 10.5,
                    description = "bananas",
                    incomingAccount = account1.id,
                    outgoingAccount = account2.id
                )
            )

            val newVersion = OneWayTransaction(
                amount = 500.2,
                description = "some other description",
                account = account2.id
            )

            val newVersionWithOriginalID = newVersion.copy(id = transaction.id)

            handleUpdateTransactionRequest(transaction.id, newVersion).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Accepted)
                assertThat(response.contentType()).isEqualTo(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                assertThat(response.content).isJsonEqualTo(Transaction.serializer(), newVersionWithOriginalID)
            }

            assertThat(getTransaction(transaction.id)).isEqualTo(newVersionWithOriginalID)
        }
    }

    @Test
    fun `trying to update a transaction to a non-existing incoming account should fail`() {
        withTestApplication({ app(dbConfig) }) {
            val account1 = createAccount(Account("Account 1"))
            val transaction = createTransaction(
                OneWayTransaction(
                    account = account1.id,
                    amount = 10.5,
                    description = "bananas"
                )
            )

            val newVersion = OneWayTransaction(
                account = randomID(),
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
        withTestApplication({ app(dbConfig) }) {
            val account1 = createAccount(Account("Account 1"))
            val transaction = createTransaction(
                OneWayTransaction(
                    account = account1.id,
                    amount = 10.5,
                    description = "bananas"
                )
            )

            val newVersion = Transfer(
                outgoingAccount = randomID(),
                incomingAccount = transaction.account,
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
