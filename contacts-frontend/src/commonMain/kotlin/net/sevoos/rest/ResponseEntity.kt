package net.sevoos.rest

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class ResponseEntity<T>(
    val status: Int,
    val body: T?
)
