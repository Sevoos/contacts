package net.sevoos.contacts.iconhandling

import net.sevoos.contacts.iconhandling.config.IconHandlingConfig
import net.sevoos.contacts.contact.ContactApiSpringConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [
    DataSourceAutoConfiguration::class,
    HibernateJpaAutoConfiguration::class
])
@EnableConfigurationProperties(value = [
    IconHandlingConfig::class,
    ContactApiSpringConfig::class
])
class IconHandlingApplication

fun main(args: Array<String>) {
	runApplication<IconHandlingApplication>(*args)
}
