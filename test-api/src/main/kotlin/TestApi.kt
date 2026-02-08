import net.sevoos.contacts.communicationchannel.enums.EnumCommunicationChannel.*
import net.sevoos.contacts.contact.api.ContactApiConfig
import net.sevoos.contacts.contact.api.ContactPublicApiService
import net.sevoos.contacts.contact.dto.ContactPatchDtoClient
import net.sevoos.contacts.icon.api.IconPrivateApiService
import net.sevoos.contacts.icon.api.IconPublicApiService
import net.sevoos.contacts.iconhandling.api.IconHandlingApiConfig
import net.sevoos.contacts.iconhandling.api.IconHandlingPrivateApiService
import net.sevoos.contacts.iconhandling.api.IconHandlingPublicApiService
import org.openapitools.jackson.nullable.JsonNullable

//val contactApiConfig = ContactApiConfig("http://localhost:8000", "/api/contact/v1")
//val iconHandlingApiConfig = IconHandlingApiConfig("http://localhost:8000", "/api/icon-handling/v1")

val contactApiConfig = ContactApiConfig("http://localhost:8091", "/api/v1")
val iconHandlingApiConfig = IconHandlingApiConfig("http://localhost:8092", "/api/v1")

val contactPublicApiService = ContactPublicApiService(contactApiConfig)
val iconHandlingPrivateApiService = IconHandlingPrivateApiService(iconHandlingApiConfig)
val iconHandlingPublicApiService = IconHandlingPublicApiService(iconHandlingApiConfig)
val iconPrivateApiService = IconPrivateApiService(contactApiConfig)
val iconPublicApiService = IconPublicApiService(contactApiConfig)

fun main() {

    println(contactPublicApiService.patchContact(14, ContactPatchDtoClient(
        patronymicName = null,
        category = listOf()
    )).body)
//    println(contactPublicApiService.createContact(contactCreationDto))

//    println(contactPublicApiService.getContact(3).body)

//    println(contactPublicApiService.setDefaultIcon(SetDefaultIconDto(3, 5)))

//    iconPublicApiService.deleteIconEntity(11)

//    val name = "session_a.png"
//    val test = Files.readAllBytes(Path(name))
//    println(iconHandlingPublicApiService.uploadIcon(1, name, test))

//    contactPublicApiService.deleteContact(2)

//    val bytes = iconHandlingPublicApiService.downloadIconPreview(5).body!!
//    File("nikita_preview.jpg").outputStream().write(bytes)

//    val timezone = TimeZone.of("Asia/Almaty")
//    println(Clock.System.now().toLocalDateTime(timezone))
}

//val contactCreationDto = ContactCreationDto(
//    BirthdayDateDto(1988, 2, 14),
//    listOf("Family", "Paternal line"),
//    listOf(
//        CommunicationChannelContactCreationDto(
//            null,
//            Telegram,
//            "+79250491234"
//        ),
//        CommunicationChannelContactCreationDto(
//            null,
//            Email,
//            "andreymaslov@gmail.com"
//        )
//    ),
//    "Папа",
//    "Андрей",
//    "Маслов",
//    "Андреевич",
//    "Asia/Almaty"
//)