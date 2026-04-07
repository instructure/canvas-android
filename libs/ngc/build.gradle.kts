/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    kotlin("plugin.serialization") version "2.1.20"
}

android {
    namespace = "com.instructure.ngc"
    compileSdk = Versions.COMPILE_SDK
    buildToolsVersion = Versions.BUILD_TOOLS

    defaultConfig {
        minSdk = Versions.MIN_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildTypes.getByName("debug") {
        isDebuggable = true
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources.excludes.add("META-INF/DEPENDENCIES")
        resources.excludes.add("META-INF/LICENSE")
        resources.excludes.add("META-INF/LICENSE.txt")
        resources.excludes.add("META-INF/NOTICE")
        resources.excludes.add("LICENSE.txt")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    hilt {
        enableAggregatingTask = false
    }
}

dependencies {
    implementation(project(":pandautils"))

    implementation(Libs.NAVIGATION_COMPOSE)
    implementation(Libs.HILT)
    ksp(Libs.HILT_COMPILER)
    implementation(Libs.HILT_ANDROIDX_WORK)
    ksp(Libs.HILT_ANDROIDX_COMPILER)

    implementation(Libs.ANDROIDX_ANNOTATION)
    implementation(Libs.ANDROIDX_APPCOMPAT)

    implementation(Libs.VIEW_MODEL)
    implementation(Libs.HILT_COMPOSE_NAVIGATION)
    implementation(Libs.KOTLIN_SERIALIZABLE)

    implementation(Libs.FIREBASE_CRASHLYTICS) {
        isTransitive = true
    }

    /* Unit Test Dependencies */
    testImplementation(Libs.JUNIT)
    testImplementation(Libs.MOCKK)
    testImplementation(Libs.KOTLIN_COROUTINES_TEST)
    testImplementation(Libs.THREETEN_BP)
    testImplementation(Libs.ANDROIDX_CORE_TESTING)
}
