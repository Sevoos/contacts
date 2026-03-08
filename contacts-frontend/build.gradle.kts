import com.google.devtools.ksp.gradle.KspAATask
import org.jetbrains.kotlin.gradle.dsl.JsModuleKind

plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp") version "2.3.2"
    kotlin("plugin.serialization") version "1.9.0"
    `maven-publish`
}

version = "1.0"
group = "net.sevoos.contacts"

val moduleName = "contacts-frontend"

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
        compilations["main"].packageJson {
            customField("name", "@contacts/frontend-api")
            customField("version", "1.0.0")
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
}

val taskJsDevDist = tasks.named("jsBrowserDevelopmentLibraryDistribution")
val taskJsProdDist = tasks.named("jsBrowserProductionLibraryDistribution")

val kspKotlinJs = "kspKotlinJs"

taskJsDevDist.configure {
    mustRunAfter(tasks.named(kspKotlinJs))
}
taskJsProdDist.configure {
    mustRunAfter(tasks.named(kspKotlinJs))
}

val distJsDirectory = rootProject.projectDir
    .resolve(moduleName)
    .resolve("build")
    .resolve("dist")
    .resolve("js")

val frontendLibraryDirectory = rootProject.projectDir
    .resolve("contacts-frontend-js")
    .resolve("kotlin-lib")

tasks.register<Copy>("exportJavascriptDevelopment") {
    dependsOn(tasks.named(kspKotlinJs), taskJsDevDist)
    from(distJsDirectory.resolve("developmentLibrary"))
    into(frontendLibraryDirectory)
}

tasks.register<Copy>("exportJavascriptProduction") {
    dependsOn(tasks.named(kspKotlinJs), taskJsProdDist)
    from(distJsDirectory.resolve("productionLibrary"))
    into(frontendLibraryDirectory)
}

tasks.withType<KspAATask>().configureEach {
    outputs.cacheIf { false }
}
