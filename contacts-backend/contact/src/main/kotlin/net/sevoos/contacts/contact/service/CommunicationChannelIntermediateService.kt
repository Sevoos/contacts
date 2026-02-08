package net.sevoos.contacts.contact.service

import net.sevoos.contacts.communicationchannel.dto.CommunicationChannelCreationDto
import net.sevoos.contacts.communicationchannel.dto.CommunicationChannelDto
import net.sevoos.contacts.contact.dto.CommunicationChannelPatchDto
import net.sevoos.contacts.contact.repository.CommunicationChannelRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.OK

@Service
class CommunicationChannelIntermediateService {

    @Autowired
    private lateinit var contactService: ContactService
    @Autowired
    private lateinit var repository: CommunicationChannelRepository
    @Autowired
    private lateinit var service: CommunicationChannelService

    fun createCommunicationChannel(dto: CommunicationChannelCreationDto): ResponseEntity<Long> {
        val contactId = dto.contactId
        if (!contactService.contactExists(contactId)) {
            return ResponseEntity.status(NOT_FOUND).body(null)
        }
        contactService.updateModificationDate(contactId)
        return ResponseEntity.ok(
            service.createCommunicationChannel(dto)
        )
    }

    fun deleteCommunicationChannel(id: Long): ResponseEntity<Unit> {
        val optionalEntity = repository.findById(id)
        if (optionalEntity.isEmpty) {
            return ResponseEntity.status(NOT_FOUND).body(null)
        }
        val entity = optionalEntity.get()
        service.deleteCommunicationChannel(entity)
        contactService.updateModificationDate(entity.contactId)
        return ResponseEntity.ok(null)
    }

    fun getCommunicationChannel(id: Long): ResponseEntity<CommunicationChannelDto> {
        if (!service.communicationChannelExists(id)) {
            return ResponseEntity.status(NOT_FOUND).body(null)
        }
        return ResponseEntity.ok(service.getCommunicationChannel(id))
    }

    fun patchCommunicationChannel(
        id: Long,
        patchDto: CommunicationChannelPatchDto
    ): ResponseEntity<CommunicationChannelDto?> {
        val optionalEntity = repository.findById(id)
        if (optionalEntity.isEmpty) {
            return ResponseEntity.status(NOT_FOUND).body(null)
        }
        val newDtoIfModified = service.patchCommunicationChannel(optionalEntity.get(), patchDto)
        return ResponseEntity.status(if (newDtoIfModified == null) NO_CONTENT else OK).body(newDtoIfModified)
    }

}