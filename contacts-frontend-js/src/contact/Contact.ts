import {
  BirthdayDateDto, ContactDto, sortCommunicationChannels,
} from "@contacts/frontend-api";
import {
  deleteContactIfSaved,
  getContactsWithIdenticalNameFromAll,
  getNameToActuallyDisplay, saveContact
} from "@/utils/ContactUtils.ts";
import {type CommunicationChannel, communicationChannelFromDto} from "@/contact/CommunicationChannel.ts";
import {myStore} from "@/store";

export class Contact {
  constructor(
    public id: bigint,
    public birthdayDate: BirthdayDateDto | null,
    public category: string[],
    public communicationChannels: CommunicationChannel[],
    public creationDate: bigint,
    public defaultIconId: bigint | null,
    public displayName: string | null,
    public firstName: string,
    public icons: bigint[],
    public lastName: string,
    public modificationDate: bigint,
    public nameToActuallyDisplay: string,
    public patronymicName: string | null,
    public timezone: string
  ) {

  }
}

export function saveContactFromDto(dto: ContactDto) {
  const store = myStore()
  const existingContactsWithIdenticalName = getContactsWithIdenticalNameFromAll(dto)
  const communicationChannels = dto.communicationChannels.asJsReadonlyArrayView().slice()
  sortCommunicationChannels(communicationChannels)
  const contact = new Contact(
    dto.id,
    dto.birthdayDate ?? null,
    dto.category.asJsReadonlyArrayView().slice(),
    communicationChannels.map(
      communicationChannelDto => communicationChannelFromDto(communicationChannelDto)
    ),
    dto.creationDate,
    dto.defaultIconId ?? null,
    dto.displayName ?? null,
    dto.firstName,
    dto.icons.asJsReadonlyArrayView().slice(),
    dto.lastName,
    dto.modificationDate,
    getNameToActuallyDisplay(
      existingContactsWithIdenticalName,
      dto
    ),
    dto.patronymicName ?? null,
    dto.timezone
  )
  deleteContactIfSaved(dto.id)
  saveContact(contact)

  if (existingContactsWithIdenticalName.length === 1) {
    const previousContact = existingContactsWithIdenticalName[0]!
    previousContact.nameToActuallyDisplay = getNameToActuallyDisplay(
      [contact],
      previousContact
    )
    saveContact(previousContact)
    const index = store.contacts.findIndex(contact => contact.id === previousContact.id)!
    store.contacts[index] = previousContact
  }
}
