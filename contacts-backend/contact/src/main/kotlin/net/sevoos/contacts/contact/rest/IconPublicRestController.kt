package net.sevoos.contacts.contact.rest

import net.sevoos.contacts.contact.service.IconIntermediateService
import net.sevoos.contacts.contact.service.IconService
import net.sevoos.contacts.icon.api.CHECK_ICON_EXISTENCE
import net.sevoos.contacts.icon.api.DELETE_ICON_ENTITY
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Suppress("unused")
class IconPublicRestController {

    @Autowired
    private lateinit var intermediateService: IconIntermediateService
    @Autowired
    private lateinit var service: IconService

    @ResponseBody
    @PostMapping(path = [CHECK_ICON_EXISTENCE])
    fun checkIconExistence(@RequestBody id: Long): ResponseEntity<Boolean> =
        ResponseEntity.ok(service.checkIconExistence(id))

    @PostMapping(path = [DELETE_ICON_ENTITY])
    fun deleteIconEntity(@RequestBody iconId: Long): ResponseEntity<Unit> =
        intermediateService.deleteIconEntity(iconId)

}