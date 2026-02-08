package net.sevoos.contacts.iconhandling.api

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.toByteArray
import net.sevoos.generation.annotations.AsynchronousService
import net.sevoos.rest.ResponseEntity
import net.sevoos.rest.RestUtility

@AsynchronousService
class IconHandlingPublicApiAsynchronousService(config: IconHandlingApiConfig) {

    private val restUtility = RestUtility(config.hostIconHandling, config.apiIconHandling)

    suspend fun downloadIconFullQuality(id: Long): ResponseEntity<ByteArray> {
        val response = restUtility.postForEntityLongBody<ByteReadChannel>(DOWNLOAD_ICON_FULL_QUALITY, id)
        return ResponseEntity(
            response.status,
            response.body?.toByteArray()
        )
    }

    suspend fun downloadIconPreview(id: Long): ResponseEntity<ByteArray> {
        val response = restUtility.postForEntityLongBody<ByteReadChannel>(DOWNLOAD_ICON_PREVIEW, id)
        return ResponseEntity(
            response.status,
            response.body?.toByteArray()
        )
    }

    suspend fun uploadIcon(contactId: Long, fileName: String, fileBytes: ByteArray): ResponseEntity<Long> =
        restUtility.uploadFileStringMeta(UPLOAD_ICON, fileName, fileBytes, contactId)

}