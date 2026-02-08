package net.sevoos.generation

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

object LongAsStringSerializer : KSerializer<String> {
    override val descriptor = PrimitiveSerialDescriptor("LongAsString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) = encoder.encodeString(value)

    override fun deserialize(decoder: Decoder): String {
//        val element = (decoder as JsonDecoder).decodeJsonElement()
//        return when (element) {
//            is JsonPrimitive -> if (element.isString) element.content else element.long.toString()
//            else -> error("Invalid value")
//        }
        return when (val element = (decoder as JsonDecoder).decodeJsonElement()) {
            is JsonPrimitive -> if (element.isString) element.content else element.long.toString()
            else -> error("Invalid value")
        }
    }
}