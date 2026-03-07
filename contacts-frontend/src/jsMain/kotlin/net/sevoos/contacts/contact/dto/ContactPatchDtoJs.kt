package net.sevoos.contacts.contact.dto

import kotlin.js.JsExport
import net.sevoos.contacts.birthdaydate.dto.BirthdayDateDto
import net.sevoos.rest.PatchField

@JsExport
@Suppress("unused")
class ContactPatchDtoJs {

    internal var birthdayDate: PatchField<BirthdayDateDto> = PatchField.Undefined
    internal var category: PatchField<Array<String>> = PatchField.Undefined
    internal var defaultIconId: PatchField<Long> = PatchField.Undefined
    internal var displayName: PatchField<String> = PatchField.Undefined
    internal var firstName: PatchField<String> = PatchField.Undefined
    internal var lastName: PatchField<String> = PatchField.Undefined
    internal var patronymicName: PatchField<String> = PatchField.Undefined
    internal var timezone: PatchField<String> = PatchField.Undefined

    fun setBirthdayDate(value: BirthdayDateDto?) {
        birthdayDate = PatchField.Present(value)
    }

    fun setCategory(value: Array<String>) {
        category = PatchField.Present(value)
    }

    fun setDefaultIconId(value: Long?) {
        defaultIconId = PatchField.Present(value)
    }

    fun setDisplayName(value: String?) {
        displayName = PatchField.Present(value)
    }

    fun setFirstName(value: String) {
        firstName = PatchField.Present(value)
    }

    fun setLastName(value: String) {
        lastName = PatchField.Present(value)
    }

    fun setPatronymicName(value: String?) {
        patronymicName = PatchField.Present(value)
    }

    fun setTimezone(value: String) {
        timezone = PatchField.Present(value)
    }
}
