package net.sevoos.contacts.contact.rest

import net.sevoos.contacts.icon.api.CREATE_ICON_ENTITY
import net.sevoos.contacts.contact.service.IconIntermediateService
import net.sevoos.contacts.contact.service.IconService
import net.sevoos.contacts.icon.api.CHECK_ICON_EXISTENCE
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RequestMapping(path = ["private"])
@RestController
@Suppress("unused")
class IconPrivateRestController {

    @Autowired
    private lateinit var intermediateService: IconIntermediateService
    @Autowired
    private lateinit var service: IconService

    @ResponseBody
    @PostMapping(path = [CREATE_ICON_ENTITY])
    fun createIconEntity(@RequestBody contactId: Long): ResponseEntity<Long> =
        intermediateService.createIconEntity(contactId)

}