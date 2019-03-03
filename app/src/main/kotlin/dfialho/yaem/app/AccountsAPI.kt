package dfialho.yaem.app

import dfialho.yaem.app.managers.AccountManager
import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.throwError
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
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
            throwError { error }
        }

        logOnError(log) {
            manager.create(account)
        }

        call.respond(HttpStatusCode.Created, Json.stringify(Account.serializer(), account))
    }

    get("accounts") {
        call.respond(Json.stringify(Account.serializer().list, manager.list()))
    }

    get("accounts/{id}") {
        val receivedID = call.parameters["id"] ?: throw IllegalArgumentException("ID parameter is required")

        val account = manager.get(receivedID)
        call.respond(HttpStatusCode.OK, Json.stringify(Account.serializer(), account))
    }

    delete("accounts/{id}") {
        val receivedID = call.parameters["id"] ?: throw IllegalArgumentException("ID parameter is required")

        manager.delete(receivedID)
        call.respond(HttpStatusCode.Accepted, "Account with id '$receivedID' was deleted")
    }
}
