package net.sevoos.contacts.communicationchannel.api

import net.sevoos.contacts.communicationchannel.dto.CommunicationChannelCreationDto
import net.sevoos.contacts.communicationchannel.dto.CommunicationChannelDto
import net.sevoos.contacts.communicationchannel.dto.CommunicationChannelPatchDtoClient
import net.sevoos.contacts.contact.api.ContactApiConfig
import net.sevoos.generation.annotations.AsynchronousService
import net.sevoos.rest.ResponseEntity
import net.sevoos.rest.RestUtility

@AsynchronousService
class CommunicationChannelPublicApiAsynchronousService(
    config: ContactApiConfig
) {

    private val restUtility = RestUtility(config.hostContact, config.apiContact)

    suspend fun createCommunicationChannel(dto: CommunicationChannelCreationDto): ResponseEntity<Long> =
        restUtility.postForEntity(DELETE_COMMUNICATION_CHANNEL, dto)

    suspend fun deleteCommunicationChannel(id: Long): ResponseEntity<Unit> =
        restUtility.postForEntityLongBody(DELETE_COMMUNICATION_CHANNEL, id)

    suspend fun getCommunicationChannel(id: Long): ResponseEntity<CommunicationChannelDto> =
        restUtility.postForEntityLongBody(GET_COMMUNICATION_CHANNEL, id)

    suspend fun patchCommunicationChannel(id: Long, patchDto: CommunicationChannelPatchDtoClient): ResponseEntity<CommunicationChannelDto?> =
        restUtility.patchForEntity(PATCH_COMMUNICATION_CHANNEL, patchDto, id)

}