package net.sevoos.contacts.contact.service

import net.sevoos.contacts.contact.repository.IconRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class IconIntermediateService {

    @Autowired
    private lateinit var contactService: ContactService
    @Autowired
    private lateinit var repository: IconRepository
    @Autowired
    private lateinit var service: IconService

    fun createIconEntity(contactId: Long): ResponseEntity<Long> {
        if (!contactService.contactExists(contactId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
        val iconId = service.createEntity(contactId)
        contactService.updateModificationDate(contactId)
        return ResponseEntity.ok(iconId)
    }

    fun deleteIconEntity(iconId: Long): ResponseEntity<Unit> {
        val optionalEntity = repository.findById(iconId)
        if (optionalEntity.isEmpty) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
        val entity = optionalEntity.get()
        contactService.updateModificationDate(entity.contactId)
        service.deleteIconEntity(entity)
        return ResponseEntity.ok(null)
    }

}