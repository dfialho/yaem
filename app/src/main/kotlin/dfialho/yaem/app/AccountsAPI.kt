package dfialho.yaem.app

import dfialho.yaem.app.api.Account
import dfialho.yaem.app.managers.AccountManager
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.*
import kotlinx.serialization.list

fun Route.accounts(manager: AccountManager) {

    post("accounts") {
        val account = json.validatedParse(Account.serializer(), call.receiveText())

        manager.create(account)

        call.respond(HttpStatusCode.Created, json.stringify(Account.serializer(), account))
    }

    get("accounts") {
        call.respond(json.stringify(Account.serializer().list, manager.list()))
    }

    get("accounts/{id}") {
        val receivedID = call.parameters["id"] ?: throw IllegalArgumentException("ID parameter is required")

        val account = manager.get(receivedID)
        call.respond(HttpStatusCode.OK, json.stringify(Account.serializer(), account))
    }

    put("accounts/{id}") {
        val accountID = call.parameters["id"] ?: throw IllegalArgumentException("ID parameter is required")
        val account = json.validatedParse(Account.serializer(), call.receiveText())

        manager.update(accountID, account)
        val updatedAccount = account.copy(id = accountID)

        call.respond(HttpStatusCode.Accepted, json.stringify(Account.serializer(), updatedAccount))
    }

    delete("accounts/{id}") {
        val receivedID = call.parameters["id"] ?: throw IllegalArgumentException("ID parameter is required")

        manager.delete(receivedID)
        call.respond(HttpStatusCode.Accepted, "Account with id '$receivedID' was deleted")
    }
}
