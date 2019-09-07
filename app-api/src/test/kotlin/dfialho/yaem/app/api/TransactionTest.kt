package dfialho.yaem.app.api

import assertk.assertThat
import assertk.assertions.isEqualTo
import dfialho.yaem.json.lib.json
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.list
import kotlinx.serialization.serializer
import org.junit.Test
import java.time.Instant

@ImplicitReflectionSerializer
class TransactionTest {

    @ImplicitReflectionSerializer
    inline fun <reified T : Any> assertRoundTripSerialization(item: T) {
        @Suppress("UNCHECKED_CAST")
        val serializer = item::class.serializer() as KSerializer<Any>

        val serialized = json.stringify(serializer , item)
        val deserialized = json.parse(serializer, serialized)
        assertThat(deserialized).isEqualTo(item)
    }

    @Test
    fun `serialization round-trip of one way transaction`() {

        assertRoundTripSerialization(
            Transaction(
                amount = 10.5,
                description = "bananas",
                receiver = randomID(),
                timestamp = Instant.ofEpochMilli(1551633736370)
            )
        )
    }

    @Test
    fun `serialization round-trip of transfer`() {

        assertRoundTripSerialization(
            Transaction(
                amount = 10.5,
                description = "bananas",
                receiver = randomID(),
                sender = randomID(),
                timestamp = Instant.ofEpochMilli(1551633736370)
            )
        )
    }

    @Test
    fun `serialization round-trip of list of multiple transactions`() {

        val transactions = listOf(
            Transaction(
                amount = 10.5,
                description = "bananas",
                receiver = randomID(),
                timestamp = Instant.ofEpochMilli(1551633736370)
            ),
            Transaction(
                amount = 10.5,
                description = "bananas",
                receiver = randomID(),
                sender = randomID(),
                timestamp = Instant.ofEpochMilli(1551633736370)
            )
        )

        val serializer = Transaction.serializer().list
        val serialized = json.stringify(serializer, transactions)
        val deserialized = json.parse(serializer, serialized)

        assertThat(deserialized).isEqualTo(transactions)
    }
}
