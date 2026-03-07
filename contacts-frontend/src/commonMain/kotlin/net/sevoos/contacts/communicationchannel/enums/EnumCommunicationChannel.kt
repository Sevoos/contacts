package net.sevoos.contacts.communicationchannel.enums

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
enum class EnumCommunicationChannel {
    Telegram,
    Instagram,
    WhatsApp,
    Matrix,
    Discord,
    TikTok,
    Email,
    LinkedIn,
}