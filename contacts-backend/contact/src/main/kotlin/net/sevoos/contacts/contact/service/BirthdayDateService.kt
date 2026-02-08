package net.sevoos.contacts.contact.service

import net.sevoos.contacts.birthdaydate.dto.BirthdayDateDto
import net.sevoos.contacts.contact.entity.BirthdayDateEntity
import net.sevoos.contacts.contact.repository.BirthdayDateRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class BirthdayDateService {

    private val constructor = BirthdayDateEntity::class.java.getConstructor()
    @Autowired
    private lateinit var repository: BirthdayDateRepository

    fun deleteBirthdayDate(entity: BirthdayDateEntity) {
        repository.delete(entity)
    }

    fun entityToDto(entity: BirthdayDateEntity): BirthdayDateDto = BirthdayDateDto(
        entity.year,
        entity.month,
        entity.day
    )

    fun nullableEntityToDto(nullableEntity: BirthdayDateEntity?): BirthdayDateDto? =
        if (nullableEntity == null) null else entityToDto(nullableEntity)

    fun saveDtoToEntity(contactId: Long, dto: BirthdayDateDto): BirthdayDateEntity {
        val entity = constructor.newInstance()
        entity.contactId = contactId
        entity.year = dto.year
        entity.month = dto.month
        entity.day = dto.day
        return repository.save(entity)
    }

}