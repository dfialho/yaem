package dfialho.yaem.app

import dfialho.yaem.app.api.*
import dfialho.yaem.json.lib.json
import io.ktor.server.testing.TestApplicationEngine
import kotlinx.serialization.list

//
// Accounts
//

fun TestApplicationEngine.createAccount(account: Account): Account =
    handleCreateAccountRequest(account).run {
        json.parse(Account.serializer(), response.content ?: "")
    }

fun TestApplicationEngine.getAccount(accountID: ID): Account =
    handleGetAccountRequest(accountID).run {
        json.parse(Account.serializer(), response.content ?: "")
    }

fun TestApplicationEngine.listAccounts(): List<Account> =
    handleListAccountsRequest().run {
        json.parse(Account.serializer().list, response.content ?: "")
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
