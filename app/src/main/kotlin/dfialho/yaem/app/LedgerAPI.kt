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
        val transaction = json.validatedParse(Transaction.serializer(), call.receiveText())

        manager.create(transaction)

        call.respond(HttpStatusCode.Created, json.stringify(Transaction.serializer(), transaction))
    }

    get("ledger") {
        call.respond(json.stringify(Transaction.serializer().list, manager.list()))
    }

    get("ledger/{id}") {
        val receivedID = call.parameters["id"] ?: throw IllegalArgumentException("Transaction ID is required")

        val transaction = manager.get(receivedID)
        call.respond(HttpStatusCode.OK, json.stringify(Transaction.serializer(), transaction))
    }

    put("ledger/{id}") {
        val trxID = call.parameters["id"] ?: throw IllegalArgumentException("Transaction ID is required")
        val trx = json.validatedParse(Transaction.serializer(), call.receiveText())

        manager.update(trxID, trx)
        val updatedTrx = trx.copy(id = trxID)

        call.respond(HttpStatusCode.Accepted, json.stringify(Transaction.serializer(), updatedTrx))
    }

    delete("ledger/{id}") {
        val receivedID = call.parameters["id"] ?: throw IllegalArgumentException("Transaction ID is required")

        manager.delete(receivedID)
        call.respond(HttpStatusCode.Accepted, "Transaction with id '$receivedID' was deleted")
    }
}
