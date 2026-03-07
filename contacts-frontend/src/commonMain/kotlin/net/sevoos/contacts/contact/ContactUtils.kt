@file:JsExport

package net.sevoos.contacts.contact

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn
import net.sevoos.contacts.birthdaydate.dto.BirthdayDateDto
import kotlin.js.JsExport
import kotlin.time.Clock
import kotlin.time.Instant

fun timezoneOf(zoneId: String): TimeZone = TimeZone.of(zoneId)

fun getBirthdayStart(birthdayDateDto: BirthdayDateDto, timezone: TimeZone): Instant {
    val todayDate = Clock.System.todayIn(timezone)
    val currentYear = todayDate.year
    val birthdayDateThisYear = LocalDate(currentYear, birthdayDateDto.month, birthdayDateDto.day)
    val year = if (todayDate > birthdayDateThisYear) {
        currentYear + 1
    } else {
        currentYear
    }
    return LocalDate(year, birthdayDateDto.month, birthdayDateDto.day).atStartOfDayIn(timezone)
}