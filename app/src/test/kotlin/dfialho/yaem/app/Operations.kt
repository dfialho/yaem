package dfialho.yaem.app

import io.ktor.server.testing.TestApplicationEngine
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import java.time.Instant

//
// Accounts
//

fun TestApplicationEngine.createAccount(account: Account): Account = handleCreateAccountRequest(account).run {
    Json.parse(Account.serializer(), response.content ?: "")
}

fun TestApplicationEngine.getAccount(accountID: ID): Account = handleGetAccountRequest(accountID).run {
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


fun TestApplicationEngine.listTransactions(): List<Transaction> = handleListTransactionsRequest().run {
    Json.parse(Transaction.serializer().list, response.content ?: "")
}
