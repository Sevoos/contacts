plugins {
    kotlin("multiplatform")
    `maven-publish`
}

val myGroup = "net.sevoos.contacts"
val myVersion = "1.0.0"

group = myGroup
version = myVersion

kotlin {
    jvm()
    js(IR) { browser() }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
            }
        }
        val jvmMain by getting
        val jsMain by getting
    }
}

publishing {
    publications {
        val name = "generation-annotations"
        create<MavenPublication>(name) {
            groupId = myGroup
            artifactId = name
            version = myVersion
            kotlin.targets["jvm"].components.forEach {
                from(it)
            }
        }
        publications.withType<MavenPublication>().matching {
            it.name != name
        }
    }
    repositories {
        maven {
            name = "localRepo"
            url = uri("${rootProject.projectDir}/local-repo")
        }
    }
}