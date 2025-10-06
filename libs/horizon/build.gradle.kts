plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    kotlin("plugin.serialization") version "2.1.20"
}

android {
    namespace = "com.instructure.horizon"
    compileSdk = Versions.COMPILE_SDK
    buildToolsVersion = Versions.BUILD_TOOLS

    defaultConfig {
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK

        testInstrumentationRunner = "com.instructure.horizon.espresso.HorizonCustomTestRunner"
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
    kapt(Libs.HILT_COMPILER)
    implementation(Libs.HILT_ANDROIDX_WORK)
    kapt(Libs.HILT_ANDROIDX_COMPILER)

    implementation(Libs.PSPDFKIT)

    implementation(Libs.ANDROIDX_ANNOTATION)
    implementation(Libs.ANDROIDX_APPCOMPAT)
    implementation(Libs.ANDROIDX_WEBKIT)

    implementation(Libs.VIEW_MODEL)
    implementation(Libs.HILT_COMPOSE_NAVIGATION)
    implementation(Libs.KOTLIN_SERIALIZABLE)

    /* WorkManager */
    implementation(Libs.ANDROIDX_WORK_MANAGER)
    implementation(Libs.ANDROIDX_WORK_MANAGER_KTX)

    implementation(Libs.FIREBASE_CRASHLYTICS) {
        isTransitive = true
    }

    /* Android Test Dependencies */
    androidTestImplementation(project(":espresso"))
    androidTestImplementation(project(":dataseedingapi"))
    androidTestImplementation(Libs.COMPOSE_UI_TEST)

    /* Unit Test Dependencies */
    testImplementation(Libs.JUNIT)
    testImplementation(Libs.ROBOLECTRIC)
    testImplementation(Libs.ANDROIDX_TEST_JUNIT)
    testImplementation(Libs.MOCKK)
    androidTestImplementation(Libs.ANDROIDX_TEST_JUNIT)
    testImplementation(Libs.KOTLIN_COROUTINES_TEST)
    testImplementation(Libs.THREETEN_BP)
    testImplementation(Libs.ANDROIDX_CORE_TESTING)
    androidTestImplementation(Libs.HILT_TESTING)

    /* Pandautils dependencies to provide fake implementations for testing */
    androidTestImplementation(Libs.PLAY_IN_APP_UPDATES)
    androidTestImplementation(Libs.ROOM)
}