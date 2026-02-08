package net.sevoos.contacts.contact.rest

import net.sevoos.contacts.contact.api.CHECK_CONTACT_EXISTENCE
import net.sevoos.contacts.contact.api.CREATE_CONTACT
import net.sevoos.contacts.contact.api.DELETE_CONTACT
import net.sevoos.contacts.contact.api.GET_ALL_CONTACTS_IDS
import net.sevoos.contacts.contact.api.GET_CONTACT
import net.sevoos.contacts.contact.api.GET_MODIFICATION_DATE
import net.sevoos.contacts.contact.api.PATCH_CONTACT
import net.sevoos.contacts.contact.api.SET_DEFAULT_ICON
import net.sevoos.contacts.contact.dto.ContactCreationDto
import net.sevoos.contacts.contact.dto.ContactDto
import net.sevoos.contacts.contact.dto.ContactPatchDto
import net.sevoos.contacts.contact.dto.SetDefaultIconDto
import net.sevoos.contacts.contact.service.ContactIntermediateService
import net.sevoos.contacts.contact.service.ContactService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Suppress("unused")
class ContactPublicRestController {

    @Autowired
    private lateinit var intermediateService: ContactIntermediateService
    @Autowired
    private lateinit var service: ContactService

    @ResponseBody
    @PostMapping(path = [CHECK_CONTACT_EXISTENCE])
    fun checkContactExistence(@RequestBody id: Long): ResponseEntity<Boolean> =
        ResponseEntity.ok(service.contactExists(id))

    @ResponseBody
    @PostMapping(path = [CREATE_CONTACT])
    fun createContact(@RequestBody dto: ContactCreationDto): ResponseEntity<Long> =
        intermediateService.createContact(dto)

    @ResponseBody
    @PostMapping(path = [DELETE_CONTACT])
    fun deleteContact(@RequestBody id: Long): ResponseEntity<Unit> =
        intermediateService.deleteContact(id)

    @ResponseBody
    @GetMapping(path = [GET_ALL_CONTACTS_IDS])
    fun getAllContactsIds(): ResponseEntity<List<Long>> =
        ResponseEntity.ok(service.getAllContactsIds())

    @ResponseBody
    @PostMapping(path = [GET_CONTACT])
    fun getContact(@RequestBody id: Long): ResponseEntity<ContactDto> =
        intermediateService.getContact(id)

    @ResponseBody
    @PostMapping(path = [GET_MODIFICATION_DATE])
    fun getModificationDate(@RequestBody id: Long): ResponseEntity<Long> =
        intermediateService.getModificationDate(id)

    @ResponseBody
    @PatchMapping(path = ["$PATCH_CONTACT/{id}"])
    fun patchContact(@PathVariable id: Long, @RequestBody patchDto: ContactPatchDto): ResponseEntity<ContactDto?> =
        intermediateService.patchContact(id, patchDto)

    @ResponseBody
    @PostMapping(path = [SET_DEFAULT_ICON])
    fun setDefaultIcon(@RequestBody dto: SetDefaultIconDto): ResponseEntity<Unit> =
        intermediateService.setDefaultIcon(dto)

}