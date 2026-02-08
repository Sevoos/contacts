package net.sevoos.contacts.contact.service

import net.sevoos.contacts.contact.entity.IconEntity
import net.sevoos.contacts.contact.repository.IconRepository
import net.sevoos.contacts.iconhandling.api.IconHandlingPrivateApiService
import net.sevoos.contacts.shared.iconhandling.IconHandlingApiSpringConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class IconService(
    iconHandlingApiSpringConfig: IconHandlingApiSpringConfig
) {

    private val constructor = IconEntity::class.java.getConstructor()

    private val iconHandlingPrivateApiService = IconHandlingPrivateApiService(iconHandlingApiSpringConfig)
    @Autowired
    private lateinit var repository: IconRepository

    fun checkIconExistence(id: Long): Boolean = repository.findById(id).isPresent

    fun createEntity(contactId: Long): Long {
        val entity = constructor.newInstance()
        entity.contactId = contactId
        repository.save(entity)
        return entity.id
    }

    fun deleteIconEntity(entity: IconEntity) {
        repository.delete(entity)
        iconHandlingPrivateApiService.deleteIcon(entity.id)
    }

}