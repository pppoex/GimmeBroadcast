pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.minecraftforge.net") }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "net.minecraftforge.gradle") {
                useModule("net.minecraftforge.gradle:ForgeGradle:${requested.version}")
            }
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "GimmeBroadcast-Multi-Forge"
include("broadcast-core")
include("mc-1.21.1")
include("mc-1.21.3")
include("mc-1.21.4")
include("mc-1.21.5")
include("mc-1.21.6")
include("mc-1.21.8")
include("mc-1.21.9")
include("mc-1.21.10")
include("mc-1.21.11")
