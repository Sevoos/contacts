package net.sevoos.contacts.contact

import net.sevoos.contacts.contact.api.ContactApiConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "shared")
class ContactApiSpringConfig : ContactApiConfig {
    //    override val hostContact: String,
//    override val apiContact: String

    constructor(hostContact: String, apiContact: String) : super(hostContact, apiContact)
}