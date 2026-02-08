package net.sevoos.utilities

import org.springframework.web.multipart.MultipartFile
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageTypeSpecifier
import javax.imageio.ImageWriteParam
import javax.imageio.metadata.IIOMetadataNode
import javax.imageio.stream.FileImageOutputStream


fun fileToImage(file: MultipartFile): BufferedImage? {
    try {
        val image = ImageIO.read(file.inputStream)
        requireNotNull(image)
        return image
    } catch (_: Exception) {
        return null
    }
}

fun toCenteredSquare(image: BufferedImage): BufferedImage {
    val width = image.width
    val height = image.height

    if (width == height) return image

    val size = minOf(width, height)
    val x = (width - size) / 2
    val y = (height - size) / 2

    return image.getSubimage(x, y, size, size)
}

fun resize(originalImage: BufferedImage, targetWidth: Int, targetHeight: Int): BufferedImage {
    val scaled = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH)
    val resized = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB) //TYPE_INT_ARGB

    val g2d = resized.createGraphics()
    g2d.drawImage(scaled, 0, 0, null)
    g2d.dispose()

    return resized
}
