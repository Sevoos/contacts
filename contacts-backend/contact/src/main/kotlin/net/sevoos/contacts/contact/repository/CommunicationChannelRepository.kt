package net.sevoos.contacts.contact.repository

import net.sevoos.contacts.contact.entity.CommunicationChannelEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommunicationChannelRepository: JpaRepository<CommunicationChannelEntity, Long>