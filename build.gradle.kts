plugins {
    kotlin("multiplatform") version "2.3.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0"
}

kotlin {
    jvmToolchain(25)
}

group = "net.incongru.brewery"
version = "1.0-SNAPSHOT"
description = "An over-engineered script to routinely update a Homebrew setup"

repositories {
    mavenCentral()
}

kotlin {

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
            }
        }
    }

    listOf(
        macosArm64(), // Mac M1
        macosX64(),   // Mac Legacy
    ).forEach { nativeTarget ->
        nativeTarget.apply {
            binaries {
                executable {
                    entryPoint = "net.incongru.brewery.main"
                }
            }
        }
    }
}

tasks.register("createUniversalBinary", Exec::class) {
    // Only run on macOS, where 'lipo' is available.
    onlyIf { System.getProperty("os.name").lowercase().contains("mac") }
    val output = layout.buildDirectory.file("macos-universal/brew-update-all-the-things").get().asFile
    output.parentFile.mkdirs()

    commandLine(
        "lipo",
        "-create",
        // TODO get those from above
        "build/bin/macosArm64/releaseExecutable/brew-update-all-the-things.kexe",
        "build/bin/macosX64/releaseExecutable/brew-update-all-the-things.kexe",
        "-output", output
    )

    doLast {
        println("Universal binary created at: $output")
    }
}