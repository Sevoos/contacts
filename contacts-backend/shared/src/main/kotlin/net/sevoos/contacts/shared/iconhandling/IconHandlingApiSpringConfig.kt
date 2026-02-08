package net.sevoos.contacts.shared.iconhandling

import net.sevoos.contacts.iconhandling.api.IconHandlingApiConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "shared")
/*data*/ class IconHandlingApiSpringConfig/*(
    val hostIconHandling: String,
    val apiIconHandling: String
)*/: IconHandlingApiConfig {
    constructor(hostIconHandling: String, apiIconHandling: String): super(hostIconHandling, apiIconHandling)
}
