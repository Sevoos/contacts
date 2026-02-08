package net.sevoos.contacts.contact.entity

import jakarta.persistence.*
import net.sevoos.contacts.communicationchannel.enums.EnumCommunicationChannel

@Entity
class CommunicationChannelEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    var comment: String?,

    var contactId: Long,

    @Enumerated(EnumType.STRING)
    var type: EnumCommunicationChannel,

    var value: String

)