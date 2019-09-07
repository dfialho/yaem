package dfialho.yaem.app.api

import dfialho.yaem.app.api.serializers.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Transaction(
    val amount: Double,
    val receiver: ID,
    val sender: ID? = null,
    val description: String = "",
    @Serializable(with = InstantSerializer::class) val timestamp: Instant = Instant.now(),
    val id: ID = randomID()
)
