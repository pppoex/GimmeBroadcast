// Forge 1.21.3-53.1.10 — MC 1.21.3
// To add a new version: copy this dir, then change:
//   FORGE_VERSION, MAPPINGS, and archivesName

plugins {
    id("net.minecraftforge.gradle") version "6.0.53"
    id("java-library")
}

val FORGE_VERSION = "1.21.3-53.1.10"
val MAPPINGS = "1.21.3"

version = "1.0.0"

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
base { archivesName = "GimmeBroadcast-1.21.3" }

repositories {
    mavenCentral()
    maven { url = uri("https://maven.minecraftforge.net") }
}

dependencies {
    minecraft("net.minecraftforge:forge:${FORGE_VERSION}")
}

minecraft {
    mappings("official", MAPPINGS)
}

sourceSets.main.get().java.srcDir(rootProject.projectDir.resolve("src/main/java"))
sourceSets.main.get().java.srcDir(rootProject.projectDir.resolve("broadcast-core/src/main/java"))

tasks.withType<ProcessResources>().configureEach {
    inputs.property("modVersion", version)
    filesMatching("META-INF/mods.toml") { expand("modVersion" to version) }
}
