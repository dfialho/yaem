package dfialho.yaem.app

import assertk.assertThat
import assertk.assertions.isEqualTo
import dfialho.yaem.app.api.Transaction
import dfialho.yaem.app.api.randomID
import dfialho.yaem.app.testutils.isErrorListWith
import dfialho.yaem.app.testutils.resources.*
import dfialho.yaem.app.testutils.withTestResourceAPI
import dfialho.yaem.app.validators.ValidationError
import io.ktor.http.HttpStatusCode
import org.junit.Test

class TransactionsAPITest {

    @Test
    fun `create transaction`() {
        withTestResourceAPI {
            val account = create(anyAccount())
            create(anyTransaction(account.id))
        }
    }

    @Test
    fun `delete a transaction`() {
        withTestResourceAPI {
            val account = create(anyAccount())
            val transaction = create(anyTransaction(account.id))

            delete<Transaction>(transaction.id)
        }
    }

    @Test
    fun `trying to delete non-existing transaction responds with not found`() {
        withTestResourceAPI {
            val transactionID = randomID()

            handleDeleteRequest<Transaction>(transactionID).run {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
                assertThat(response.content).isErrorListWith(ValidationError.NotFound("transaction", transactionID))
            }
        }
    }

    @Test
    fun `trying to create a transaction for non-existing account responds with not found`() {
        withTestResourceAPI {
            val transaction = anyTransaction(randomID())

            handleCreateRequest(transaction).run {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
                assertThat(response.content).isErrorListWith(ValidationError.TransactionMissingAccount())
            }
        }
    }
}
