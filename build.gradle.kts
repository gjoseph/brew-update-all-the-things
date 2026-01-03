plugins {
    kotlin("multiplatform") version "2.3.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0"
}

kotlin {
    jvmToolchain(25)
}

group = "net.incongru.brewery"
version = "1.0-SNAPSHOT"
description = "An over-engineered script to routinely update a homebrew setup"

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