package dfialho.yaem.app

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import kotlinx.serialization.json.Json

//
// Accounts
//

fun TestApplicationEngine.handleCreateAccountRequest(account: Account): TestApplicationCall {
    return handleCreateAccountRequest(body = Json.stringify(Account.serializer(), account))
}

fun TestApplicationEngine.handleCreateAccountRequest(body: String): TestApplicationCall {

    return handleRequest(HttpMethod.Post, "/api/accounts") {
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(body)
    }
}

fun TestApplicationEngine.handleUpdateAccountRequest(accountID: ID, account: Account): TestApplicationCall {
    return handleUpdateAccountRequest(accountID, body = Json.stringify(Account.serializer(), account))
}

fun TestApplicationEngine.handleUpdateAccountRequest(accountID: String, body: String): TestApplicationCall {

    return handleRequest(HttpMethod.Put, "/api/accounts/$accountID") {
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(body)
    }
}

fun TestApplicationEngine.handleListAccountsRequest() = handleRequest(HttpMethod.Get, "/api/accounts")

fun TestApplicationEngine.handleGetAccountRequest(accountID: ID) = handleRequest(HttpMethod.Get, "/api/accounts/$accountID")

fun TestApplicationEngine.handleDeleteAccountRequest(accountID: ID) = handleRequest(HttpMethod.Delete, "/api/accounts/$accountID")

//
// Transactions
//

fun TestApplicationEngine.handleCreateTransactionRequest(transaction: Transaction): TestApplicationCall {
    return handleCreateTransactionRequest(body = Json.stringify(Transaction.serializer(), transaction))
}

fun TestApplicationEngine.handleCreateTransactionRequest(body: String): TestApplicationCall {

    return handleRequest(HttpMethod.Post, "/api/ledger") {
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(body)
    }
}

fun TestApplicationEngine.handleListTransactionsRequest() = handleRequest(HttpMethod.Get, "/api/ledger")

fun TestApplicationEngine.handleGetTransactionRequest(nonExistingID: String) = handleRequest(HttpMethod.Get, "/api/ledger/$nonExistingID")

fun TestApplicationEngine.handleDeleteTransactionRequest(transactionID: ID) = handleRequest(HttpMethod.Delete, "/api/ledger/$transactionID")
