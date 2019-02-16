package dfialho.yaem.app

import dfialho.yaem.app.api.serializers.InstantSerializer
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import java.time.Instant

const val ACCOUNT_NAME_MAX_LENGTH = 32

@Serializable
data class Account (
    val name: String,
    @Optional val initialBalance: Double = 0.0,
    @Serializable(with = InstantSerializer::class) @Optional val startTimestamp: Instant = Instant.now(),
    @Optional val id: ID = randomID()
)
