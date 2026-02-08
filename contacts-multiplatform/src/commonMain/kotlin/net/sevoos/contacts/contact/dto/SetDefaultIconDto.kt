package net.sevoos.contacts.contact.dto

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class SetDefaultIconDto(
    val contactId: Long,
    val iconId: Long
)
