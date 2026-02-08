import {BirthdayDateDto} from "@/lib";

export class BirthdayDateCreation {
  constructor(
    public year: number | null,
    public month: number | null,
    public day: number | null
  ) {
  }
}

export function birthdayDateCreationToDto(object: BirthdayDateCreation): BirthdayDateDto {
  return new BirthdayDateDto(
    object.year,
    object.month!,
    object.day!
  )
}

export function isBirthdayEmpty(birthdayDate: BirthdayDateCreation): boolean {
  return birthdayDate.year === null && birthdayDate.month === null && birthdayDate.day === null
}
