package net.sevoos.contacts.communicationchannel.dto

import kotlinx.serialization.Serializable
import net.sevoos.contacts.communicationchannel.enums.EnumCommunicationChannel
import kotlin.js.JsExport

@JsExport
@Serializable
data class CommunicationChannelDto(
    val id: Long,
    val comment: String?,
//    val contactId: Long,
    val type: EnumCommunicationChannel,
    val value: String
)
