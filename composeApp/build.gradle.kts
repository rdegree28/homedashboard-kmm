import java.util.Properties
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

// HA connection config is injected from the gitignored local.properties (keeps secrets out of source).
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

// Generate a WebDefaults constant for the web target (no BuildConfig on wasmJs) from local.properties.
// The generated file lands in build/ (gitignored), so the token never enters source control.
val generateWebConfig = tasks.register("generateWebConfig") {
    val outDir = layout.buildDirectory.dir("generated/webConfig")
    val url = localProps.getProperty("ha.url", "")
    val token = localProps.getProperty("ha.token", "")
    inputs.property("url", url)
    inputs.property("token", token)
    outputs.dir(outDir)
    doLast {
        val file = outDir.get().file("com/degree/homedash/WebDefaults.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            |package com.degree.homedash
            |
            |internal object WebDefaults {
            |    const val HA_URL = "$url"
            |    const val HA_TOKEN = "$token"
            |}
            |""".trimMargin(),
        )
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.preview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(compose.uiTooling)
            implementation(libs.kotlinx.coroutines.android)
        }
        wasmJsMain {
            kotlin.srcDir(generateWebConfig)
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }
    }
}

composeCompiler {
    // Treat the listed :shared types as stable so composables receiving them remain skippable.
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose_stability.conf"))
}

android {
    namespace = "com.degree.homedash"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        applicationId = "com.degree.homedash"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"

        buildConfigField("String", "HA_URL", "\"${localProps.getProperty("ha.url", "")}\"")
        buildConfigField("String", "HA_TOKEN", "\"${localProps.getProperty("ha.token", "")}\"")
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}
