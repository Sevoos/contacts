package net.sevoos.contacts.contact.api

import kotlin.js.JsExport

@JsExport
open class ContactApiConfig(
    open val contactHost: String,
    open val contactApi: String
)