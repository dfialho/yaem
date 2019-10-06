package dfialho.yaem.app.testutils.resources

import dfialho.yaem.app.api.ID
import dfialho.yaem.json.lib.json
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import kotlinx.serialization.SerializationStrategy

//
// Create
//

inline fun <reified T : Any> TestApplicationEngine.handleCreateRequest(resource: T): TestApplicationCall =
    handleCreateRequest(resource, resource.serializer())

inline fun <reified T : Any> TestApplicationEngine.handleCreateRequest(
    resource: T,
    serializer: SerializationStrategy<T>
): TestApplicationCall = handleCreateRequest<T> {
    json.stringify(serializer, resource)
}

inline fun <reified T : Any> TestApplicationEngine.handleCreateRequest(crossinline body: () -> String): TestApplicationCall {
    return handleRequest(HttpMethod.Post, api<T>()) {
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(body())
    }
}

//
// Update
//

inline fun <reified T : Any> TestApplicationEngine.handleUpdateRequest(resource: T): TestApplicationCall =
    handleUpdateRequest(resource, resource.serializer())

inline fun <reified T : Any> TestApplicationEngine.handleUpdateRequest(
    resource: T,
    serializer: SerializationStrategy<T>
): TestApplicationCall = handleUpdateRequest<T> {
    json.stringify(serializer, resource)
}

inline fun <reified T : Any> TestApplicationEngine.handleUpdateRequest(crossinline body: () -> String): TestApplicationCall {
    return handleRequest(HttpMethod.Put, api<T>()) {
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(body())
    }
}

//
// Get, List, Delete
//

inline fun <reified T : Any> TestApplicationEngine.handleGetRequest(id: ID): TestApplicationCall {
    return handleRequest(HttpMethod.Get, "${api<T>()}/$id")
}

inline fun <reified T : Any> TestApplicationEngine.handleListRequest(): TestApplicationCall {
    return handleRequest(HttpMethod.Get, api<T>())
}

inline fun <reified T : Any> TestApplicationEngine.handleDeleteRequest(id: ID): TestApplicationCall {
    return handleRequest(HttpMethod.Delete, "${api<T>()}/$id")
}
