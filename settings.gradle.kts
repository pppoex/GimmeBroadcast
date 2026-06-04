pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.neoforged.net/releases") }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "GimmeBroadcast-Multi"

include("broadcast-core")
include("mc-1.21.0")
include("mc-1.21.1")
include("mc-1.21.2")
include("mc-1.21.3")
include("mc-1.21.4")
include("mc-1.21.5")
include("mc-1.21.6")
include("mc-1.21.7")
include("mc-1.21.8")
include("mc-1.21.9")
include("mc-1.21.10")
include("mc-1.21.11")
