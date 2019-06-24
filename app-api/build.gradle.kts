val serializationVersion: String by project

apply(plugin = "kotlinx-serialization")

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationVersion")
}