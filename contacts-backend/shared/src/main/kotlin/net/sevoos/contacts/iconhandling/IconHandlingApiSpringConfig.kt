package net.sevoos.contacts.iconhandling

import net.sevoos.contacts.iconhandling.api.IconHandlingApiConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "shared")
/*data*/ class IconHandlingApiSpringConfig/*(
    val iconHandlingHost: String,
    val apiIconHandling: String
)*/: IconHandlingApiConfig {
    constructor(iconHandlingHost: String, apiIconHandling: String): super(iconHandlingHost, apiIconHandling)
}
