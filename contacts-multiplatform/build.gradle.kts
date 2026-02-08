import org.jetbrains.kotlin.gradle.dsl.JsModuleKind
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation

plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp") version "2.3.2"
    kotlin("plugin.serialization") version "1.9.0"
    `maven-publish`
}

val localRepoName = "local-repo"
val localRepoPath = rootProject.projectDir.resolve(localRepoName).toString()

kotlin {
    jvm()
    js(IR) {
        browser() {
            testTask {
                useMocha()
            }
        }
        binaries.library()
        generateTypeScriptDefinitions()
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-Xes-long-as-bigint")
                }
            }
        }

        compilerOptions {
            moduleKind.set(JsModuleKind.MODULE_ES)
            target.set("es2015")

            freeCompilerArgs.add("-Xes-long-as-bigint")
        }

    }
    sourceSets {
        val ktorVersion = "3.3.2"
        val datetimeVersion = "0.7.1"
        val serializationVersion = "1.9.0"
        val commonMain by getting {
            dependencies {
                implementation(project(":generation-annotations"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
                // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-serialization-json
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                // https://mvnrepository.com/artifact/io.ktor/ktor-client-core
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                // https://mvnrepository.com/artifact/io.ktor/ktor-client-content-negotiation
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                // https://mvnrepository.com/artifact/io.ktor/ktor-serialization-kotlinx-json
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
                implementation("org.slf4j:slf4j-api:2.0.17")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")
                // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-datetime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$datetimeVersion")
            }
        }
        val jvmMain by getting {
            dependencies {
                // https://mvnrepository.com/artifact/io.ktor/ktor-client-cio
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                // Source: https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-datetime-jvm
                implementation("org.jetbrains.kotlinx:kotlinx-datetime-jvm:$datetimeVersion")
                // Source: https://mvnrepository.com/artifact/org.openapitools/jackson-databind-nullable
                implementation("org.openapitools:jackson-databind-nullable:0.2.9")
            }
        }
        val jsMain by getting {
            dependencies {
                // https://mvnrepository.com/artifact/io.ktor/ktor-client-js
                implementation("io.ktor:ktor-client-js:$ktorVersion")
                val coroutinesVersion = "1.10.2"
                // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core-js
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutinesVersion")
                // Source: https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-datetime-js
                implementation("org.jetbrains.kotlinx:kotlinx-datetime-js:$datetimeVersion")
            }
        }
    }

    sourceSets.all {
        languageSettings {
            optIn("kotlin.js.ExperimentalJsExport")
            optIn("kotlin.ExperimentalMultiplatform")
            optIn("kotlin.time.ExperimentalTime")
            optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

dependencies {
    add("kspJvm", project(":ksp-processor"))
    add("kspJs", project(":ksp-processor"))
//    add("kspCommonMainMetadata", project(":ksp-processor"))
}

tasks.register<Copy>("copyLocalRepo") {
    dependsOn(":generation-annotations:publishGeneration-annotationsPublicationToLocalRepoRepository")
    dependsOn(":contacts-multiplatform:publishContacts-global-jvmPublicationToLocalRepoRepository")

    from(localRepoPath)
    into(rootProject.projectDir.resolve("contacts-backend").resolve(localRepoName).toString())
}

val exportedJsCodeDirectory = rootProject.projectDir
    .resolve("contacts-multiplatform")
    .resolve("build")
    .resolve("dist")
    .resolve("js")
val frontendLibraryDirectory = rootProject.projectDir
    .resolve("contacts-frontend")
    .resolve("src")
    .resolve("lib")

tasks.register<Copy>("exportJavascriptDevelopment") {
    dependsOn(":contacts-multiplatform:kspKotlinJs")
    dependsOn(":contacts-multiplatform:jsBrowserDevelopmentLibraryDistribution")

    from(
        exportedJsCodeDirectory.resolve("developmentLibrary")
    )
    into(frontendLibraryDirectory)
}

publishing {
    publications {
        val name = "contacts-global-jvm"
        create<MavenPublication>(name) {
            groupId = "net.sevoos.contacts"
            artifactId = "contacts-global-jvm"
            version = "1.0.0"
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
            url = uri(localRepoPath)
        }
    }
}

tasks.named("publish") {
    dependsOn("kspKotlinJs")
    dependsOn("kspKotlinJvm")
}

tasks.named("build") {
    dependsOn("kspKotlinJs")
    dependsOn("kspKotlinJvm")
}

ksp {
    //    arg("kotlin.platform.type", "jvm")
}