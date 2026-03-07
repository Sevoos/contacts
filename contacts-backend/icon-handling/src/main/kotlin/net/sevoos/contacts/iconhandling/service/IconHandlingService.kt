package net.sevoos.contacts.iconhandling.service

import jakarta.annotation.PostConstruct
import net.sevoos.contacts.icon.api.IconPrivateApiService
import net.sevoos.contacts.iconhandling.config.IconHandlingConfig
import net.sevoos.contacts.contact.ContactApiSpringConfig
import net.sevoos.utilities.resize
import net.sevoos.utilities.toCenteredSquare
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.File
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

    fun getFullQualityPath(id: Long): Path = iconsDirectory.resolve("$id.png")

    fun getPreviewPath(id: Long): Path = iconsDirectory.resolve("$id-preview.jpg")

}