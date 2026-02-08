package net.sevoos.contacts.contact.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "contact")
data class ContactDefaultSettingsConfig(
    val defaultTimezone: String?
)
