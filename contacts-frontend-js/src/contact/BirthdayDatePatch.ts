import {BirthdayDateCreation} from "@/contact/BirthdayDateCreation.ts";

export class BirthdayDatePatch {
  constructor(
    public include: boolean,
    public date: BirthdayDateCreation
  ) {
  }

  public copy(): BirthdayDatePatch {
    return new BirthdayDatePatch(this.include, this.date.copy())
  }

}

export function doBirthdaysEqual(a: BirthdayDatePatch, b: BirthdayDatePatch): boolean {
  return JSON.stringify(a) === JSON.stringify(b)
}
