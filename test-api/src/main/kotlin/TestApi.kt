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

}