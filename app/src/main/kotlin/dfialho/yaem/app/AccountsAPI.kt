package dfialho.yaem.app

import dfialho.yaem.app.api.Account
import dfialho.yaem.app.controllers.AccountController
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import kotlinx.serialization.list

fun Route.accounts(controller: AccountController) = route("accounts") {

    post {
        val account = json.validatedParse(Account.serializer(), call.receiveText())

        controller.create(account)

        call.respondText(ContentType.Application.Json, HttpStatusCode.Created) {
            json.stringify(Account.serializer(), account)
        }
    }

    get {
        val accounts = controller.list()

        call.respondText(ContentType.Application.Json, HttpStatusCode.OK) {
            json.stringify(Account.serializer().list, accounts)
        }
    }

    get("{id}") {
        val receivedID = call.parameters["id"] ?: throw IllegalArgumentException("ID parameter is required")

        val account = controller.get(receivedID)

        call.respondText(ContentType.Application.Json, HttpStatusCode.OK) {
            json.stringify(Account.serializer(), account)
        }
    }

    put("{id}") {
        val accountID = call.parameters["id"] ?: throw IllegalArgumentException("ID parameter is required")
        val account = json.validatedParse(Account.serializer(), call.receiveText())

        controller.update(accountID, account)
        val updatedAccount = account.copy(id = accountID)

        call.respondText(ContentType.Application.Json , HttpStatusCode.Accepted) {
            json.stringify(Account.serializer(), updatedAccount)
        }
    }

    delete("{id}") {
        val receivedID = call.parameters["id"] ?: throw IllegalArgumentException("ID parameter is required")

        controller.delete(receivedID)
        call.respond(HttpStatusCode.Accepted)
    }
}
