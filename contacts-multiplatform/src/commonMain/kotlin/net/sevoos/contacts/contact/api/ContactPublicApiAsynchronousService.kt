package net.sevoos.contacts.contact.api

import net.sevoos.contacts.contact.dto.ContactCreationDto
import net.sevoos.contacts.contact.dto.ContactDto
import net.sevoos.contacts.contact.dto.ContactPatchDtoClient
import net.sevoos.contacts.contact.dto.SetDefaultIconDto
import net.sevoos.generation.annotations.AsynchronousService
import net.sevoos.rest.ResponseEntity
import net.sevoos.rest.RestUtility
import net.sevoos.rest.omittableJson

@AsynchronousService
class ContactPublicApiAsynchronousService(config: ContactApiConfig) {

    private val omittableRestUtility = RestUtility(config.hostContact, config.apiContact, omittableJson)
    private val restUtility = RestUtility(config.hostContact, config.apiContact)

    suspend fun checkContactExistence(id: Long): Boolean =
        restUtility.postForObject(CHECK_CONTACT_EXISTENCE, id)

    suspend fun createContact(contactDto: ContactCreationDto): ResponseEntity<Long> =
        restUtility.postForEntity(CREATE_CONTACT, contactDto)

    suspend fun deleteContact(id: Long): ResponseEntity<Unit> =
        restUtility.postForEntityLongBody(DELETE_CONTACT, id)

    suspend fun getAllContactsIds(): List<Long> =
        restUtility.getForObject(GET_ALL_CONTACTS_IDS)

    suspend fun getContact(id: Long): ResponseEntity<ContactDto> =
        restUtility.postForEntityLongBody(GET_CONTACT, id)

    suspend fun getModificationDate(id: Long): ResponseEntity<Long> =
        restUtility.postForEntityLongBody(GET_MODIFICATION_DATE, id)

    suspend fun patchContact(id: Long, patchDto: ContactPatchDtoClient): ResponseEntity<ContactDto?> =
        omittableRestUtility.patchForEntity(PATCH_CONTACT, patchDto, id)

    suspend fun setDefaultIcon(dto: SetDefaultIconDto): ResponseEntity<Unit> =
        restUtility.postForEntity(SET_DEFAULT_ICON, dto)

}