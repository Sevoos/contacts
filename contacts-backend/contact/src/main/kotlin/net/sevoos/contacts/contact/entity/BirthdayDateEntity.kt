package net.sevoos.contacts.contact.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class BirthdayDateEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    var contactId: Long,

    var day: Int,

    var month: Int,

    var year: Int?,

)