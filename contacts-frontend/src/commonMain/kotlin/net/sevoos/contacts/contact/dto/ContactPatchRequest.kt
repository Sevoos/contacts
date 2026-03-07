package net.sevoos.contacts.contact.dto

import kotlinx.serialization.Serializable
import net.sevoos.contacts.birthdaydate.dto.BirthdayDateDto
import net.sevoos.rest.PatchField

@Serializable
data class ContactPatchRequest(
    val birthdayDate: PatchField<BirthdayDateDto> = PatchField.Undefined,
    val category: PatchField<Array<String>> = PatchField.Undefined,
    val defaultIconId: PatchField<Long> = PatchField.Undefined,
    val displayName: PatchField<String> = PatchField.Undefined,
    val firstName: PatchField<String> = PatchField.Undefined,
    val lastName: PatchField<String> = PatchField.Undefined,
    val patronymicName: PatchField<String> = PatchField.Undefined,
    val timezone: PatchField<String> = PatchField.Undefined
)

