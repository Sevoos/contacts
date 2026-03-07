package net.sevoos.rest

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

class RestUtility(
    host: String,
    api: String,
    json: Json? = null
) {

    private val hostApi = host + api
    val httpClient = newHttpClient(json)

    inline fun<reified Q> getAttachBodyBlock(body: Q): HttpRequestBuilder.() -> Unit = {
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    fun getUrl(mapping: String, vararg uriVariables: Any): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("$hostApi/$mapping")
        uriVariables.forEach {
            stringBuilder.append("/$it")
        }
        return stringBuilder.toString()
    }

    suspend inline fun <reified T> getForEntity(
        mapping: String,
        vararg uriVariables: Any
    ): ResponseEntity<T> = httpClient.get(getUrl(mapping, *uriVariables)).toResponseEntity()

    suspend inline fun <reified T> getForObject( //<reified T : Any>
        mapping: String,
        vararg uriVariables: Any
    ): T = getForEntity<T>(mapping, *uriVariables).body!!

    suspend inline fun <reified T, reified Q> patchForEntity(
        mapping: String,
        body: Q,
        vararg uriVariables: Any
    ): ResponseEntity<T> =
        httpClient.patch(getUrl(mapping, *uriVariables), getAttachBodyBlock(body)).toResponseEntity()

    suspend inline fun <reified T, reified Q> patchForObject(
        mapping: String,
        body: Q,
        vararg uriVariables: Any
    ): T = patchForEntity<T, Q>(mapping, body, *uriVariables).body!!

    suspend inline fun <reified T, reified Q> postForEntity(
        mapping: String,
        body: Q,
        vararg uriVariables: Any
    ): ResponseEntity<T> =
        httpClient.post(getUrl(mapping, *uriVariables), getAttachBodyBlock(body)).toResponseEntity()

    suspend inline fun <reified T> postForEntityLongBody(
        mapping: String,
        body: Long,
        vararg uriVariables: Any
    ): ResponseEntity<T> =
        httpClient.post(getUrl(mapping, *uriVariables), getAttachBodyBlock(body)).toResponseEntity()


    suspend inline fun <reified T, reified Q> postForObject(
        mapping: String,
        body: Q,
        vararg uriVariables: Any
    ): T = postForEntity<T, Q>(mapping, body, *uriVariables).body!!

    suspend inline fun <reified T> uploadFile(
        mapping: String,
        fileName: String,
        fileBytes: ByteArray,
        vararg uriVariables: Any
    ): ResponseEntity<T> {
        val response = httpClient.submitFormWithBinaryData(
            url = getUrl(mapping, *uriVariables),
            formData = formData {
                // File part
                append(
                    "file",
                    fileBytes,
                    Headers.build {
                        append(HttpHeaders.ContentType, "application/octet-stream")
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    }
                )
            }
        )
        return response.toResponseEntity()
    }

}

val patchJson = Json {
    serializersModule = SerializersModule {
        contextual(PatchField::class) { typeArguments ->
            @Suppress("UNCHECKED_CAST")
            PatchFieldSerializer(
                typeArguments[0] as KSerializer<Any>
            )
        }
    }
}

internal fun newHttpClient(json: Json?): HttpClient =
    HttpClient(getEngineFactory()) {
        expectSuccess = true
        install(ContentNegotiation) {
//            json(
//                Json {
//                    ignoreUnknownKeys = true
//                    explicitNulls = false
//                }
//            )
            if (json == null) json() else json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis  = 30_000
        }
//        install(plugin = Logging) {
//            level = LogLevel.ALL
//            logger = Logger.SIMPLE
//        }
    }

suspend inline fun <reified T> HttpResponse.toResponseEntity(): ResponseEntity<T> =
    ResponseEntity(status.value, body<T?>())