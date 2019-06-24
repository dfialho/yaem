val kotlinVersion: String by project
val logbackVersion: String by project
val loggingVersion: String by project
val assertkVersion: String by project
val mockkVersion: String by project

plugins {
    kotlin("jvm") version "1.3.40" apply false
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

        "testImplementation"("com.willowtreeapps.assertk:assertk-jvm:$assertkVersion")
        "testImplementation"("io.mockk:mockk:$mockkVersion")
    }
}
