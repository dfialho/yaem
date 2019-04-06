package dfialho.yaem.app

import dfialho.yaem.app.managers.AccountManager
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.list

fun Route.accounts(manager: AccountManager) {

    post("accounts") {
        val account = Json.validatedParse(Account.serializer(), call.receiveText())

        manager.create(account)

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

    put("accounts/{id}") {
        val accountID = call.parameters["id"] ?: throw IllegalArgumentException("ID parameter is required")
        val account = Json.validatedParse(Account.serializer(), call.receiveText())

        manager.update(accountID, account)
        val updatedAccount = account.copy(id = accountID)

        call.respond(HttpStatusCode.Accepted, Json.stringify(Account.serializer(), updatedAccount))
    }

    delete("accounts/{id}") {
        val receivedID = call.parameters["id"] ?: throw IllegalArgumentException("ID parameter is required")

        manager.delete(receivedID)
        call.respond(HttpStatusCode.Accepted, "Account with id '$receivedID' was deleted")
    }
}
