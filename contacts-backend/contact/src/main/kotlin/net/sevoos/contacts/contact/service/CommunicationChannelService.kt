package net.sevoos.contacts.contact.service

import net.sevoos.contacts.communicationchannel.dto.CommunicationChannelCreationDto
import net.sevoos.contacts.communicationchannel.dto.CommunicationChannelDto
import net.sevoos.contacts.contact.dto.CommunicationChannelPatchDto
import net.sevoos.contacts.contact.entity.CommunicationChannelEntity
import net.sevoos.contacts.contact.repository.CommunicationChannelRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommunicationChannelService {

    private val constructor = CommunicationChannelEntity::class.java.getConstructor()

    @Autowired
    private lateinit var repository: CommunicationChannelRepository

    fun communicationChannelExists(id: Long): Boolean =
        repository.existsById(id)

    fun createCommunicationChannel(dto: CommunicationChannelCreationDto): Long {
        val entity = constructor.newInstance()
        entity.comment = dto.comment
        entity.type = dto.type
        entity.contactId = dto.contactId
        entity.value = dto.value
        repository.save(entity)
        return entity.id
    }

    fun deleteCommunicationChannel(entity: CommunicationChannelEntity) {
        repository.delete(entity)
    }

    fun entityToDto(entity: CommunicationChannelEntity): CommunicationChannelDto =
        CommunicationChannelDto(
            entity.id,
            entity.comment,
//            entity.contactId,
            entity.type,
            entity.value
        )

    fun getCommunicationChannel(id: Long): CommunicationChannelDto =
        entityToDto(repository.getReferenceById(id))

    @Transactional
    fun patchCommunicationChannel(
        entity: CommunicationChannelEntity,
        patchDto: CommunicationChannelPatchDto
    ): CommunicationChannelDto? {
        val savedDto = entityToDto(entity)

        if (patchDto.comment.isPresent) {
            entity.comment = patchDto.comment.get()
        }
        if (patchDto.contactId.isPresent) {
            entity.contactId = patchDto.contactId.get()
        }
        if (patchDto.type.isPresent) {
            entity.type = patchDto.type.get()
        }
        if (patchDto.value.isPresent) {
            entity.value = patchDto.value.get()
        }

        val newDto = entityToDto(entity)
        return if (savedDto.toString() == newDto.toString()) null else newDto
    }

}