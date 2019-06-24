package dfialho.yaem.app

import io.ktor.server.testing.TestApplicationEngine
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import java.lang.IllegalStateException
import java.time.Instant

//
// Accounts
//

fun TestApplicationEngine.createAccount(account: Account): Account = handleCreateAccountRequest(account).run {
    json.parse(Account.serializer(), response.content ?: "")
}

fun TestApplicationEngine.getAccount(accountID: ID): Account = handleGetAccountRequest(accountID).run {
    json.parse(Account.serializer(), response.content ?: "")
}

fun TestApplicationEngine.listAccounts(): List<Account> = handleListAccountsRequest().run {
    json.parse(Account.serializer().list, response.content ?: "")
}

//
// Transactions
//

inline fun <reified T : Transaction>TestApplicationEngine.createTransaction(transaction: T): T {

    handleCreateTransactionRequest(transaction).run {
        val trx = json.parse(Transaction.serializer(), response.content ?: "")

        if (trx !is T) {
            throw IllegalStateException("Parsed transaction of type ${trx::class.simpleName}, but expected ${T::class.simpleName}")
        }

        return trx
    }
}

fun TestApplicationEngine.createOneWayTransaction(
    account: ID,
    id: ID = randomID(),
    amount: Double = 10.5,
    description: String = "",
    timestamp: Instant = Instant.now()
): Transaction {
    return createTransaction(OneWayTransaction(account, amount, description, timestamp, id))
}

fun TestApplicationEngine.getTransaction(trxID: ID): Transaction = handleGetTransactionRequest(trxID).run {
    json.parse(Transaction.serializer(), response.content ?: "")
}

fun TestApplicationEngine.listTransactions(): List<Transaction> = handleListTransactionsRequest().run {
    json.parse(Transaction.serializer().list, response.content ?: "")
}
