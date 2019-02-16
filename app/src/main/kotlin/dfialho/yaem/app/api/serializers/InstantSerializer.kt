package dfialho.yaem.app.api.serializers

import kotlinx.serialization.*
import kotlinx.serialization.internal.LongDescriptor
import java.time.Instant

@Serializer(forClass = Instant::class)
object InstantSerializer: KSerializer<Instant> {

    override val descriptor: SerialDescriptor = LongDescriptor.withName("WithCustomDefault")

    override fun serialize(encoder: Encoder, obj: Instant) {
        encoder.encodeLong(obj.toEpochMilli())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.ofEpochMilli(decoder.decodeLong())
    }
}
