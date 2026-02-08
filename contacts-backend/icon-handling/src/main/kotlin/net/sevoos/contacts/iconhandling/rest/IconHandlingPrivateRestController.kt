package net.sevoos.contacts.iconhandling.rest

import net.sevoos.contacts.iconhandling.api.DELETE_ICON_FILE
import net.sevoos.contacts.iconhandling.service.IconHandlingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["private"])
@Suppress("unused")
class IconHandlingPrivateRestController {

    @Autowired
    private lateinit var iconHandlingService: IconHandlingService

    @PostMapping(path = [DELETE_ICON_FILE])
    fun deleteIcon(@RequestBody iconId: Long) = iconHandlingService.deleteIcon(iconId)

}