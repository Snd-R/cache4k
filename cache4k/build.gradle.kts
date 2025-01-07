import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinAtomicfu)
}

kotlin {
    jvm{
        val main = compilations.getByName("main")
        compilations.create("lincheck") {
            defaultSourceSet {
                dependencies {
                    implementation(main.compileDependencyFiles + main.output.classesDirs)
                }
            }
            project.tasks.register<Test>("jvmLincheck") {
                classpath = compileDependencyFiles + runtimeDependencyFiles + output.allOutputs
                testClassesDirs = output.classesDirs
                useJUnitPlatform()
                testLogging {
                    events("passed", "skipped", "failed")
                }
                jvmArgs(
                    "--add-opens", "java.base/jdk.internal.misc=ALL-UNNAMED",
                    "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                    "--add-exports", "java.base/jdk.internal.util=ALL-UNNAMED",
                    "--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED",
                    "--add-exports", "java.base/jdk.internal.access=ALL-UNNAMED",
                )
            }
        }
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    sourceSets {
        val nonJvmMain by creating {
            dependsOn(commonMain.get())
        }
        wasmJsMain {
            dependsOn(nonJvmMain)
        }
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.stately.isoCollections)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        jvmTest {
            dependencies {
                implementation(kotlin("test-junit5"))
            }
        }
        val jvmLincheck by getting {
            dependsOn(jvmMain.get())
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation(libs.kotlinx.lincheck)
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
