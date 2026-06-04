// ======================================================================
// mc-1.21.11 — NeoForge 21.11.42
// ======================================================================

plugins {
    id("net.neoforged.gradle.userdev") version "7.1.36"
    `java-library`
}

version = "1.0.0"

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
base { archivesName = "GimmeBroadcast-1.21.11" }

repositories {
    mavenCentral()
    maven { url = uri("https://maven.neoforged.net/releases") }
}

dependencies {
    implementation(libs.nfo11)
}

sourceSets.main.get().java.srcDir(rootProject.projectDir.resolve("src/main/java"))
sourceSets.main.get().java.srcDir(rootProject.projectDir.resolve("broadcast-core/src/main/java"))

tasks.withType<ProcessResources>().configureEach {
    inputs.property("modVersion", version)
    filesMatching("META-INF/neoforge.mods.toml") { expand("modVersion" to version) }
}
