plugins {
    kotlin("multiplatform") version "2.2.20"
}

kotlin {
    jvmToolchain(24)
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
//                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1") // Replace with your actual dependency
            }
        }}
    listOf(
        macosArm64(), // Mac M1
        macosX64(), // Mac Legacy
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