package net.sevoos.contacts.contact.api

import kotlin.js.JsExport

@JsExport
open class ContactApiConfig(
    open val hostContact: String,
    open val apiContact: String
)