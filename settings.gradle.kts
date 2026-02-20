pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "atelier-validator"

include("validator-core")
include("validator-kotlinx-datetime")
include("validator-ktor-server")
include("validator-ktor-client")
