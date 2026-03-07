package net.sevoos.contacts.iconhandling.rest

import net.sevoos.contacts.iconhandling.api.DOWNLOAD_ICON_FULL_QUALITY
import net.sevoos.contacts.iconhandling.api.DOWNLOAD_ICON_PREVIEW
import net.sevoos.contacts.iconhandling.api.UPLOAD_ICON
import net.sevoos.contacts.iconhandling.service.IconHandlingIntermediateService
import net.sevoos.contacts.iconhandling.service.IconHandlingService
import net.sevoos.utilities.fileToImage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


@RestController
@Suppress("unused")
class IconHandlingPublicRestController {

    @Autowired
    private lateinit var service: IconHandlingService
    @Autowired
    private lateinit var intermediateService: IconHandlingIntermediateService

    @ResponseBody
    @PostMapping(path = [DOWNLOAD_ICON_FULL_QUALITY])
    fun downloadIconFullQuality(@RequestBody id: Long): ResponseEntity<InputStreamResource> =
        intermediateService.downloadIconFullQuality(id)

    @ResponseBody
    @PostMapping(path = [DOWNLOAD_ICON_PREVIEW])
    fun downloadIconPreview(@RequestBody id: Long): ResponseEntity<InputStreamResource> =
        intermediateService.downloadIconPreview(id)

    @ResponseBody
    @PostMapping(path = ["$UPLOAD_ICON/{contactId}"])
    fun uploadIcon(
        @RequestPart("file") file: MultipartFile,
        @PathVariable contactId: Long
    ): ResponseEntity<Long> {
        val image = fileToImage(file) ?: return ResponseEntity.badRequest().body(null)
        return intermediateService.addIconToContact(contactId, image)
    }

}