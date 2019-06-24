package dfialho.yaem.app.api

import dfialho.yaem.app.api.serializers.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

const val ACCOUNT_NAME_MAX_LENGTH = 32

@Serializable
data class Account (
    val name: String,
    val initialBalance: Double = 0.0,
    @Serializable(with = InstantSerializer::class) val startTimestamp: Instant = Instant.now(),
    val id: ID = randomID()
)
