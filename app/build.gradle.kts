import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion: String by project
val exposedVersion: String by project
val h2Version: String by project
val serializationVersion: String by project

plugins {
    application
}

apply(plugin = "kotlinx-serialization")

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

repositories {
    maven { url = uri("https://kotlin.bintray.com/ktor") }
    maven { url = uri("https://dl.bintray.com/kotlin/exposed") }
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
}

dependencies {
    implementation(project(":app-api"))
    implementation(project(":json-lib"))
}

dependencies {
    implementation("org.jetbrains.exposed:exposed:$exposedVersion")
    implementation("com.h2database:h2:$h2Version")
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
}

dependencies {
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
}
