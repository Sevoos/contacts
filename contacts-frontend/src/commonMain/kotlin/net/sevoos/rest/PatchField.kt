package net.sevoos.rest

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable(with = PatchFieldSerializer::class)
sealed class PatchField<out T: Any> {
    @Serializable
    object Undefined : PatchField<Nothing>()

    @Serializable
    data class Present<T : Any>(val value: T?) : PatchField<T>()
}