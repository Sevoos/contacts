package net.sevoos.contacts.iconhandling.service

import jakarta.annotation.PostConstruct
import net.sevoos.contacts.icon.api.IconPrivateApiService
import net.sevoos.contacts.iconhandling.config.IconHandlingConfig
import net.sevoos.contacts.contact.ContactApiSpringConfig
import net.sevoos.utilities.resize
import net.sevoos.utilities.toCenteredSquare
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists


@Service
class IconHandlingService(
    val config: IconHandlingConfig,
    contactApiSpringConfig: ContactApiSpringConfig
) {

    private val iconApiService = IconPrivateApiService(contactApiSpringConfig)

    lateinit var iconsDirectory: Path

    private fun constructResponseWithInputStreamResource(file: File): ResponseEntity<InputStreamResource> =
        ResponseEntity.ok()
            .contentLength(file.length())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(InputStreamResource(FileInputStream(file)))

    @PostConstruct
    @Suppress("unused")
    private fun postConstruct() {
        iconsDirectory = File(config.iconsDirectory).toPath()
        if (!iconsDirectory.exists()) {
            iconsDirectory.createDirectories()
        }
    }

    fun addIconToContact(contactId: Long, image: BufferedImage): Long {
        val squareImage = toCenteredSquare(image)
        val size = 45
        val resizedImage = resize(squareImage, size, size)
        val iconId = iconApiService.createIconEntity(contactId)
        ImageIO.write(
            squareImage,
            "png",
            getFullQualityPath(iconId).toFile()
        )
        ImageIO.write(
            resizedImage,
            "jpg",
            getPreviewPath(iconId).toFile()
        )
        return iconId
    }

    fun deleteIcon(iconId: Long) {
        getFullQualityPath(iconId).deleteIfExists()
        getPreviewPath(iconId).deleteIfExists()
    }

    fun downloadIcon(path: Path): ResponseEntity<InputStreamResource> {
        val file = path.toFile()
        if (!file.exists()) {
            println("File not found: ${file.absolutePath}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
        return constructResponseWithInputStreamResource(file)
    }

    fun getFullQualityPath(id: Long): Path = iconsDirectory.resolve("$id.png")

    fun getPreviewPath(id: Long): Path = iconsDirectory.resolve("$id-preview.jpg")

}