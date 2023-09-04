// build.gradle.kts
plugins {
    kotlin("multiplatform") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val hostArch = System.getProperty("os.arch")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" && hostArch == "x86_64" -> macosX64("native")
        hostOs == "Mac OS X" && (hostArch == "arm64" || hostArch == "aarch64") -> macosArm64("native")
        isMingwX64 -> mingwX64("native")
        hostOs == "Linux" -> linuxX64("native")
        else -> throw GradleException("Host OS is not supported: $hostOs")
    }

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "dev.commandk.cli.main"
                runTask?.run {
                    val args = providers.gradleProperty("runArgs")
                    argumentProviders.add(
                        CommandLineArgumentProvider {
                            args.orNull?.split(' ') ?: emptyList()
                        },
                    )
                }
            }
        }
    }

    sourceSets {
        val nativeTest by getting
        val nativeMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:2.3.2")
                implementation("io.ktor:ktor-client-curl:2.3.2")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.2")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.2")
                implementation("io.arrow-kt:arrow-core:1.2.0")
                implementation("io.arrow-kt:arrow-fx-coroutines:1.2.0")
                implementation("com.squareup.okio:okio:3.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
                implementation("com.github.ajalt.mordant:mordant:2.0.1")
                implementation("com.github.ajalt.clikt:clikt:4.1.0")
                implementation("io.ktor:ktor-client-logging:2.3.2")
                implementation("com.kgit2:kommand:1.0.2")
            }
        }
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.2.1")
            }
        }
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "7.6"
    distributionType = Wrapper.DistributionType.BIN
}
