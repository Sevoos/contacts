@file:JsExport

package net.sevoos.timezone

import kotlinx.datetime.IllegalTimeZoneException
import kotlinx.datetime.TimeZone
import kotlin.js.JsExport

fun isTimezoneValid(timezone: String): Boolean {
    try {
        TimeZone.of(timezone)
        return true
    } catch (_: IllegalTimeZoneException) {
        return false
    }
}