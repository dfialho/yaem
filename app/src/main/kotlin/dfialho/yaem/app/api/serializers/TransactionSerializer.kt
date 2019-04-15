package dfialho.yaem.app.api.serializers

import dfialho.yaem.app.OneWayTransaction
import dfialho.yaem.app.Transaction
import dfialho.yaem.app.Transfer
import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl

@Serializer(forClass = Transaction::class)
object TransactionSerializer : KSerializer<Transaction> {

    override val descriptor: SerialDescriptor = object : SerialClassDescImpl("kotlin.Any") {
        override val kind: SerialKind = UnionKind.SEALED

        init {
            addElement("type")
            addElement("value")
        }
    }

    override fun serialize(encoder: Encoder, obj: Transaction) {

        encoder.encodeStructure(descriptor) {

            val serializer = when (obj) {
                is OneWayTransaction -> OneWayTransaction.serializer()
                is Transfer -> Transfer.serializer()
            }

            encodeStringElement(descriptor, 0, transactionTypeOf(obj))
            @Suppress("UNCHECKED_CAST")
            encodeSerializableElement(descriptor, 1, serializer as SerializationStrategy<Transaction>, obj)
        }
    }

    override fun deserialize(decoder: Decoder): Transaction {

        return decoder.decodeStructure(descriptor) {

            val initialIndex = decodeElementIndex(descriptor)

            if (initialIndex == CompositeDecoder.READ_ALL) {
                val type = decodeStringElement(descriptor, 0)
                return@decodeStructure decodeSerializableElement(descriptor, 1, serializerForType(type))

            } else {
                var index = initialIndex
                var type: String? = null
                var transaction: Transaction? = null

                mainLoop@ while (index != CompositeDecoder.READ_DONE) {
                    when (index) {
                        CompositeDecoder.READ_DONE -> break@mainLoop
                        0 -> type = decodeStringElement(descriptor, 0)
                        1 -> transaction = decodeSerializableElement(descriptor, 1, serializerForType(type))
                        else -> throw SerializationException("Invalid index: $index")
                    }

                    index = decodeElementIndex(descriptor)
                }

                return@decodeStructure requireNotNull(transaction) { "Field 'properties' is required" }
            }
        }
    }

}

private fun serializerForType(type: String?): KSerializer<out Transaction> {
    requireNotNull(type) { "Field 'type' is required" }

    return when (type) {
        "OneWay" -> OneWayTransaction.serializer()
        "Transfer" -> Transfer.serializer()
        else -> throw SerializationException("Transaction of type '$type' is not recognized")
    }
}

private fun transactionTypeOf(transaction: Transaction): String = when (transaction) {
    is OneWayTransaction -> "OneWay"
    is Transfer -> "Transfer"
}

private fun Encoder.encodeStructure(desc: SerialDescriptor, block: CompositeEncoder.() -> Unit) {
    val encoder = beginStructure(desc)
    block(encoder)
    encoder.endStructure(desc)
}

private fun <R> Decoder.decodeStructure(desc: SerialDescriptor, block: CompositeDecoder.() -> R): R {
    val decoder = beginStructure(desc)
    val result = block(decoder)
    decoder.endStructure(desc)
    return result
}
