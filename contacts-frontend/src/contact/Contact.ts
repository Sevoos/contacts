import {
  BirthdayDateDto, ContactDto, sortCommunicationChannels,
} from "@/lib";
import {
  deleteContactIfSaved,
  findAllContacts,
  getNameToActuallyDisplay, saveContact
} from "@/utils/ContactUtils.ts";
import {type CommunicationChannel, communicationChannelFromDto} from "@/contact/CommunicationChannel.ts";

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
  const existingContactsWithIdenticalName = findAllContacts().filter(contact => {
    return contact.id !== dto.id && contact.firstName === dto.firstName
  })
  const displayName = dto.displayName ?? null
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
    displayName,
    dto.firstName,
    dto.icons.asJsReadonlyArrayView().slice(),
    dto.lastName,
    dto.modificationDate,
    getNameToActuallyDisplay(
      existingContactsWithIdenticalName,
      displayName,
      dto.firstName,
      dto.lastName
    ),
    dto.patronymicName ?? null,
    dto.timezone
  )
  deleteContactIfSaved(dto.id)
  saveContact(contact)

  if (existingContactsWithIdenticalName.length == 1) {
    const previousContact = existingContactsWithIdenticalName[0]!
    previousContact.nameToActuallyDisplay = getNameToActuallyDisplay(
      [contact],
      previousContact.displayName,
      previousContact.firstName,
      previousContact.lastName
    )
    saveContact(previousContact)
  }
}

export function doContactsEqual(a: Contact, b: Contact): boolean {
  return JSON.stringify(a) === JSON.stringify(b)
}
