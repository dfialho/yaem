package dfialho.yaem.app

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import kotlinx.serialization.json.Json


fun TestApplicationEngine.handleCreateAccountRequest(account: Account): TestApplicationCall {

    return handleRequest(HttpMethod.Post, "/api/accounts") {
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(Json.stringify(Account.serializer(), account))
    }
}

fun TestApplicationEngine.handleCreateAccountRequest(body: String): TestApplicationCall {

    return handleRequest(HttpMethod.Post, "/api/accounts") {
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(body)
    }
}

fun TestApplicationEngine.handleListAccountsRequest() = handleRequest(HttpMethod.Get, "/api/accounts")

fun TestApplicationEngine.handleGetAccountRequest(accountID: ID) = handleRequest(HttpMethod.Get, "/api/accounts/$accountID")

fun TestApplicationEngine.handleDeleteAccountRequest(accountID: ID) = handleRequest(HttpMethod.Delete, "/api/accounts/$accountID")
