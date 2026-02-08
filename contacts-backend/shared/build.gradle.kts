plugins {
    kotlin("jvm")
//    kotlin("plugin.spring") version "1.9.25"
//    id("org.springframework.boot") version "3.5.6"
//    id("io.spring.dependency-management") version "1.1.7"
//    id("org.openapi.generator") version "7.16.0"
    application
}

group = "net.sevoos"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
//openApiGenerate {
//    generatorName.set("kotlin")
//    inputSpec.set("${layout.projectDirectory}/src/main/resources/openapi.yaml")
//    outputDir.set("${layout.buildDirectory.get()}/generated/openapi")
//
////    apiPackage.set("net.sevoos.api")
//    modelPackage.set("net.sevoos.contacts.dto")
////    invokerPackage.set("net.sevoos.client")
//
//    configOptions.set(
//        mapOf(
//            "modelOnly" to "true",
//            "serializationLibrary" to "kotlinx_serialization",
//            "generateApis" to "false",
//            "generateSupportingFiles" to "false"
//        )
//    )
//
//}

//sourceSets {
//    main {
//        java {
//            srcDir("${layout.buildDirectory.get()}/generated/openapi/src/main/kotlin/net/sevoos/contacts/dto")
//        }
//    }
//}

//tasks.named("compileKotlin") {
//    dependsOn("openApiGenerate")
//}

//tasks.named("build") {
//    dependsOn("openApiGenerate")
//}

application {
    mainClass = "net.sevoos.contacts.shared.SharedApplicationKt"
}