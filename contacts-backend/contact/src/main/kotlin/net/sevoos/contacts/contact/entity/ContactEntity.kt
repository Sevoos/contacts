package net.sevoos.contacts.contact.entity

import jakarta.persistence.*

@Entity
class ContactEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "birthday_date_id", referencedColumnName = "id", unique = true)
    var birthdayDate: BirthdayDateEntity?,

    @ElementCollection
    @Column
    var category: List<String>,

    @OneToMany(mappedBy = "contactId", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var communicationChannels: MutableList<CommunicationChannelEntity>,

    var creationDate: Long,

    var defaultIconId: Long?,

    var displayName: String?,

    var firstName: String,

    @OneToMany(mappedBy = "contactId", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var icons: MutableList<IconEntity>,

    var lastName: String,

    var modificationDate: Long,

    var patronymicName: String?,

    var timezone: String,

)
