plugins {
	kotlin("jvm")
//	kotlin("plugin.spring") version "1.9.25"
//	id("org.springframework.boot") version "3.5.6"
//	id("io.spring.dependency-management") version "1.1.7"
//    id("common-dependencies")
    application
}

group = "net.sevoos"
version = "1.0"
description = ""

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
    implementation(project(":shared"))
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
    mainClass = "net.sevoos.contacts.iconhandling.IconHandlingApplicationKt"
}