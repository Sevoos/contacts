package net.sevoos.contacts.icon.api

import net.sevoos.contacts.contact.api.ContactApiConfig
import net.sevoos.generation.annotations.AsynchronousService
import net.sevoos.rest.ResponseEntity
import net.sevoos.rest.RestUtility

@AsynchronousService
class IconPublicApiAsynchronousService(config: ContactApiConfig) {

    private val restUtility = RestUtility(config.hostContact, config.apiContact)

    suspend fun checkIconExistence(id: Long): Boolean =
        restUtility.postForObject(CHECK_ICON_EXISTENCE, id)

    suspend fun deleteIconEntity(iconId: Long): ResponseEntity<Unit> = restUtility.postForEntity(
        DELETE_ICON_ENTITY,
        iconId
    )

}