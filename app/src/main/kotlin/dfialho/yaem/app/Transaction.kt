package dfialho.yaem.app

import dfialho.yaem.app.api.serializers.InstantSerializer
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Transaction(
    val amount: Double,
    val description: String,
    val incomingAccount: ID,
    @Optional val sendingAccount: ID? = null,
    @Serializable(with = InstantSerializer::class) @Optional val timestamp: Instant = Instant.now(),
    @Optional val id: ID = randomID()
)