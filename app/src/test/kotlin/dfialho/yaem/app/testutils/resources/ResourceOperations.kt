package dfialho.yaem.app.testutils.resources

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import dfialho.yaem.app.api.ID
import dfialho.yaem.json.lib.json
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationResponse
import io.ktor.server.testing.contentType
import kotlinx.serialization.list

inline fun <reified T : Any> TestApplicationResponse.parseContent(): T {
    return json.parse(serializer<T>(), content ?: "")
}

inline fun <reified T : Any> TestApplicationResponse.parseContents(): List<T> {
    return json.parse(serializer<T>().list, content ?: "")
}

inline fun <reified T : Any> TestApplicationEngine.create(resource: T): T {
    return handleCreateRequest(resource).run {
        assertThat(response.status()).isEqualTo(HttpStatusCode.Created)
        assertThat(response.contentType()).isEqualTo(ContentType.Application.Json.withCharset(Charsets.UTF_8))
        assertThat(response.content)
            .isNotNull()
            .isNotEmpty()

        response.parseContent()
    }
}

inline fun <reified T : Any> TestApplicationEngine.get(id: ID): T {
    return handleGetRequest<T>(id).run {
        assertAll {
            assertThat(response.status())
                .isEqualTo(HttpStatusCode.OK)

            assertThat(response.contentType())
                .isEqualTo(ContentType.Application.Json.withCharset(Charsets.UTF_8))

            assertThat(response.content)
                .isNotNull()
                .isNotEmpty()
        }

        response.parseContent()
    }
}

inline fun <reified T : Any> TestApplicationEngine.list(): List<T> {
    return handleListRequest<T>().run {
        assertAll {
            assertThat(response.status())
                .isEqualTo(HttpStatusCode.OK)

            assertThat(response.contentType())
                .isEqualTo(ContentType.Application.Json.withCharset(Charsets.UTF_8))

            assertThat(response.content)
                .isNotNull()
                .isNotEmpty()
        }

        response.parseContents()
    }
}

inline fun <reified T : Any> TestApplicationEngine.delete(id: ID) {
    handleDeleteRequest<T>(id).run {
        assertThat(response.status()).isEqualTo(HttpStatusCode.Accepted)
    }
}
