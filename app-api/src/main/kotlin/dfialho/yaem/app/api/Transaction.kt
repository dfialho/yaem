package dfialho.yaem.app.api

import dfialho.yaem.app.api.serializers.InstantSerializer
import dfialho.yaem.app.api.serializers.TransactionSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

sealed class Transaction {

    companion object {
        fun serializer() = TransactionSerializer
    }

    abstract val id: ID
    abstract val timestamp: Instant
    abstract val amount: Double
    abstract val description: String

    abstract fun copy(id: ID): Transaction
}

@Serializable
data class OneWayTransaction(
    val account: ID,
    override val amount: Double,
    override val description: String = "",
    @Serializable(with = InstantSerializer::class) override val timestamp: Instant = Instant.now(),
    override val id: ID = randomID()
) : Transaction() {

    override fun copy(id: ID): Transaction = copy(id = id, description = description)
}

@Serializable
data class Transfer(
    val outgoingAccount: ID,
    val incomingAccount: ID,
    override val amount: Double,
    override val description: String = "",
    @Serializable(with = InstantSerializer::class) override val timestamp: Instant = Instant.now(),
    override val id: ID = randomID()
) : Transaction() {

    override fun copy(id: ID): Transaction {
        return copy(id = id, description = description)
    }
}
