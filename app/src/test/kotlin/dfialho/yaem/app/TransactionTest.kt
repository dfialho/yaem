package dfialho.yaem.app

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.serialization.json.Json
import org.junit.Test

class TransactionTest {

    @Test
    fun serializationRoundTrip() {
        val transaction = Transaction(amount = 10.5, description = "bananas", incomingAccount = randomID())

        val serializedTransaction = Json.stringify(Transaction.serializer(), transaction)
        val deserializedTransaction = Json.parse(Transaction.serializer(), serializedTransaction)

        assertThat(deserializedTransaction).isEqualTo(transaction)
    }
}