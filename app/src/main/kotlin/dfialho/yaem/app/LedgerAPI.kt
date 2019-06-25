package dfialho.yaem.app

import dfialho.yaem.app.api.Transaction
import dfialho.yaem.app.managers.LedgerManager
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.list

fun Route.ledger(manager: LedgerManager) = route("ledger") {

    post {
        val transaction = json.validatedParse(Transaction.serializer(), call.receiveText())

        manager.create(transaction)

        call.respondText(ContentType.Application.Json, HttpStatusCode.Created) {
            json.stringify(Transaction.serializer(), transaction)
        }
    }

    get {
        val transactions = manager.list()

        call.respondText(ContentType.Application.Json, HttpStatusCode.OK) {
            json.stringify(Transaction.serializer().list, transactions)
        }
    }

    get("{id}") {
        val receivedID = call.parameters["id"] ?: throw IllegalArgumentException("Transaction ID is required")

        val transaction = manager.get(receivedID)

        call.respondText(ContentType.Application.Json, HttpStatusCode.OK) {
            json.stringify(Transaction.serializer(), transaction)
        }
    }

    put("{id}") {
        val trxID = call.parameters["id"] ?: throw IllegalArgumentException("Transaction ID is required")
        val trx = json.validatedParse(Transaction.serializer(), call.receiveText())

        manager.update(trxID, trx)
        val updatedTrx = trx.copy(id = trxID)

        call.respondText(ContentType.Application.Json, HttpStatusCode.Accepted) {
            json.stringify(Transaction.serializer(), updatedTrx)
        }
    }

    delete("{id}") {
        val receivedID = call.parameters["id"] ?: throw IllegalArgumentException("Transaction ID is required")

        manager.delete(receivedID)
        call.respond(HttpStatusCode.Accepted)
    }
}
