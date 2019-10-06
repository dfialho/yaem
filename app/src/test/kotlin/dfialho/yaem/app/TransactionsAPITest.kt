package dfialho.yaem.app

import assertk.assertThat
import assertk.assertions.isEqualTo
import dfialho.yaem.app.api.Transaction
import dfialho.yaem.app.api.randomID
import dfialho.yaem.app.repositories.DatabaseConfig
import dfialho.yaem.app.testutils.isErrorListWith
import dfialho.yaem.app.testutils.resources.*
import dfialho.yaem.app.validators.ValidationError
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import java.util.*

class TransactionsAPITest {

    val dbConfig = DatabaseConfig(
        url = "jdbc:h2:mem:${UUID.randomUUID()};MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        driver = "org.h2.Driver"
    )

    @Test
    fun `create transaction`() {
        withTestApplication({ app(dbConfig) }) {
            val account = create(anyAccount())
            create(anyTransaction(account.id))
        }
    }

    @Test
    fun `delete a transaction`() {
        withTestApplication({ app(dbConfig) }) {
            val account = create(anyAccount())
            val transaction = create(anyTransaction(account.id))

            delete<Transaction>(transaction.id)
        }
    }

    @Test
    fun `trying to delete non-existing transaction responds with not found`() {
        withTestApplication({ app(dbConfig) }) {
            val transactionID = randomID()

            handleDeleteRequest<Transaction>(transactionID).run {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
                assertThat(response.content).isErrorListWith(ValidationError.NotFound("transaction", transactionID))
            }
        }
    }

    @Test
    fun `trying to create a transaction for non-existing account responds with not found`() {
        withTestApplication({ app(dbConfig) }) {
            val transaction = anyTransaction(randomID())

            handleCreateRequest(transaction).run {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
                assertThat(response.content).isErrorListWith(ValidationError.TransactionMissingAccount())
            }
        }
    }
}
