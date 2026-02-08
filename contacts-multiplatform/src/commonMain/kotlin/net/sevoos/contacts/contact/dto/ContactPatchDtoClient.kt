package net.sevoos.contacts.contact.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import net.sevoos.contacts.birthdaydate.dto.BirthdayDateDto
import kotlin.js.JsExport

@Serializable
@JsExport
class ContactPatchDtoClient(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val birthdayDate: BirthdayDateDto? = null,
    val category: Array<String>? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val defaultIconId: Long? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val displayName: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val patronymicName: String? = null,
    val timezone: String? = null
)

