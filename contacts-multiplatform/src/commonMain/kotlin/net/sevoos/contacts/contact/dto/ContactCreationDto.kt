@file:Suppress("unused")

package net.sevoos.contacts.contact.dto

import kotlinx.serialization.Serializable
import net.sevoos.contacts.birthdaydate.dto.BirthdayDateDto
import net.sevoos.contacts.communicationchannel.dto.CommunicationChannelContactCreationDto
import kotlin.js.JsExport

@JsExport
@Serializable
class ContactCreationDto (
    var birthdayDate: BirthdayDateDto?,
    var category: Array<String>,
    var communicationChannels: Array<CommunicationChannelContactCreationDto>,
    var displayName: String?,
    var firstName: String,
    var lastName: String,
    var patronymicName: String?,
    var timezone: String,
)