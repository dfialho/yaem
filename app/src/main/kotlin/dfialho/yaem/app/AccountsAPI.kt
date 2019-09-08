package dfialho.yaem.app

import dfialho.yaem.app.api.Account
import dfialho.yaem.app.controllers.AccountController
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*

fun Route.accounts(controller: AccountController) = route("accounts") {

    post {
        val account = call.validatedReceive<Account>()
        val createdAccount = controller.create(account)
        call.respond(HttpStatusCode.Created, createdAccount)
    }

    get {
        val accounts = controller.list()
        call.respond(HttpStatusCode.OK, accounts)
    }

    get("{id}") {
        val receivedID = call.parameters["id"] ?: throw IllegalArgumentException("ID parameter is required")
        val account = controller.get(receivedID)
        call.respond(HttpStatusCode.OK, account)
    }

    put {
        val account = call.validatedReceive<Account>()
        val updatedAccount = controller.update(account)
        call.respond(HttpStatusCode.Accepted, updatedAccount)
    }

    delete("{id}") {
        val receivedID = call.parameters["id"] ?: throw IllegalArgumentException("ID parameter is required")
        controller.delete(receivedID)
        call.respond(HttpStatusCode.Accepted)
    }
}
