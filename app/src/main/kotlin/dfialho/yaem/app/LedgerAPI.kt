package dfialho.yaem.app

import dfialho.yaem.app.managers.LedgerManager
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.*
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

    put("ledger/{id}") {
        val trxID = call.parameters["id"] ?: throw IllegalArgumentException("Transaction ID is required")
        val trx = Json.validatedParse(Transaction.serializer(), call.receiveText())

        manager.update(trxID, trx)
        val updatedTrx = trx.copy(id = trxID)

        call.respond(HttpStatusCode.Accepted, Json.stringify(Transaction.serializer(), updatedTrx))
    }

    delete("ledger/{id}") {
        val receivedID = call.parameters["id"] ?: throw IllegalArgumentException("Transaction ID is required")

        manager.delete(receivedID)
        call.respond(HttpStatusCode.Accepted, "Transaction with id '$receivedID' was deleted")
    }
}
