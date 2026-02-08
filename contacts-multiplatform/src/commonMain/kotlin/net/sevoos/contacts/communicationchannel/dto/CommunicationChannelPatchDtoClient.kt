package net.sevoos.contacts.communicationchannel.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import net.sevoos.contacts.communicationchannel.enums.EnumCommunicationChannel
import kotlin.js.JsExport

@JsExport
@Serializable
data class CommunicationChannelPatchDtoClient(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val comment: String? = null,
    val contactId: Long? = null,
    val type: EnumCommunicationChannel? = null,
    val value: String? = null
)
