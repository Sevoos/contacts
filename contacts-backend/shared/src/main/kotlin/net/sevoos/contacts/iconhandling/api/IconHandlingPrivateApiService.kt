package net.sevoos.contacts.iconhandling.api

import net.sevoos.rest.RestUtility
import net.sevoos.rest.postForObjectBlocking

class IconHandlingPrivateApiService(
    config: IconHandlingApiConfig
) {

    private val restUtility = RestUtility(config.hostIconHandling, config.apiIconHandling + "/private")

    fun deleteIcon(iconId: Long) = restUtility.postForObjectBlocking<Unit>(
        DELETE_ICON_FILE,
        iconId
    )

}