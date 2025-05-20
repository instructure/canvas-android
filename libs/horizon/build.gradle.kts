plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    kotlin("plugin.serialization") version "2.1.20"
}
apply(plugin = "kotlin-parcelize")

android {
    namespace = "com.instructure.horizon"
    compileSdk = Versions.COMPILE_SDK
    buildToolsVersion = Versions.BUILD_TOOLS

    defaultConfig {
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK

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
}

dependencies {
    implementation(project(":pandautils"))

    implementation(Libs.NAVIGATION_COMPOSE)
    implementation(Libs.HILT)
    kapt(Libs.HILT_COMPILER)

    implementation(Libs.ANDROIDX_ANNOTATION)
    implementation(Libs.ANDROIDX_APPCOMPAT)
    implementation(Libs.ANDROIDX_WEBKIT)

    implementation(Libs.VIEW_MODEL)
    implementation(Libs.HILT_COMPOSE_NAVIGATION)
    implementation(Libs.KOTLIN_SERIALIZABLE)

    /* WorkManager */
    implementation(Libs.ANDROIDX_WORK_MANAGER)
    implementation(Libs.ANDROIDX_WORK_MANAGER_KTX)
}