package net.sevoos.contacts.contact.entity

import jakarta.persistence.*

@Entity
@Table
class IconEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    var contactId: Long,

)
