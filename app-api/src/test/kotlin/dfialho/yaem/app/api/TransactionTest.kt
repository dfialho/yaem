package dfialho.yaem.app.api

import assertk.assertThat
import assertk.assertions.isEqualTo
import dfialho.yaem.app.api.serializers.TransactionSerializer
import dfialho.yaem.json.lib.json
import kotlinx.serialization.list
import org.junit.Test
import java.time.Instant

class TransactionTest {

    @Test
    fun `serialization round-trip of one way transaction`() {
        val transaction = OneWayTransaction(
            account = randomID(),
            amount = 10.5,
            description = "bananas",
            timestamp = Instant.ofEpochMilli(1551633736370)
        )

        val serialized = json.stringify(OneWayTransaction.serializer(), transaction)
        val deserialized = json.parse(OneWayTransaction.serializer(), serialized)

        assertThat(deserialized).isEqualTo(transaction)
    }

    @Test
    fun `serialization round-trip of transfer`() {
        val transaction = Transfer(
            outgoingAccount = randomID(),
            incomingAccount= randomID(),
            amount = 10.5,
            description = "bananas",
            timestamp = Instant.ofEpochMilli(1551633736370)
        )

        val serialized = json.stringify(Transfer.serializer(), transaction)
        val deserialized = json.parse(Transfer.serializer(), serialized)

        assertThat(deserialized).isEqualTo(transaction)
    }

    @Test
    fun `serialization round-trip of one-way transaction with generic serializer`() {
        val transaction = OneWayTransaction(
            account = randomID(),
            amount = 10.5,
            description = "bananas",
            timestamp = Instant.ofEpochMilli(1551633736370),
            id = randomID()
        )

        val serialized = json.stringify(TransactionSerializer, transaction)
        val deserialized = json.parse(TransactionSerializer, serialized)

        assertThat(deserialized).isEqualTo(transaction)
    }

    @Test
    fun `serialization round-trip of transfer with generic serializer`() {
        val transaction = Transfer(
            outgoingAccount = randomID(),
            incomingAccount= randomID(),
            amount = 10.5,
            description = "bananas",
            timestamp = Instant.ofEpochMilli(1551633736370)
        )

        val serialized = json.stringify(TransactionSerializer, transaction)
        val deserialized = json.parse(TransactionSerializer, serialized)

        assertThat(deserialized).isEqualTo(transaction)
    }

    @Test
    fun `serialization round-trip of list of multiple types of transactions`() {
        val transactions = listOf(
            OneWayTransaction(
                account = randomID(),
                amount = 11.5,
                description = "one way transaction"
            ),
            Transfer(
                outgoingAccount = randomID(),
                incomingAccount= randomID(),
                amount = 10.5,
                description = "transfer"
            )
        )

        val serialized = json.stringify(TransactionSerializer.list, transactions)
        val deserialized = json.parse(TransactionSerializer.list, serialized)

        assertThat(deserialized).isEqualTo(transactions)
    }
}
