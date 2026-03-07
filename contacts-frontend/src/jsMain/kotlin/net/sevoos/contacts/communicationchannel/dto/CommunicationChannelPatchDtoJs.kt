package net.sevoos.contacts.communicationchannel.dto

import kotlin.js.JsExport
import net.sevoos.contacts.communicationchannel.enums.EnumCommunicationChannel
import net.sevoos.rest.PatchField

@JsExport
class CommunicationChannelPatchDtoJs {

    internal var comment: PatchField<String> = PatchField.Undefined
    internal var type: PatchField<EnumCommunicationChannel> = PatchField.Undefined
    internal var value: PatchField<String> = PatchField.Undefined

    fun setComment(comment: String?) {
        this.comment = PatchField.Present(comment)
    }

    fun setType(type: EnumCommunicationChannel) {
        this.type = PatchField.Present(type)
    }

    fun setValue(value: String) {
        this.value = PatchField.Present(value)
    }
}