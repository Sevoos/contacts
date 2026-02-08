import {BirthdayDateDto} from "@/lib";

export class ContactGeneralPatch {
  constructor(
    public birthdayDate?: BirthdayDateDto | null,
    public category?: Array<string>,
    public communicationChannels?: bigint[],
    public defaultIconId?: bigint | null,
    public displayName?: string | null,
    public firstName?: string,
    public icons?: bigint[],
    public lastName?: string,
    public patronymicName?: string | null,
    public timezone?: string
  ) {
  }
}

export type ContactGeneralPatchKeyName = keyof ContactGeneralPatch

export const birthdayDate = "birthdayDate"
export const category = "category"
export const communicationChannels = "communicationChannels"
export const defaultIconId = "defaultIconId"
export const displayName = "displayName"
export const firstName = "firstName"
export const icons = "icons"
export const lastName = "lastName"
export const patronymicName = "patronymicName"
export const timezone = "timezone"
