import {ContactLocalStore} from "@/utils/ContactLocalStore.ts";
import type {Contact} from "@/contact/Contact.ts";
import {BirthdayDateDto} from "@/lib";
import {Temporal} from "@js-temporal/polyfill";
import Instant = Temporal.Instant;

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

export function getNameToActuallyDisplayFromAllContacts(
  displayName: string | null,
  firstName: string,
  lastName: string
): string {
  return getNameToActuallyDisplay(
    findAllContacts(),
    displayName,
    firstName,
    lastName
  )
}

export function getNameToActuallyDisplay(
  contacts: Contact[],
  displayName: string | null,
  firstName: string,
  lastName: string
): string {

  if (displayName) {
    return displayName
  }
  const contactsWithIdenticalName = contacts.filter(contact => {
    return contact.firstName == firstName
  })
  if (contactsWithIdenticalName.length == 0) {
    return firstName
  }
  return firstName + " " + lastName
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

export function getBirthdayStart(birthday: BirthdayDateDto, timezone: string): Instant {
  // const now = Temporal.Now
  // const startOfToday = now.zonedDateTimeISO(effectiveTimezone()).startOfDay().toInstant()
  // if (timezone === "Europe/Athens") {
  //   return startOfToday
  // }
  // return now.instant().add({hours: 24, seconds: 2})

  const todayDate = Temporal.Now.plainDateISO(timezone);

  let birthdayDate = Temporal.PlainDate.from({
    year: todayDate.year,
    month: birthday.month,
    day: birthday.day
  });

  if (Temporal.PlainDate.compare(todayDate, birthdayDate) > 0) {
    birthdayDate = birthdayDate.add({ years: 1 });
  }

  return birthdayDate
    .toZonedDateTime(timezone)
    .startOfDay().toInstant()

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

export type ContactKeyName = keyof Contact
