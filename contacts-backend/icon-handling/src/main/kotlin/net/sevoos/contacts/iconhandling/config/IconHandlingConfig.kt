package net.sevoos.contacts.iconhandling.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "iconhandling")
data class IconHandlingConfig(
    val iconsDirectory: String
)
