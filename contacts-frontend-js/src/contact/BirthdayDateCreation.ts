import {BirthdayDateDto} from "@/lib";
import {Temporal} from "@js-temporal/polyfill";
import {getMaxDay, isLeapYear} from "@/utils/ContactCreationUtils.ts";

export class BirthdayDateCreation {
  constructor(
    public year: number | null,
    public month: number | null,
    public day: number | null
  ) {
  }

  public copy(): BirthdayDateCreation {
    return new BirthdayDateCreation(
      this.year,
      this.month,
      this.day
    )
  }

}

export function birthdayDateCreationFromDto(dto: BirthdayDateDto): BirthdayDateCreation {
  return new BirthdayDateCreation(
    dto.year ?? null,
    dto.month,
    dto.day
  )
}

export function birthdayDateCreationToDto(object: BirthdayDateCreation): BirthdayDateDto {
  return new BirthdayDateDto(
    object.year,
    object.month!,
    object.day!
  )
}

export function isBirthdayCreationEmpty(birthdayDate: BirthdayDateCreation): boolean {
  return birthdayDate.year === null && birthdayDate.month === null && birthdayDate.day === null
}

export function getBirthdayDateCreationErrorCheck(object: BirthdayDateCreation, timezone: string): () => string | null {
  return () => {
    if (isBirthdayCreationEmpty(object)) {
      return "Birthday date should be included yet is absent"
    }
    const year = object.year
    const yearNotNullish = year !== null && year !== undefined

    if (yearNotNullish) {
      if (year > Temporal.Now.plainDateISO(timezone).year) {
        return "Year is too great"
      }
      if (year < 1900 && year !== 0) {
        return "Year is too long ago"
      }
    }

    const month = object.month
    if (month === null) {
      return "Month can't be empty"
    }
    if (month < 1 || month > 12) {
      return "Month should belong to [1; 12]"
    }
    const day = object.day
    if (day === null) {
      return "Day can't be empty"
    }
    if (day < 1) {
      return "Day must be a positive number"
    }

    return day <= getMaxDay(month, yearNotNullish ? isLeapYear(year) : true) ? null : "Day is too large"
  }
}

export function emptyBirthdayDateCreation(): BirthdayDateCreation {
  return new BirthdayDateCreation(null, null, null)
}
