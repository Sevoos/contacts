package net.sevoos.contacts.communicationchannel.dto

import kotlinx.serialization.Serializable
import net.sevoos.contacts.communicationchannel.enums.EnumCommunicationChannel
import kotlin.js.JsExport

@JsExport
@Serializable
data class CommunicationChannelCreationDto(
    val comment: String?,
    val type: EnumCommunicationChannel,
    val contactId: Long,
    val value: String,
)
