package net.sevoos.contacts.birthdaydate.dto

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class BirthdayDateDto (
    var year: Int? = null,
    var month: Int = 0,
    var day: Int = 0
)