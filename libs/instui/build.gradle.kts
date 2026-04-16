/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.instructure.instui"
    compileSdk = Versions.COMPILE_SDK
    buildToolsVersion = Versions.BUILD_TOOLS

    defaultConfig {
        minSdk = Versions.MIN_SDK
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    // Compose dependencies for Color, Dp, FontWeight, FontFamily
    implementation(platform(Libs.COMPOSE_BOM))
    implementation(Libs.COMPOSE_UI)
    implementation(Libs.COMPOSE_FOUNDATION)
}

/**
 * Gradle task to generate InstUI tokens from instructure-ui.
 *
 * Usage: ./gradlew :libs:instui:generateInstUITokens
 *
 * This task:
 * 1. Installs npm dependencies if needed
 * 2. Downloads tokens from instructure-ui repository
 * 3. Generates Kotlin files using Style Dictionary
 */
tasks.register<Exec>("generateInstUITokens") {
    group = "build"
    description = "Downloads InstUI tokens and generates Kotlin primitives for Compose"
    workingDir = file("scripts")

    doFirst {
        // Ensure npm dependencies are installed
        if (!file("scripts/node_modules").exists()) {
            println("Installing npm dependencies...")
            exec {
                workingDir = file("scripts")
                commandLine("npm", "install")
            }
        }
    }

    commandLine("npm", "run", "build")
}
