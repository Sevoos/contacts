package net.sevoos.contacts.contact.repository

import net.sevoos.contacts.contact.entity.ContactEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ContactRepository: JpaRepository<ContactEntity, Long> {

}