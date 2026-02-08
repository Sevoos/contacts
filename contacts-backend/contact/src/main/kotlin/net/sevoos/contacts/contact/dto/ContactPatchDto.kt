package net.sevoos.contacts.contact.dto

import net.sevoos.contacts.birthdaydate.dto.BirthdayDateDto
import org.openapitools.jackson.nullable.JsonNullable

data class ContactPatchDto(
    val birthdayDate: JsonNullable<BirthdayDateDto?> = JsonNullable.undefined(),
    val category: JsonNullable<List<String>> = JsonNullable.undefined(),
    val defaultIconId: JsonNullable<Long?> = JsonNullable.undefined(),
    val displayName: JsonNullable<String?> = JsonNullable.undefined(),
    val firstName: JsonNullable<String> = JsonNullable.undefined(),
    val lastName: JsonNullable<String> = JsonNullable.undefined(),
    val patronymicName: JsonNullable<String?> = JsonNullable.undefined(),
    val timezone: JsonNullable<String> = JsonNullable.undefined()
)