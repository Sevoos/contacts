package net.sevoos.contacts.icon.api

import net.sevoos.contacts.contact.api.ContactApiConfig
import net.sevoos.rest.RestUtility
import net.sevoos.rest.postForObjectBlocking

class IconPrivateApiService(config: ContactApiConfig) {

    private val restUtility = RestUtility(config.contactHost, config.contactApi + "/private")

    fun createIconEntity(contactId: Long): Long =
        restUtility.postForObjectBlocking(CREATE_ICON_ENTITY, contactId)

}