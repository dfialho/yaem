import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String by project
val logbackVersion: String by project
val loggingVersion: String by project
val assertkVersion: String by project
val mockkVersion: String by project

buildscript {
    val kotlinVersion: String by project

    repositories {
        jcenter()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    }
}

plugins {
    kotlin("jvm") version "1.3.50" apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "dfialho.yaem"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }

    dependencies {
        "implementation"(kotlin("stdlib-jdk8"))
        "implementation"("ch.qos.logback:logback-classic:$logbackVersion")
        "implementation"("io.github.microutils:kotlin-logging:$loggingVersion")
    }

    dependencies {
        "testImplementation"("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
        "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
        "testImplementation"("com.willowtreeapps.assertk:assertk-jvm:$assertkVersion")
        "testImplementation"("io.mockk:mockk:$mockkVersion")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}
