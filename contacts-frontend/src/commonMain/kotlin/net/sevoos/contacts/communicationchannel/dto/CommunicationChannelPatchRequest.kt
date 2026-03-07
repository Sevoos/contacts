package net.sevoos.contacts.communicationchannel.dto

import net.sevoos.contacts.communicationchannel.enums.EnumCommunicationChannel
import net.sevoos.rest.PatchField

data class CommunicationChannelPatchRequest(
    val comment: PatchField<String> = PatchField.Undefined,
    val type: PatchField<EnumCommunicationChannel> = PatchField.Undefined,
    val value: PatchField<String> = PatchField.Undefined
)
