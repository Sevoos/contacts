package net.sevoos.contacts.contact.dto

import net.sevoos.contacts.communicationchannel.enums.EnumCommunicationChannel
import org.openapitools.jackson.nullable.JsonNullable

data class CommunicationChannelPatchDto(
    val comment: JsonNullable<String?> = JsonNullable.undefined(),
    val contactId: JsonNullable<Long> = JsonNullable.undefined(),
    val type: JsonNullable<EnumCommunicationChannel> = JsonNullable.undefined(),
    val value: JsonNullable<String> = JsonNullable.undefined()
)
