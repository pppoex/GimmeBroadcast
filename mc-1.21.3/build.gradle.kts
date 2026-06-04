// ======================================================================
// mc-1.21.1 — NeoForge 21.1.233
// ======================================================================
//
// 添加新版本：复制此目录 → mc-1.21.X/
// 改 build.gradle.kts 里三处：
//   1. libs.neoforge["3"]  →  对应版本 key
//   2. archivesName  →  版本号
//   3. version = "1.0.0" →  模组版本
// 改 mc-1.21.X/src/main/resources/META-INF/neoforge.mods.toml 的 versionRange
// 在 gradle/libs.versions.toml 添加版本 + 库条目
// 在 settings.gradle.kts 加 include("mc-1.21.X")
// ======================================================================

plugins {
    id("net.neoforged.gradle.userdev") version "7.1.36"
    `java-library`
}

version = "1.0.0"

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
base { archivesName = "GimmeBroadcast-1.21.3" }

repositories {
    mavenCentral()
    maven { url = uri("https://maven.neoforged.net/releases") }
}

dependencies {
    implementation(libs.nfo3)
}

sourceSets.main.get().java.srcDir(rootProject.projectDir.resolve("src/main/java"))
sourceSets.main.get().java.srcDir(rootProject.projectDir.resolve("broadcast-core/src/main/java"))

tasks.withType<ProcessResources>().configureEach {
    inputs.property("modVersion", version)
    filesMatching("META-INF/neoforge.mods.toml") { expand("modVersion" to version) }
}
