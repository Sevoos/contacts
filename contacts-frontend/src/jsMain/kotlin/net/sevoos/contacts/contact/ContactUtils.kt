package net.sevoos.contacts.contact

import net.sevoos.contacts.contact.api.ContactPublicApiFrontendService
import net.sevoos.contacts.contact.dto.ContactPatchDtoJs
import net.sevoos.contacts.contact.dto.ContactPatchRequest

private fun ContactPatchDtoJs.toRequest(): ContactPatchRequest =
    ContactPatchRequest(
        birthdayDate,
        category,
        defaultIconId,
        displayName,
        firstName,
        lastName,
        patronymicName,
        timezone
    )

@JsExport
fun patchContact(id: Long, patchDto: ContactPatchDtoJs, apiService: ContactPublicApiFrontendService) =
    apiService.patchContact(id, patchDto.toRequest())