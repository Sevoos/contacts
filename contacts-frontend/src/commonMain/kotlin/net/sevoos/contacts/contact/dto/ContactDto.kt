@file:Suppress("unused")

package net.sevoos.contacts.contact.dto

import kotlinx.serialization.Serializable
import net.sevoos.contacts.birthdaydate.dto.BirthdayDateDto
import net.sevoos.contacts.communicationchannel.dto.CommunicationChannelDto
import kotlin.js.JsExport

@JsExport
@Serializable
data class ContactDto(
    var id: Long,
    var birthdayDate: BirthdayDateDto?,
    var category: List<String>,
    var communicationChannels: List<CommunicationChannelDto>,
    var creationDate: Long,
    var defaultIconId: Long?,
    var displayName: String?,
    var firstName: String,
    var icons: List<Long>,
    var lastName: String,
    var modificationDate: Long,
    var patronymicName: String?,
    var timezone: String,
//    var testProperty: IconDto,
//    var wth: Int
)
