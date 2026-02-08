package net.sevoos.contacts.contact.repository

import net.sevoos.contacts.contact.entity.BirthdayDateEntity
import org.springframework.data.jpa.repository.JpaRepository

interface BirthdayDateRepository: JpaRepository<BirthdayDateEntity, Long>