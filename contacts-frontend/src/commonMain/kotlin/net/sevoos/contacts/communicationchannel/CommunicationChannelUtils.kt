package net.sevoos.contacts.communicationchannel

import net.sevoos.contacts.communicationchannel.dto.CommunicationChannelDto
import net.sevoos.contacts.communicationchannel.enums.EnumCommunicationChannel
import kotlin.js.JsExport
import net.sevoos.contacts.communicationchannel.enums.EnumCommunicationChannel.Telegram
import net.sevoos.contacts.communicationchannel.enums.EnumCommunicationChannel.Instagram
import net.sevoos.contacts.communicationchannel.enums.EnumCommunicationChannel.WhatsApp
import net.sevoos.contacts.communicationchannel.enums.EnumCommunicationChannel.Matrix
import net.sevoos.contacts.communicationchannel.enums.EnumCommunicationChannel.Discord
import net.sevoos.contacts.communicationchannel.enums.EnumCommunicationChannel.TikTok
import net.sevoos.contacts.communicationchannel.enums.EnumCommunicationChannel.Email
import net.sevoos.contacts.communicationchannel.enums.EnumCommunicationChannel.LinkedIn

val typesWithAtCharacter = HashSet(listOf(Matrix, Discord, TikTok))

@JsExport
fun removeAtIfNeeded(value: String): String {
    if (value.startsWith('@')) {
        return value.substring(1)
    }
    return value
}

@JsExport
fun sortCommunicationChannels(
    communicationChannels: Array<CommunicationChannelDto>
) = communicationChannels.sortWith { a, b ->
    val compareOrdinal = a.type.compareTo(b.type)
    if (compareOrdinal != 0) {
        compareOrdinal
    } else {
        val commentA = a.comment
        val commentB = b.comment
        val falsyA = commentA == null || commentA == ""
        val falsyB = commentB == null || commentB == ""
        if (falsyA && falsyB) {
            0
        } else if (!falsyA && !falsyB) {
            commentA.compareTo(commentB)
        } else if (falsyA) {
            -1
        } else {
            1
        }
    }
}

fun getCommunicationChannelLink(type: EnumCommunicationChannel, value: String): String {
    if (type == Email) {
        return "mailto:$value"
    }
    return "https://" +
        when (type) {
            Telegram -> "t.me"
            Instagram -> "instagram.com"
            WhatsApp -> "wa.me"
            Matrix -> "matrix.to/#"
            Discord -> "discord.com/channels"
            TikTok -> "tiktok.com"
            LinkedIn -> "linkedin.com/in"
            Email -> throw IllegalArgumentException()
        } +
        "/" +
        if (typesWithAtCharacter.contains(type)) {
            "@"
        } else {
            ""
        } +
        if (type == Discord) {
            "me"
        } else {
            value
        }

}

@JsExport
fun getCommunicationChannelLink(
    type: String,
    value: String
): String = getCommunicationChannelLink(EnumCommunicationChannel.valueOf(type), value)