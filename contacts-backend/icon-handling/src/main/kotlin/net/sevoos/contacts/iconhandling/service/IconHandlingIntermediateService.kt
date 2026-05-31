package net.sevoos.contacts.iconhandling.service

import net.sevoos.contacts.contact.api.ContactPublicApiService
import net.sevoos.contacts.icon.api.IconPublicApiService
import net.sevoos.contacts.contact.ContactApiSpringConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream

@Service
class IconHandlingIntermediateService(
    contactApiSpringConfig: ContactApiSpringConfig
) {

    private val contactApiService = ContactPublicApiService(contactApiSpringConfig)
    private val iconApiService = IconPublicApiService(contactApiSpringConfig)
    @Autowired
    private lateinit var service: IconHandlingService

    private fun getMissingEntityResponse(id: Long): ResponseEntity<InputStreamResource>? =
        if (iconApiService.checkIconExistence(id)) null
        else ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)

    fun addIconToContact(contactId: Long, image: BufferedImage): ResponseEntity<Long> {
        if (!contactApiService.checkContactExistence(contactId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
        return ResponseEntity.ok(service.addIconToContact(contactId, image))
    }

    fun downloadIconFullQuality(id: Long): ResponseEntity<InputStreamResource> {
        val missingEntityResponse = getMissingEntityResponse(id)
        if (missingEntityResponse != null) {
            return missingEntityResponse
        }
        return service.downloadIcon(service.getFullQualityPath(id))
    }

    fun downloadIconPreview(id: Long): ResponseEntity<InputStreamResource> {
        val missingEntityResponse = getMissingEntityResponse(id)
        if (missingEntityResponse != null) {
            return missingEntityResponse
        }
        return service.downloadIcon(service.getPreviewPath(id))
    }

}