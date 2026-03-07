package net.sevoos.rest

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class PatchFieldSerializer<T : Any>(
    private val valueSerializer: KSerializer<T>
) : KSerializer<PatchField<T>> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildSerialDescriptor("PatchField", SerialKind.CONTEXTUAL)

    override fun serialize(encoder: Encoder, value: PatchField<T>) {
        when (value) {
            PatchField.Undefined -> {

            }

            is PatchField.Present -> {
                val nullableSerializer = valueSerializer.nullable
                encoder.encodeSerializableValue(nullableSerializer, value.value)
            }
        }
    }

    override fun deserialize(decoder: Decoder): PatchField<T> {
        val nullableSerializer = valueSerializer.nullable
        val decoded = decoder.decodeSerializableValue(nullableSerializer)
        return PatchField.Present(decoded)
    }
}
