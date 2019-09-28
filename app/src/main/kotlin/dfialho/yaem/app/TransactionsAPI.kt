package dfialho.yaem.app

import dfialho.yaem.app.api.Transaction
import dfialho.yaem.app.controllers.TransactionController
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*

fun Route.transactions(controller: TransactionController) = route("transactions") {

    post {
        val transaction = call.validatedReceive<Transaction>()
        val createdTransaction = controller.create(transaction)
        call.respond(HttpStatusCode.Created, createdTransaction)
    }

    get {
        val transactions = controller.list()
        call.respond(HttpStatusCode.OK, transactions)
    }

    get("{id}") {
        val receivedID = call.parameters["id"] ?: throw IllegalArgumentException("ID parameter is required")
        val transaction = controller.get(receivedID)
        call.respond(HttpStatusCode.OK, transaction)
    }

    put {
        val transaction = call.validatedReceive<Transaction>()
        val updatedTransaction = controller.update(transaction)
        call.respond(HttpStatusCode.Accepted, updatedTransaction)
    }

    delete("{id}") {
        val receivedID = call.parameters["id"] ?: throw IllegalArgumentException("ID parameter is required")
        controller.delete(receivedID)
        call.respond(HttpStatusCode.Accepted)
    }
}
