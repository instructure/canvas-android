@file:Suppress("unused")

object Versions {
    /* SDK Versions */
    const val COMPILE_SDK = 28
    const val MIN_SDK = 21
    const val TARGET_SDK = 28

    /* Build/tooling */
    const val ANDROID_GRADLE_TOOLS = "3.4.0"
    const val BUILD_TOOLS = "28.0.3"
    const val BUILD_SCAN = "1.16"

    /* Testing */
    const val ATSL_ORCHESTRATOR = "1.1.0-beta01"
    const val JACOCO = "0.8.3"
    const val JUNIT = "4.12"
    const val ROBOLECTRIC = "4.2.1"
    const val ESPRESSO = "3.1.0"
    const val OKREPLAY = "1.4.0"
    const val JACOCO_ANDROID = "0.1.2"

    /* Kotlin */
    const val KOTLIN = "1.3.31"
    const val KOTLIN_ANKO = "0.10.4"
    const val KOTLIN_COROUTINES = "1.1.1"

    /* Google, Play Services */
    const val GOOGLE_SERVICES = "4.0.1"
    const val ANDROIDX = "1.0.0"
    const val FIREBASE_CORE = "16.0.7"
    const val FIREBASE_JOB_DISPATCHER = "0.8.6"

    /* Others */
    const val APOLLO = "1.0.0-alpha5"
    const val CRASHLYTICS = "2.6.8@aar"
    const val PSPDFKIT = "4.8.1"
    const val EXOPLAYER = "2.9.6"
    const val PHOTO_VIEW = "2.3.0"
    const val ANDROID_SVG = "1.3"
    const val MOBIUS = "1.2.1"
    const val SQLDELIGHT = "1.1.3"
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
    const val ANDROIDX_ANNOTATION = "androidx.annotation:annotation:${Versions.ANDROIDX}"
    const val ANDROIDX_APPCOMPAT = "androidx.appcompat:appcompat:${Versions.ANDROIDX}"
    const val ANDROIDX_BROWSER = "androidx.browser:browser:1.0.0"
    const val ANDROIDX_CARDVIEW = "androidx.cardview:cardview:${Versions.ANDROIDX}"
    const val ANDROIDX_CONSTRAINT_LAYOUT = "androidx.constraintlayout:constraintlayout:1.1.2"
    const val ANDROIDX_DESIGN = "com.google.android.material:material:${Versions.ANDROIDX}"
    const val ANDROIDX_EXIF = "androidx.exifinterface:exifinterface:${Versions.ANDROIDX}"
    const val ANDROIDX_FRAGMENT = "androidx.fragment:fragment:${Versions.ANDROIDX}"
    const val ANDROIDX_PALETTE = "androidx.palette:palette:${Versions.ANDROIDX}"
    const val ANDROIDX_PERCENT = "androidx.percentlayout:percentlayout:${Versions.ANDROIDX}"
    const val ANDROIDX_RECYCLERVIEW = "androidx.recyclerview:recyclerview:${Versions.ANDROIDX}"
    const val ANDROIDX_VECTOR = "androidx.vectordrawable:vectordrawable:${Versions.ANDROIDX}"

    /* Play Services */
    const val PLAY_SERVICES = "com.google.android.gms:play-services-gcm:16.1.0"
    const val PLAY_SERVICES_ANALYTICS = "com.google.android.gms:play-services-analytics:16.0.7"
    const val PLAY_SERVICES_OSS_LICENSES = "com.google.android.gms:play-services-oss-licenses:16.0.2"
    const val PLAY_SERVICES_WEARABLE = "com.google.android.gms:play-services-wearable:16.0.1"
    const val PLAY_SERVICES_AUTH = "com.google.android.gms:play-services-auth:16.0.1"
    const val FIREBASE_CORE = "com.google.firebase:firebase-core:${Versions.FIREBASE_CORE}"
    const val FIREBASE_JOB_DISPATCHER = "com.firebase:firebase-jobdispatcher:${Versions.FIREBASE_JOB_DISPATCHER}"

    /* Mobius */
    const val MOBIUS_CORE = "com.spotify.mobius:mobius-core:${Versions.MOBIUS}"
    const val MOBIUS_TEST = "com.spotify.mobius:mobius-test:${Versions.MOBIUS}"
    const val MOBIUS_ANDROID = "com.spotify.mobius:mobius-android:${Versions.MOBIUS}"
    const val MOBIUS_EXTRAS = "com.spotify.mobius:mobius-extras:${Versions.MOBIUS}"

    /* Testing */
    const val JUNIT = "junit:junit:${Versions.JUNIT}"
    const val ROBOLECTRIC = "org.robolectric:robolectric:${Versions.ROBOLECTRIC}"
    const val ANDROIDX_TEST_JUNIT = "androidx.test.ext:junit:1.1.0"
    const val MOCKK = "io.mockk:mockk:1.9.3"

    /* Other */
    const val CRASHLYTICS = "com.crashlytics.sdk.android:crashlytics:${Versions.CRASHLYTICS}"
    const val PSPDFKIT = "com.pspdfkit:pspdfkit:${Versions.PSPDFKIT}"
    const val EXOPLAYER = "com.google.android.exoplayer:exoplayer:${Versions.EXOPLAYER}"
    const val PHOTO_VIEW = "com.github.chrisbanes:PhotoView:${Versions.PHOTO_VIEW}"
    const val ANDROID_SVG = "com.caverock:androidsvg:${Versions.ANDROID_SVG}"
}

object Plugins {
    const val FABRIC = "io.fabric.tools:gradle:1.+"
    const val ANDROID_GRADLE_TOOLS = "com.android.tools.build:gradle:${Versions.ANDROID_GRADLE_TOOLS}"
    const val APOLLO = "com.apollographql.apollo:apollo-gradle-plugin:${Versions.APOLLO}"
    const val KOTLIN = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.KOTLIN}"
    const val OSS_LICENSES = "com.google.android.gms:oss-licenses-plugin:0.9.4"
    const val GOOGLE_SERVICES = "com.google.gms:google-services:${Versions.GOOGLE_SERVICES}"
    const val OKREPLAY = "com.airbnb.okreplay:gradle-plugin:${Versions.OKREPLAY}"
    const val BUILD_SCAN = "com.gradle:build-scan-plugin:${Versions.BUILD_SCAN}"
    const val JACOCO_ANDROID = "com.dicedmelon.gradle:jacoco-android:${Versions.JACOCO_ANDROID}"
    const val SQLDELIGHT = "com.squareup.sqldelight:gradle-plugin:${Versions.SQLDELIGHT}"
}

object BuildScan {
    const val SERVER = "https://hab4legp72thxyba43fm5jueym-trial.gradle.com"
    const val PLUGIN_ID = "com.gradle.build-scan"
}
