import {Temporal} from "@js-temporal/polyfill";
import {timeZonesNames} from "@vvo/tzdb";

export const explicitTimezone = "explicitTimezone"
export const foolproofDeleteDelay = "foolproofDeleteDelay"
export const fetchAllContactsOnlyManually = "fetchAllContactsOnlyManually"
export const fetchAllContactsPeriod = "fetchAllContactsPeriod"
export const memorizeTimezoneDuringContactCreation = "memorizeTimezoneDuringContactCreation"
export const setCreatedIconAsDefault = "setCreatedIconAsDefault"

export const defaultConfig = new Map<string, string>()

const stringFalse = String(false)
const stringTrue = String(true)

defaultConfig.set(explicitTimezone, String(null))
defaultConfig.set(foolproofDeleteDelay, stringTrue)
defaultConfig.set(fetchAllContactsOnlyManually, stringFalse)
defaultConfig.set(fetchAllContactsPeriod, String(24 * 3600 * 1000))
defaultConfig.set(memorizeTimezoneDuringContactCreation, stringTrue)
defaultConfig.set(setCreatedIconAsDefault, stringTrue)

export class MyConfig {
  constructor(
    public explicitTimezone: string | null,
    public foolproofDeleteDelay: boolean,
    public fetchAllContactsOnlyManually: boolean,
    public fetchAllContactsPeriod: number,
    public memorizeTimezoneDuringContactCreation: boolean,
    public setCreatedIconAsDefault: boolean,
  ) {
  }
}

function getItem(key: string): string {
  return localStorage.getItem(key)!
}

function setItem(key: string, value: any) {
  localStorage.setItem(key, String(value))
}

export function toBoolean(value: string): boolean {
  if (value === "true") {
    return true
  } else if (value === "false") {
    return false
  } else {
    throw new Error(`Invalid boolean: \"${value}\"`)
  }
}

export function readConfig(): MyConfig {
  const readExplicitTimezone = getItem(explicitTimezone)
  return new MyConfig(
    readExplicitTimezone === "null" ? null : readExplicitTimezone,
    toBoolean(getItem(foolproofDeleteDelay)),
    toBoolean(getItem(fetchAllContactsOnlyManually)),
    Number(getItem(fetchAllContactsPeriod)),
    toBoolean(getItem(memorizeTimezoneDuringContactCreation)),
    toBoolean(getItem(setCreatedIconAsDefault)),
  )
}

export function validateTimezone(timezone: string): boolean {
  try {
    Temporal.Now.zonedDateTimeISO(timezone)
    return true
  } catch (_) {
    return false
  }
}

export function writeConfig(config: MyConfig) {
  const currentExplicitTimezone = config.explicitTimezone
  if (currentExplicitTimezone !== null && !validateTimezone(currentExplicitTimezone)) {
    throw new Error("Invalid timezone: \"" + currentExplicitTimezone + "\"")
  }

  Object.keys(config).forEach(key => {
    setItem(key, config[key as MyConfigKeyName])
  })

  // setItem(explicitTimezone, currentExplicitTimezone)
  // setItem(fetchAllContactsOnlyManually, config.fetchAllContactsOnlyManually)
  // setItem(fetchAllContactsPeriod, config.fetchAllContactsPeriod)
  // setItem(memorizeTimezoneDuringContactCreation, config.memorizeTimezoneDuringContactCreation)
  // setItem(setCreatedIconAsDefault, config.setCreatedIconAsDefault)
}

export const automatedSelection = 'Automated selection'
export const availableExplicitTimezones = [automatedSelection, ...timeZonesNames]

export function configsEqual(a: MyConfig, b: MyConfig): boolean {
  return JSON.stringify(a) === JSON.stringify(b)
}

export type MyConfigKeyName = keyof MyConfig

export const defaultTimezoneForContactCreation = "defaultTimezoneForContactCreation"
export const latestFetchAllContacts = "latestFetchAllContacts"
