import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String by project
val logbackVersion: String by project
val loggingVersion: String by project
val assertkVersion: String by project
val mockkVersion: String by project
val kotlintestVersion: String by project

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
    id("info.solidsoft.pitest") version "1.4.5"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "info.solidsoft.pitest")

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
        "testImplementation"("io.kotlintest:kotlintest-runner-junit5:3.3.2")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks.withType<Test> {
        useJUnitPlatform { }
    }
}
