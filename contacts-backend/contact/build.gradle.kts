plugins {
	kotlin("jvm")
	kotlin("plugin.spring") version "2.2.20"
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa")
    application
}

group = "net.sevoos"
version = "1.0"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
    implementation(project(":contacts-backend:shared"))
    // https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.6")
    // https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.6")
//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-datetime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
//    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-logging
//    implementation("org.springframework.boot:spring-boot-starter-logging:3.5.6")
//    // https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
//    implementation("org.slf4j:slf4j-simple:2.0.17")
    // Source: https://mvnrepository.com/artifact/org.openapitools/jackson-databind-nullable
    implementation("org.openapitools:jackson-databind-nullable:0.2.9")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

application {
    mainClass = "net.sevoos.contacts.contact.ContactApplicationKt"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
    }
}