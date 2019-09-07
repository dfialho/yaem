package dfialho.yaem.app.testutils

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import dfialho.yaem.app.api.*
import dfialho.yaem.json.lib.json
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.contentType
import kotlinx.serialization.list

//
// Accounts
//

fun TestApplicationEngine.createAccount(account: Account): Account =
    handleCreateAccountRequest(account).run {
        assertThat(response.status()).isEqualTo(HttpStatusCode.Created)
        assertThat(response.contentType()).isEqualTo(ContentType.Application.Json.withCharset(Charsets.UTF_8))
        assertThat(response.content)
            .isNotNull()
            .isNotEmpty()

        parseAccount()
    }

fun TestApplicationEngine.getAccount(accountID: ID): Account =
    handleGetAccountRequest(accountID).run {
        assertAll {
            assertThat(response.status())
                .isEqualTo(HttpStatusCode.OK)

            assertThat(response.contentType())
                .isEqualTo(ContentType.Application.Json.withCharset(Charsets.UTF_8))

            assertThat(response.content)
                .isNotNull()
                .isNotEmpty()
        }

        parseAccount()
    }

private fun TestApplicationCall.parseAccount() = json.parse(Account.serializer(), response.content ?: "")

fun TestApplicationEngine.listAccounts(): List<Account> =
    handleListAccountsRequest().run {
        assertAll {
            assertThat(response.status())
                .isEqualTo(HttpStatusCode.OK)

            assertThat(response.contentType())
                .isEqualTo(ContentType.Application.Json.withCharset(Charsets.UTF_8))

            assertThat(response.content)
                .isNotNull()
                .isNotEmpty()
        }

        json.parse(Account.serializer().list, response.content ?: "")
    }

fun TestApplicationEngine.deleteAccount(accountID: ID): Unit =
    handleDeleteAccountRequest(accountID).run {
        assertThat(response.status()).isEqualTo(HttpStatusCode.Accepted)
    }

//
// Transactions
//

fun TestApplicationEngine.createTransaction(transaction: Transaction): Transaction =
    handleCreateTransactionRequest(transaction).run {
        json.parse(Transaction.serializer(), response.content ?: "")
    }

fun TestApplicationEngine.getTransaction(trxID: ID): Transaction =
    handleGetTransactionRequest(trxID).run {
        json.parse(Transaction.serializer(), response.content ?: "")
    }

fun TestApplicationEngine.listTransactions(): List<Transaction> =
    handleListTransactionsRequest().run {
        json.parse(Transaction.serializer().list, response.content ?: "")
    }
