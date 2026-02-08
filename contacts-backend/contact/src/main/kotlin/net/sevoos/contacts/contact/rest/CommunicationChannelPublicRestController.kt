package net.sevoos.contacts.contact.rest

import net.sevoos.contacts.communicationchannel.dto.CommunicationChannelCreationDto
import net.sevoos.contacts.communicationchannel.api.CREATE_COMMUNICATION_CHANNEL
import net.sevoos.contacts.communicationchannel.api.DELETE_COMMUNICATION_CHANNEL
import net.sevoos.contacts.communicationchannel.api.GET_COMMUNICATION_CHANNEL
import net.sevoos.contacts.communicationchannel.api.PATCH_COMMUNICATION_CHANNEL
import net.sevoos.contacts.communicationchannel.dto.CommunicationChannelDto
import net.sevoos.contacts.contact.dto.CommunicationChannelPatchDto
import net.sevoos.contacts.contact.service.CommunicationChannelIntermediateService
import net.sevoos.contacts.contact.service.CommunicationChannelService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Suppress("unused")
class CommunicationChannelPublicRestController {

    @Autowired
    private lateinit var intermediateService: CommunicationChannelIntermediateService
    @Autowired
    private lateinit var service: CommunicationChannelService

    @ResponseBody
    @PostMapping(path = [CREATE_COMMUNICATION_CHANNEL])
    fun createCommunicationChannel(@RequestBody dto: CommunicationChannelCreationDto): ResponseEntity<Long> =
        intermediateService.createCommunicationChannel(dto)

    @PostMapping(path = [DELETE_COMMUNICATION_CHANNEL])
    fun deleteCommunicationChannel(@RequestBody id: Long): ResponseEntity<Unit> =
        intermediateService.deleteCommunicationChannel(id)

    @ResponseBody
    @PostMapping(path = [GET_COMMUNICATION_CHANNEL])
    fun getCommunicationChannel(@RequestBody id: Long): ResponseEntity<CommunicationChannelDto> =
        intermediateService.getCommunicationChannel(id)

    @PatchMapping(path = ["$PATCH_COMMUNICATION_CHANNEL/{id}"])
    fun patchCommunicationChannel(
        @PathVariable id: Long,
        @RequestBody patchDto: CommunicationChannelPatchDto
    ): ResponseEntity<CommunicationChannelDto?> = intermediateService.patchCommunicationChannel(id, patchDto)

}