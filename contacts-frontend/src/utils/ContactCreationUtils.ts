import {BirthdayDateDto, ContactCreationDto} from "@/lib";

export const birthdayDate = "birthdayDate"
export const category = "category"
export const communicationChannels = "communicationChannels"
export const displayName = "displayName"
export const firstName = "firstName"
export const lastName = "lastName"
export const patronymicName = "patronymicName"
export const timezone = "timezone"

const monthsWith30Days = new Set([4, 6, 9, 11])
const monthsWith31Days = new Set([1, 3, 5, 7, 8, 10, 12])

export function isLeapYear(year: number): boolean {
  return year % 4 === 0 ?
    (
      year % 100 === 0 ?
        year % 400 === 0 :
        true
    ) :
    false
}

export function getMaxDay(month: number, leap: boolean): number {
  function invalidMonth(): number {
    throw new Error("Invalid month")
  }
  return month === 2 ?
    (leap ? 29 : 28) :
    monthsWith30Days.has(month) ? 30 :
      monthsWith31Days.has(month) ? 31 :
        invalidMonth()
}

export type ContactCreationDtoKeyName = keyof ContactCreationDto

const requiredFieldsSet = new Set([firstName, lastName, timezone])
const optionalFieldsSet: Set<string> = new Set([birthdayDate, category, communicationChannels, displayName, patronymicName])

export function isRequired(key: ContactCreationDtoKeyName): boolean {
  if (requiredFieldsSet.has(key)) {
    return true
  } else if (optionalFieldsSet.has(key)) {
    return false
  } else {
    throw new Error("Unknown key: " + key)
  }
}

export function copyContactCreationDto(dto: ContactCreationDto): ContactCreationDto {
  return new ContactCreationDto(
    dto.birthdayDate,
    dto.category,
    dto.communicationChannels,
    dto.displayName,
    dto.firstName,
    dto.lastName,
    dto.patronymicName,
    dto.timezone
  )
}
