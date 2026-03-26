pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "atelier-validator"

include("atelier-validator-core")
include("atelier-validator-kotlinx-datetime")
include("atelier-validator-ktor-server")
include("atelier-validator-ktor-client")
