package net.sevoos.contacts.contact.service

import net.sevoos.contacts.contact.dto.ContactCreationDto
import net.sevoos.contacts.contact.dto.ContactDto
import net.sevoos.contacts.contact.dto.ContactPatchDto
import net.sevoos.contacts.contact.dto.SetDefaultIconDto
import net.sevoos.contacts.contact.repository.ContactRepository
import net.sevoos.timezone.isTimezoneValid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class ContactIntermediateService {

    @Autowired
    private lateinit var repository: ContactRepository
    @Autowired
    private lateinit var service: ContactService

    fun deleteContact(id: Long): ResponseEntity<Unit> {
        if (!service.contactExists(id)) {
            return ResponseEntity.status(NOT_FOUND).body(null)
        }
        service.deleteContact(id)
        return ResponseEntity.ok(null)
    }

    fun createContact(dto: ContactCreationDto): ResponseEntity<Long> {
        if (isTimezoneValid(dto.timezone)) {
            return ResponseEntity.ok(service.saveDtoToEntity(dto))
        } else {
            return ResponseEntity.badRequest().body(null)
        }
    }

    fun getContact(id: Long): ResponseEntity<ContactDto> {
        val optionalEntity = repository.findById(id)
        if (optionalEntity.isEmpty) {
            return ResponseEntity.status(NOT_FOUND).body(null)
        }
        return ResponseEntity.ok(service.entityToDto(optionalEntity.get()))
    }

    fun getModificationDate(id: Long): ResponseEntity<Long> {
        val optionalEntity = repository.findById(id)
        if (optionalEntity.isEmpty) {
            return ResponseEntity.status(NOT_FOUND).body(null)
        }
        return ResponseEntity.ok(optionalEntity.get().modificationDate)
    }

    fun patchContact(id: Long, patchDto: ContactPatchDto): ResponseEntity<ContactDto?> {
        val optionalEntity = repository.findById(id)
        if (optionalEntity.isEmpty) {
            return ResponseEntity.status(NOT_FOUND).body(null)
        }
        if (patchDto.timezone.isPresent && !isTimezoneValid(patchDto.timezone.get())) {
            return ResponseEntity.badRequest().body(null)
        }
        val newDtoIfModified = service.patchContact(optionalEntity.get(), patchDto)
        return ResponseEntity.status(if (newDtoIfModified == null) NO_CONTENT else OK).body(newDtoIfModified)
    }

    fun setDefaultIcon(dto: SetDefaultIconDto): ResponseEntity<Unit> {
        if (!service.contactExists(dto.contactId)) {
            return ResponseEntity.status(NOT_FOUND).body(null)
        }
        service.setDefaultIcon(dto.contactId, dto.iconId)
        return ResponseEntity.ok(null)
    }

}