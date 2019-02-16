package dfialho.yaem.app

import dfialho.yaem.app.managers.AccountManager
import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.ValidationErrorException
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.util.error
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import org.slf4j.Logger

fun Route.accounts(manager: AccountManager, log: Logger) {

    post("accounts") {
        val account = try {
            Json.parse(Account.serializer(), call.receiveText())
        } catch (e: Exception) {
            val error = ValidationError.InvalidJson("Account")
            log.info(error.message, e)
            throw ValidationErrorException(error)
        }

        val createdAccount = logOnError(log) {
            manager.create(account)
        }

        call.respond(HttpStatusCode.Accepted, Json.stringify(Account.serializer(), createdAccount))
    }

    get("accounts") {
        call.respond(Json.stringify(Account.serializer().list, manager.list()))
    }

    get("accounts/{id}") {
        val receivedID = call.parameters["id"] ?: throw IllegalArgumentException("Account ID is required")

        val account = manager.get(receivedID)

        if (account != null) {
            call.respond(HttpStatusCode.OK, Json.stringify(Account.serializer(), account))
        } else {
            call.respond(HttpStatusCode.NotFound, "Account with ID '$receivedID' was not found")
        }
    }
}

private inline fun <R> logOnError(log: Logger, action: () -> R): R {
    try {
        return action()
    } catch (e: Exception) {
        log.error(e)
        throw e
    }
}
