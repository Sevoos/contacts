import {ContactLocalStore} from "@/utils/ContactLocalStore.ts";
import {type Contact} from "@/contact/Contact.ts";
import {BirthdayDateDto, type Nullable} from "@contacts/frontend-api";
import {myStore} from "@/store";
import {stringifyBigint} from "@/utils/Utils.ts";

export const emojiBirthdayNow = '🎂'
export const emojiBirthdaySoon = '❗'

export function findAllContactsIds(): bigint[] {
  const ids: bigint[] = []
  const prefix = "contact-"
  for (const localStorageKey in localStorage) {
    if (localStorageKey.startsWith(prefix)) {
      ids.push(BigInt(localStorageKey.substring(prefix.length)))
    }
  }
  return ids
}

export function findAllContacts(): Contact[] {
  console.log("Found")
  return findAllContactsIds().map(id => findContactById(id)!)
}

export function findContactById(id: bigint): Contact | null {
  return new ContactLocalStore(id).get()
}

export function deleteContactIfSaved(id: bigint) {
  new ContactLocalStore(id).clear()
}

export function saveContact(value: Contact) {
  new ContactLocalStore(value.id).set(value)
}

type ContactIdDisplayFirstLastName = {
  id: bigint
  displayName: Nullable<string>
  firstName: string
  lastName: string
}

export function getContactsWithIdenticalNameFromAll(value: ContactIdDisplayFirstLastName): Contact[] {
  return getContactsWithIdenticalName(myStore().contacts, value)
}

export function getContactsWithIdenticalName(
  contacts: Contact[],
  value: ContactIdDisplayFirstLastName
): Contact[] {
  return contacts.filter(contact =>
    contact.id !== value.id
    && (contact.displayName ?? contact.firstName) === (value.displayName ?? value.firstName)
  )
}

export function getNameToActuallyDisplay(
  contacts: Contact[],
  value: ContactIdDisplayFirstLastName
): string {
  const mainName = (value.displayName ?? value.firstName)
  const contactsWithIdenticalName = getContactsWithIdenticalName(contacts, value)
  if (contactsWithIdenticalName.length === 0) {
    return mainName
  }
  const contactsWithIdenticalLastNameFirstLetter = contactsWithIdenticalName
    .filter(contact => contact.lastName.charAt(0) === value.lastName.charAt(0))
  if (contactsWithIdenticalLastNameFirstLetter.length === 0) {
    return mainName + " " + value.lastName.charAt(0)
  }
  return mainName + " " + value.lastName
}

export function simplifyOtherContactsDisplayedNames(value: ContactIdDisplayFirstLastName) {
  const otherContacts = getContactsWithIdenticalNameFromAll(value)
  const store = myStore()
  otherContacts.forEach(contact => {
    const previousJson = stringifyBigint(contact)
    contact.nameToActuallyDisplay = getNameToActuallyDisplay(otherContacts, contact)
    if (previousJson !== stringifyBigint(contact)) {
      saveContact(contact)
      store.contacts[store.contacts.findIndex(it => it.id === contact.id)!] = contact
    }
  })
}

export function formatBirthdayDate(date: BirthdayDateDto): string {
  const monthNames = [
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
  ];

  const day = String(date.day).padStart(2, "0");
  const month = monthNames[date.month - 1];

  if (!month) {
    throw new Error(`Invalid month value: ${date.month}`);
  }

  let result = day + " " + month
  if (date.year) {
    result += " " + date.year
  }

  return result
}

function normalize(s: string): string {
  return s
    .toLowerCase()
    .normalize("NFKD")
    .replace(/\s+/g, " ")
    .trim()
}

export function searchTokens(contacts: Contact[], query: string): Contact[] {
  const tokens = normalize(query).split(" ")

  return contacts.filter(contact => {
    let arg = `${contact.nameToActuallyDisplay} ${contact.firstName} ${contact.lastName}`
    contact.communicationChannels.forEach(communicationChannel => {
      arg += ` ${communicationChannel.value}`
    })
    if (contact.patronymicName) {
      arg += ` ${contact.patronymicName}`
    }
    const haystack = normalize(arg)
    return tokens.every(t => haystack.includes(t))
  })
}
