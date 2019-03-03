package dfialho.yaem.app

import dfialho.yaem.app.managers.LedgerManager
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

fun Route.ledger(manager: LedgerManager) {

    post("ledger") {
        val transaction = Json.validatedParse(Transaction.serializer(), call.receiveText())

        manager.create(transaction)

        call.respond(HttpStatusCode.Created, Json.stringify(Transaction.serializer(), transaction))
    }

    get("ledger") {
        call.respond(Json.stringify(Transaction.serializer().list, manager.list()))
    }

    get("ledger/{id}") {
        val receivedID = call.parameters["id"] ?: throw IllegalArgumentException("Transaction ID is required")

        val transaction = manager.get(receivedID)
        call.respond(HttpStatusCode.OK, Json.stringify(Transaction.serializer(), transaction))
    }

    delete("ledger/{id}") {
        val receivedID = call.parameters["id"] ?: throw IllegalArgumentException("Transaction ID is required")

        manager.delete(receivedID)
        call.respond(HttpStatusCode.Accepted, "Transaction with id '$receivedID' was deleted")
    }
}
