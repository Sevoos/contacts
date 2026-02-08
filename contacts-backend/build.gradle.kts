plugins {
    kotlin("jvm")
    kotlin("plugin.spring") version "2.2.20"
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.2.20"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {

}

subprojects {
    project.pluginManager.apply("org.jetbrains.kotlin.jvm")
    project.pluginManager.apply("org.jetbrains.kotlin.plugin.spring")
    project.pluginManager.apply("org.springframework.boot")
    project.pluginManager.apply("io.spring.dependency-management")

    repositories {
        mavenCentral()
        maven {
            url = uri(rootProject.projectDir.resolve("local-repo"))
        }
    }

    dependencies {
        implementation("net.sevoos.contacts:generation-annotations:1.0.0")
        implementation("net.sevoos.contacts:contacts-global-jvm:1.0.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.9.0")
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
//            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
//            implementation("org.springframework.boot:spring-boot-starter-parent:3.5.6")
        implementation("org.springframework.boot:spring-boot-starter-validation")
        // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-jpa
        implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.5.6")
//            // https://mvnrepository.com/artifact/jakarta.persistence/jakarta.persistence-api
//            implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
//        // https://mvnrepository.com/artifact/com.google.guava/guava
//        implementation("com.google.guava:guava:33.5.0-jre")
    }

    tasks.bootJar {
        enabled = true
    }

    tasks.jar {
        enabled = false
    }


}

dependencies {

}