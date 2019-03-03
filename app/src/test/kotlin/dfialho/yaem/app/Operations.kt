package dfialho.yaem.app

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.fail
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import java.time.Instant

//
// Accounts
//

fun TestApplicationEngine.createAccount(account: Account): Account = handleCreateAccountRequest(account).run {
    Json.parse(Account.serializer(), response.content ?: "")
}

fun TestApplicationEngine.listAccounts(): List<Account> = handleListAccountsRequest().run {
    Json.parse(Account.serializer().list, response.content ?: "")
}

//
// Transactions
//

fun TestApplicationEngine.createTransaction(transaction: Transaction): Transaction {
    return handleCreateTransactionRequest(transaction).run {
        Json.parse(Transaction.serializer(), response.content ?: "")
    }
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
