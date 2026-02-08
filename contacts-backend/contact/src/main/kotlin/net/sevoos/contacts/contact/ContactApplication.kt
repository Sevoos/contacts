package net.sevoos.contacts.contact

import com.fasterxml.jackson.databind.ObjectMapper
import net.sevoos.contacts.shared.iconhandling.IconHandlingApiSpringConfig
import org.openapitools.jackson.nullable.JsonNullableModule
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@SpringBootApplication
@EnableConfigurationProperties(value = [
    IconHandlingApiSpringConfig::class
])
class ContactApplication {
    @Bean
    fun objectMapper(builder: Jackson2ObjectMapperBuilder): ObjectMapper = builder
        .modules(JsonNullableModule()).build()
}

fun main(args: Array<String>) {
    runApplication<ContactApplication>(*args)
}