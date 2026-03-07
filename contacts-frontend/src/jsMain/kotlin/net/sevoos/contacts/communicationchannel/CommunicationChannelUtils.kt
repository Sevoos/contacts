package net.sevoos.contacts.communicationchannel

import net.sevoos.contacts.communicationchannel.api.CommunicationChannelPublicApiFrontendService
import net.sevoos.contacts.communicationchannel.dto.CommunicationChannelPatchDtoJs
import net.sevoos.contacts.communicationchannel.dto.CommunicationChannelPatchRequest

private fun CommunicationChannelPatchDtoJs.toRequest(): CommunicationChannelPatchRequest =
    CommunicationChannelPatchRequest(
        comment,
        type,
        value
    )


@JsExport
fun patchCommunicationChannel(
    id: Long,
    patchDto: CommunicationChannelPatchDtoJs,
    apiService: CommunicationChannelPublicApiFrontendService
) = apiService.patchCommunicationChannel(id, patchDto.toRequest())