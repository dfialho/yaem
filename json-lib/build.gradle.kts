apply(plugin = "kotlinx-serialization")

val serializationVersion: String by project

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationVersion")
}
