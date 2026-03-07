import {Temporal} from "@js-temporal/polyfill";
import ZonedDateTime = Temporal.ZonedDateTime;
import {readConfig} from "@/config/settings.ts";

export const detectedTimezone = Intl.DateTimeFormat().resolvedOptions().timeZone

export function effectiveTimezone(): string {
  return readConfig().explicitTimezone ?? detectedTimezone
}

export function formatZonedDateTime(zonedDateTime: ZonedDateTime): string {
  function pad(n: number) {
    return String(n).padStart(2, "0");
  }
  const date = `${zonedDateTime.year}-${pad(zonedDateTime.month)}-${pad(zonedDateTime.day)}`
  const time = `${pad(zonedDateTime.hour)}:${pad(zonedDateTime.minute)}`
  return `${date} ${time}`
}
