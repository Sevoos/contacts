package net.sevoos.contacts.contact.service

import jakarta.annotation.PostConstruct
import net.sevoos.contacts.birthdaydate.dto.BirthdayDateDto
import net.sevoos.contacts.communicationchannel.dto.CommunicationChannelContactCreationDto
import net.sevoos.contacts.communicationchannel.dto.CommunicationChannelCreationDto
import net.sevoos.contacts.contact.dto.ContactCreationDto
import net.sevoos.contacts.contact.dto.ContactDto
import net.sevoos.contacts.contact.dto.ContactPatchDto
import net.sevoos.contacts.contact.entity.CommunicationChannelEntity
import net.sevoos.contacts.contact.entity.ContactEntity
import net.sevoos.contacts.contact.repository.ContactRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class ContactService {

    @Autowired
    private lateinit var birthdayDateService: BirthdayDateService
    @Autowired
    private lateinit var communicationChannelService: CommunicationChannelService
    private val constructor = ContactEntity::class.java.getConstructor()
//    private var defaultTimezone = TimeZone.currentSystemDefault()
    @Autowired
    private lateinit var iconService: IconService
    @Autowired
    private lateinit var repository: ContactRepository

    @PostConstruct
    @Suppress("unused")
    private fun postConstruct() {
//        contactDefaultSettingsConfig.defaultTimezone?.let {
//            try {
//                defaultTimezone = TimeZone.of(it)
//                println(defaultTimezone)
//            } catch (_: IllegalTimeZoneException) {
//            }
//        }
    }

//    private fun contactsEqual(a: ContactEntity, b: ContactEntity) = entityToDto(a) == entityToDto(b)

    private fun getSystemTimeMillis(): Long = System.currentTimeMillis()

    fun contactExists(id: Long): Boolean = repository.findById(id).isPresent

    fun deleteCommunicationChannelWithoutUpdatingContact(entity: CommunicationChannelEntity) {
        communicationChannelService.deleteCommunicationChannel(entity)
    }

    fun deleteContact(id: Long) {
        val entity = repository.findById(id).get()
        entity.birthdayDate?.let {
            birthdayDateService.deleteBirthdayDate(it)
        }
        entity.communicationChannels.forEach {
            deleteCommunicationChannelWithoutUpdatingContact(it)
        }
        entity.icons.forEach {
            iconService.deleteIconEntity(it)
        }
        repository.delete(entity)
    }

    fun entityToDto(entity: ContactEntity): ContactDto {
        val birthdayDate = entity.birthdayDate
        return ContactDto(
            entity.id,
            if (birthdayDate == null) {
                null
            } else {
                birthdayDateService.entityToDto(birthdayDate)
            },
            entity.category,
            entity.communicationChannels.map { communicationChannelService.entityToDto(it) },
            entity.creationDate,
            entity.defaultIconId,
            entity.displayName,
            entity.firstName,
            entity.icons.map { it.id },
            entity.lastName,
            entity.modificationDate,
            entity.patronymicName,
            entity.timezone
        )
    }

    fun getAllContactsIds(): List<Long> =
        repository.findAll().map { it.id }

    @Transactional
    fun patchContact(entity: ContactEntity, patchDto: ContactPatchDto): ContactDto? {
        val savedDto = entityToDto(entity)

        if (patchDto.birthdayDate.isPresent) {
            val birthdayDateDto = patchDto.birthdayDate.get()
            val savedBirthdayDate = entity.birthdayDate
            val savedBirthdayDateDto = birthdayDateService.nullableEntityToDto(savedBirthdayDate)
            if (savedBirthdayDateDto != birthdayDateDto) {
                if (savedBirthdayDate != null) {
                    birthdayDateService.deleteBirthdayDate(savedBirthdayDate)
                }
                val newBirthdayDate = if (birthdayDateDto == null)
                    null
                else
                    birthdayDateService.saveDtoToEntity(entity.id, birthdayDateDto)
                entity.birthdayDate = newBirthdayDate
            }
        }
        if (patchDto.category.isPresent) {
            entity.category = patchDto.category.get()
        }
        if (patchDto.defaultIconId.isPresent) {
            entity.defaultIconId = patchDto.defaultIconId.get()
        }
        if (patchDto.displayName.isPresent) {
            entity.displayName = patchDto.displayName.get()
        }
        if (patchDto.firstName.isPresent) {
            entity.firstName = patchDto.firstName.get()
        }
        if (patchDto.lastName.isPresent) {
            entity.lastName = patchDto.lastName.get()
        }
        if (patchDto.patronymicName.isPresent) {
            entity.patronymicName = patchDto.patronymicName.get()
        }
        if (patchDto.timezone.isPresent) {
            entity.timezone = patchDto.timezone.get()
        }

        val newDto = entityToDto(entity)
        if (savedDto.toString() == newDto.toString()) {
            return null
        }
        entity.modificationDate = getSystemTimeMillis()
        return newDto
    }

    @Transactional
    fun finishContactCreation(
        entity: ContactEntity,
        birthdayDate: BirthdayDateDto?,
        communicationChannels: Array<CommunicationChannelContactCreationDto>
    ) {
        val contactId = entity.id
        birthdayDate?.let {
            entity.birthdayDate = birthdayDateService.saveDtoToEntity(contactId, it)
        }
        communicationChannels.forEach {
            communicationChannelService.createCommunicationChannel(
                CommunicationChannelCreationDto(
                    it.comment,
                    it.type,
                    contactId,
                    it.value
                )
            )
        }
    }

    fun saveDtoToEntity(dto: ContactCreationDto): Long {
        val entity = constructor.newInstance()
        entity.category = dto.category.toList()
        entity.displayName = dto.displayName
        entity.firstName = dto.firstName
        entity.lastName = dto.lastName
        entity.patronymicName = dto.patronymicName
        val time = getSystemTimeMillis()
        entity.creationDate = time
        entity.modificationDate = time
        entity.timezone = dto.timezone
        repository.save(entity)
        finishContactCreation(entity, dto.birthdayDate, dto.communicationChannels)
        return entity.id
    }

    @Transactional
    fun setDefaultIcon(contactId: Long, iconId: Long) {
        val contact = repository.findById(contactId).get()
        contact.defaultIconId = iconId
    }

    @Transactional
    fun updateModificationDate(id: Long) {
        val entity = repository.findById(id).get()
        entity.modificationDate = getSystemTimeMillis()
    }

}