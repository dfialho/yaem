val serializationVersion: String by project

apply(plugin = "kotlinx-serialization")

dependencies {
    implementation(project(":json-lib"))
}
