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

export const contactPatchKeys = [birthdayDate, category, defaultIconId, displayName, firstName, lastName, patronymicName, timezone] as const
export const contactGeneralPatchKeys = [...contactPatchKeys, icons, communicationChannels] as const
export type ContactPatchKey = typeof contactPatchKeys[number]
export type ContactGeneralPatchKey = typeof contactGeneralPatchKeys[number]
