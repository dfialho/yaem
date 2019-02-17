package dfialho.yaem.app

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import dfialho.yaem.app.validators.ValidationError
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlinx.serialization.json.Json
import java.time.Instant
import kotlin.test.Test

class AccountsAPITest {

    @Test
    fun listAccountOnEmptyList(): Unit = withTestApplication({ module(testing = true) }) {
        handleRequest(HttpMethod.Get, "/api/accounts").apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isJsonEmptyList(Account.serializer())
            }
        }
    }

    @Test
    fun getAccountOnEmptyList(): Unit = withTestApplication({ module(testing = true) }) {

        val accountID = "c1929c11-3caa-400c-bee4-fdad5f023759"
        handleRequest(HttpMethod.Get, "/api/accounts/$accountID").apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
                assertThat(response.content).isEqualTo("Account with ID '$accountID' was not found")
            }
        }
    }

    @Test
    fun getAccountUsingInvalidID(): Unit = withTestApplication({ module(testing = true) }) {

        val accountID = "c1929c11"
        handleRequest(HttpMethod.Get, "/api/accounts/$accountID").apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                assertThat(response.content).errorListContainsAll(ValidationError("BASE-01", "Invalid ID string: $accountID"))
            }
        }
    }

    @Test
    fun createAccount(): Unit = withTestApplication({ module(testing = true) }) {

        val account = Account(
            name = "My New Account",
            initialBalance = 10.0,
            startTimestamp = Instant.ofEpochMilli(1550250740735),
            id = "c1929c11-3caa-400c-bee4-fdad5f023759"
        )

        handleRequest(HttpMethod.Post, "/api/accounts") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBodyAsAccount(account)
        }.apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Accepted)
                assertThat(response.content).isJsonEqualTo(Account.serializer(), account)
            }
        }

        handleRequest(HttpMethod.Get, "/api/accounts").apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).jsonListContainsExactly(Account.serializer(), account)
            }
        }

        handleRequest(HttpMethod.Get, "/api/accounts/${account.id}").apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isJsonEqualTo(Account.serializer(), account)
            }
        }
    }

    @Test
    fun createAccountWithInvalidID(): Unit = withTestApplication({ module(testing = true) }) {

        val accountID = "c1929c11"
        val account = Account(
            name = "My New Account",
            initialBalance = 10.0,
            startTimestamp = Instant.ofEpochMilli(1550250740735),
            id = accountID
        )

        handleRequest(HttpMethod.Post, "/api/accounts") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBodyAsAccount(account)
        }.apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                assertThat(response.content).errorListContainsAll(ValidationError("BASE-01", "Invalid ID string: $accountID"))
            }
        }
    }

    @Test
    fun createAccountWithStringTimestamp(): Unit = withTestApplication({ module(testing = true) }) {

        handleRequest(HttpMethod.Post, "/api/accounts") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""
                {
                  "name": "My Account",
                  "initialBalance": 10.0,
                  "startTimestamp": "timestamp",
                  "id": "a1929c11-3caa-400c-bee4-fdad5f023759"
                }
            """.trimIndent())
        }.apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
                assertThat(response.content).errorListContainsAll(ValidationError("BASE-02", "Failed to parse 'Account' from json"))
            }
        }
    }

    @Test
    fun createAccountWithIntegerInitialBalance(): Unit = withTestApplication({ module(testing = true) }) {

        handleRequest(HttpMethod.Post, "/api/accounts") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""
                {
                  "name": "My Account",
                  "initialBalance": 10,
                  "startTimestamp": 1550250740735,
                  "id": "a1929c11-3caa-400c-bee4-fdad5f023759"
                }
            """.trimIndent())
        }.apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Accepted)
                assertThat(response.content).isJsonEqualTo(Account.serializer(), Account("My Account", 10.0, Instant.ofEpochMilli(1550250740735), "a1929c11-3caa-400c-bee4-fdad5f023759"))
            }
        }
    }

    @Test
    fun createAccountWithDuplicateID(): Unit = withTestApplication({ module(testing = true) }) {

        val commonID = "c1929c11-3caa-400c-bee4-fdad5f023759"
        val account1 = Account(
            name = "Account 1",
            initialBalance = 10.0,
            startTimestamp = Instant.ofEpochMilli(1550250740735),
            id = commonID
        )
        val account2 = Account(
            name = "Account 2",
            initialBalance = 10.0,
            startTimestamp = Instant.ofEpochMilli(1550250767151),
            id = commonID
        )

        handleRequest(HttpMethod.Post, "/api/accounts") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBodyAsAccount(account1)
        }.apply {
            assertThat(response.status()).isEqualTo(HttpStatusCode.Accepted)
        }

        handleRequest(HttpMethod.Post, "/api/accounts") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBodyAsAccount(account2)
        }.apply {
            assertThat(response.status()).isEqualTo(HttpStatusCode.Conflict)
        }

        handleRequest(HttpMethod.Get, "/api/accounts").apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).jsonListContainsExactly(Account.serializer(), account1)
            }
        }

        handleRequest(HttpMethod.Get, "/api/accounts/$commonID").apply {
            assertAll {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isJsonEqualTo(Account.serializer(), account1)
            }
        }
    }
}

fun TestApplicationRequest.setBodyAsAccount(account: Account) {
    setBody(Json.stringify(Account.serializer(), account))
}
