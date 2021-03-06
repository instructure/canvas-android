@file:Suppress("unused")

object Versions {
    /* SDK Versions */
    const val COMPILE_SDK = 29
    const val MIN_SDK = 23
    const val TARGET_SDK = 29

    /* Build/tooling */
    const val ANDROID_GRADLE_TOOLS = "3.5.1"
    const val BUILD_TOOLS = "28.0.3"
    const val BUILD_SCAN = "1.16"

    /* Testing */
    const val ATSL_ORCHESTRATOR = "1.1.0-beta01"
    const val JACOCO = "0.8.3"
    const val JUNIT = "4.12"
    const val ROBOLECTRIC = "4.3.1"
    const val ESPRESSO = "3.1.0"
    const val JACOCO_ANDROID = "0.1.4"

    /* Kotlin */
    const val KOTLIN = "1.4.10"
    const val KOTLIN_ANKO = "0.10.4"
    const val KOTLIN_COROUTINES = "1.3.9"

    /* Google, Play Services */
    const val GOOGLE_SERVICES = "4.3.3"
    const val ANDROIDX = "1.0.0"
    const val FIREBASE_CONFIG = "18.0.0"
    const val PLAY_CORE = "1.10.0"
    const val PLAY_CORE_KTX = "1.8.1"

    /* Others */
    const val APOLLO = "2.4.1"
    const val CRASHLYTICS = "17.2.1"
    const val ANALYTICS = "17.4.1"
    const val FIREBASE_ANALYTICS = "17.4.1"
    const val PSPDFKIT = "6.5.2"
    const val PHOTO_VIEW = "2.3.0"
    const val ANDROID_SVG = "1.3"
    const val MOBIUS = "1.2.1"
    const val SQLDELIGHT = "1.4.3"
    const val HILT = "2.35"
    const val LIFECYCLE = "2.3.0"
    const val FRAGMENT_KTX = "1.3.1"
    const val WORK_MANAGER = "2.5.0"
}

object Libs {
    /* Kotlin */
    const val KOTLIN_STD_LIB = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.KOTLIN}"
    const val KOTLIN_COROUTINES_CORE = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.KOTLIN_COROUTINES}"
    const val KOTLIN_COROUTINES_ANDROID = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.KOTLIN_COROUTINES}"
    const val KOTLIN_COROUTINES_TEST = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.KOTLIN_COROUTINES}"

    /* Apollo/GraphQL */
    const val APOLLO_RUNTIME = "com.apollographql.apollo:apollo-runtime:${Versions.APOLLO}"
    const val APOLLO_ANDROID_SUPPORT = "com.apollographql.apollo:apollo-android-support:${Versions.APOLLO}"
    const val APOLLO_HTTP_CACHE = "com.apollographql.apollo:apollo-http-cache:${Versions.APOLLO}"

    /* Support Libs */
    const val ANDROIDX_ANNOTATION = "androidx.annotation:annotation:1.1.0"
    const val ANDROIDX_APPCOMPAT = "androidx.appcompat:appcompat:1.0.2"
    const val ANDROIDX_BROWSER = "androidx.browser:browser:1.0.0"
    const val ANDROIDX_CARDVIEW = "androidx.cardview:cardview:${Versions.ANDROIDX}"
    const val ANDROIDX_CONSTRAINT_LAYOUT = "androidx.constraintlayout:constraintlayout:1.1.3"
    const val ANDROIDX_DESIGN = "com.google.android.material:material:1.2.1"
    const val ANDROIDX_EXIF = "androidx.exifinterface:exifinterface:${Versions.ANDROIDX}"
    const val ANDROIDX_FRAGMENT = "androidx.fragment:fragment:${Versions.ANDROIDX}"
    const val ANDROIDX_PALETTE = "androidx.palette:palette:${Versions.ANDROIDX}"
    const val ANDROIDX_PERCENT = "androidx.percentlayout:percentlayout:${Versions.ANDROIDX}"
    const val ANDROIDX_RECYCLERVIEW = "androidx.recyclerview:recyclerview:${Versions.ANDROIDX}"
    const val ANDROIDX_VECTOR = "androidx.vectordrawable:vectordrawable:${Versions.ANDROIDX}"
    const val ANDROIDX_SWIPE_REFRESH_LAYOUT = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
    const val ANDROIDX_CORE_TESTING = "androidx.arch.core:core-testing:2.1.0"
    const val ANDROIDX_WORK_MANAGER = "androidx.work:work-runtime:${Versions.WORK_MANAGER}"
    const val ANDROIDX_WORK_MANAGER_KTX = "androidx.work:work-runtime-ktx:${Versions.WORK_MANAGER}"

    /* Play Services */
    const val FIREBASE_ANALYTICS = "com.google.firebase:firebase-analytics:${Versions.FIREBASE_ANALYTICS}"
    const val FIREBASE_CRASHLYTICS = "com.google.firebase:firebase-crashlytics:${Versions.CRASHLYTICS}"
    const val FIREBASE_MESSAGING = "com.google.firebase:firebase-messaging:20.1.7"
    const val PLAY_SERVICES_ANALYTICS = "com.google.android.gms:play-services-analytics:17.0.0"
    const val PLAY_SERVICES_WEARABLE = "com.google.android.gms:play-services-wearable:16.0.1"
    const val PLAY_SERVICES_AUTH = "com.google.android.gms:play-services-auth:16.0.1"
    const val FIREBASE_CORE = "com.google.firebase:firebase-core:17.2.0"
    const val FIREBASE_CONFIG = "com.google.firebase:firebase-config:${Versions.FIREBASE_CONFIG}"
    const val PLAY_CORE = "com.google.android.play:core:${Versions.PLAY_CORE}"
    const val PLAY_CORE_KTX = "com.google.android.play:core-ktx:${Versions.PLAY_CORE_KTX}"

    /* Mobius */
    const val MOBIUS_CORE = "com.spotify.mobius:mobius-core:${Versions.MOBIUS}"
    const val MOBIUS_TEST = "com.spotify.mobius:mobius-test:${Versions.MOBIUS}"
    const val MOBIUS_ANDROID = "com.spotify.mobius:mobius-android:${Versions.MOBIUS}"
    const val MOBIUS_EXTRAS = "com.spotify.mobius:mobius-extras:${Versions.MOBIUS}"

    /* Testing */
    const val JUNIT = "junit:junit:${Versions.JUNIT}"
    const val ROBOLECTRIC = "org.robolectric:robolectric:${Versions.ROBOLECTRIC}"
    const val ANDROIDX_TEST_JUNIT = "androidx.test.ext:junit:1.1.0"
    const val MOCKK = "io.mockk:mockk:1.10.0"
    const val THREETEN_BP = "org.threeten:threetenbp:1.3.8"

    /* Qr Code (zxing) */
    const val JOURNEY_ZXING = "com.journeyapps:zxing-android-embedded:4.1.0"
    const val ZXING = "com.google.zxing:core:3.3.0"

    /* Dependency Inejction */
    const val HILT = "com.google.dagger:hilt-android:${Versions.HILT}"
    const val HILT_COMPILER = "com.google.dagger:hilt-android-compiler:${Versions.HILT}"
    const val HILT_TESTING = "com.google.dagger:hilt-android-testing:${Versions.HILT}"
    const val HILT_TESTING_COMPILER = "com.google.dagger:hilt-android-compiler:${Versions.HILT}"

    /* Android Architecture Components */
    const val VIEW_MODEL = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.LIFECYCLE}"
    const val LIVE_DATA = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.LIFECYCLE}"
    const val VIEW_MODE_SAVED_STATE = "androidx.lifecycle:lifecycle-viewmodel-savedstate:${Versions.LIFECYCLE}"
    const val LIFECYCLE_COMPILER = "androidx.lifecycle:lifecycle-compiler:${Versions.LIFECYCLE}"
    const val FRAGMENT_KTX = "androidx.fragment:fragment-ktx:${Versions.FRAGMENT_KTX}"

    /* Other */
    const val PSPDFKIT = "com.pspdfkit:pspdfkit:${Versions.PSPDFKIT}"
    const val EXOPLAYER = "com.github.google.ExoPlayer:exoplayer:r2.9.6"
    const val PHOTO_VIEW = "com.github.chrisbanes:PhotoView:${Versions.PHOTO_VIEW}"
    const val ANDROID_SVG = "com.caverock:androidsvg:${Versions.ANDROID_SVG}"
}

object Plugins {
    const val FIREBASE_CRASHLYTICS =  "com.google.firebase:firebase-crashlytics-gradle:2.1.0"
    const val ANDROID_GRADLE_TOOLS = "com.android.tools.build:gradle:${Versions.ANDROID_GRADLE_TOOLS}"
    const val APOLLO = "com.apollographql.apollo:apollo-gradle-plugin:${Versions.APOLLO}"
    const val KOTLIN = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.KOTLIN}"
    const val GOOGLE_SERVICES = "com.google.gms:google-services:${Versions.GOOGLE_SERVICES}"
    const val BUILD_SCAN = "com.gradle:build-scan-plugin:${Versions.BUILD_SCAN}"
    const val JACOCO_ANDROID = "com.dicedmelon.gradle:jacoco-android:${Versions.JACOCO_ANDROID}"
    const val SQLDELIGHT = "com.squareup.sqldelight:gradle-plugin:${Versions.SQLDELIGHT}"
    const val HILT = "com.google.dagger:hilt-android-gradle-plugin:${Versions.HILT}"
}

object BuildScan {
    const val SERVER = "https://hab4legp72thxyba43fm5jueym-trial.gradle.com"
    const val PLUGIN_ID = "com.gradle.build-scan"
}
