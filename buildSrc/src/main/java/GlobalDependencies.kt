@file:Suppress("unused")

object Versions {
    /* SDK Versions */
    const val COMPILE_SDK = 30
    const val MIN_SDK = 26
    const val TARGET_SDK = 30

    /* Build/tooling */
    const val ANDROID_GRADLE_TOOLS = "3.5.1"
    const val BUILD_TOOLS = "30.0.2"
    const val BUILD_SCAN = "1.16"

    /* Testing */
    const val JACOCO = "0.8.7"
    const val JUNIT = "4.13.2"
    const val ROBOLECTRIC = "4.3.1"
    const val JACOCO_ANDROID = "0.1.5"

    /* Kotlin */
    const val KOTLIN = "1.5.30"
    const val KOTLIN_COROUTINES = "1.5.2"

    /* Google, Play Services */
    const val GOOGLE_SERVICES = "4.3.10"
    const val FIREBASE_CONFIG = "18.0.0"
    const val PLAY_CORE = "1.10.3"
    const val PLAY_CORE_KTX = "1.8.1"

    /* Others */
    const val APOLLO = "2.5.9"
    const val CRASHLYTICS = "17.2.1"
    const val FIREBASE_ANALYTICS = "17.4.1"
    const val PSPDFKIT = "8.1.0"
    const val PHOTO_VIEW = "2.3.0"
    const val MOBIUS = "1.2.1"
    const val SQLDELIGHT = "1.4.3"
    const val HILT = "2.38.1"
    const val LIFECYCLE = "2.3.1"
    const val FRAGMENT = "1.3.6"
    const val WORK_MANAGER = "2.6.0"
    const val GLIDE_VERSION = "4.12.0"
    const val RETROFIT = "2.9.0"
    const val OKHTTP = "4.9.1"
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
    const val ANDROIDX_ANNOTATION = "androidx.annotation:annotation:1.2.0"
    const val ANDROIDX_APPCOMPAT = "androidx.appcompat:appcompat:1.3.1"
    const val ANDROIDX_BROWSER = "androidx.browser:browser:1.3.0"
    const val ANDROIDX_CARDVIEW = "androidx.cardview:cardview:1.0.0"
    const val ANDROIDX_CONSTRAINT_LAYOUT = "androidx.constraintlayout:constraintlayout:2.1.0"
    const val ANDROIDX_DESIGN = "com.google.android.material:material:1.4.0"
    const val ANDROIDX_EXIF = "androidx.exifinterface:exifinterface:1.3.3"
    const val ANDROIDX_FRAGMENT = "androidx.fragment:fragment:${Versions.FRAGMENT}"
    const val FRAGMENT_KTX = "androidx.fragment:fragment-ktx:${Versions.FRAGMENT}"
    const val ANDROIDX_PALETTE = "androidx.palette:palette:1.0.0"
    const val ANDROIDX_PERCENT = "androidx.percentlayout:percentlayout:1.0.0"
    const val ANDROIDX_RECYCLERVIEW = "androidx.recyclerview:recyclerview:1.2.1"
    const val ANDROIDX_VECTOR = "androidx.vectordrawable:vectordrawable:1.1.0"
    const val ANDROIDX_SWIPE_REFRESH_LAYOUT = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
    const val ANDROIDX_CORE_TESTING = "androidx.arch.core:core-testing:2.1.0"
    const val ANDROIDX_WORK_MANAGER = "androidx.work:work-runtime:${Versions.WORK_MANAGER}"
    const val ANDROIDX_WORK_MANAGER_KTX = "androidx.work:work-runtime-ktx:${Versions.WORK_MANAGER}"

    /* Firebase */
    const val FIREBASE_BOM = "com.google.firebase:firebase-bom:29.3.0"
    const val FIREBASE_ANALYTICS = "com.google.firebase:firebase-analytics"
    const val FIREBASE_CRASHLYTICS = "com.google.firebase:firebase-crashlytics"
    const val FIREBASE_MESSAGING = "com.google.firebase:firebase-messaging"
    const val FIREBASE_CORE = "com.google.firebase:firebase-core"
    const val FIREBASE_CONFIG = "com.google.firebase:firebase-config"
    const val FIREBASE_CRASHLYTICS_NDK = "com.google.firebase:firebase-crashlytics-ndk"

    /* Play Services */
    const val PLAY_SERVICES_ANALYTICS = "com.google.android.gms:play-services-analytics:18.0.1"
    const val PLAY_CORE = "com.google.android.play:core:${Versions.PLAY_CORE}"
    const val PLAY_CORE_KTX = "com.google.android.play:core-ktx:${Versions.PLAY_CORE_KTX}"
    const val FLEXBOX_LAYOUT = "com.google.android.flexbox:flexbox:3.0.0"

    /* Mobius */
    const val MOBIUS_CORE = "com.spotify.mobius:mobius-core:${Versions.MOBIUS}"
    const val MOBIUS_TEST = "com.spotify.mobius:mobius-test:${Versions.MOBIUS}"
    const val MOBIUS_ANDROID = "com.spotify.mobius:mobius-android:${Versions.MOBIUS}"
    const val MOBIUS_EXTRAS = "com.spotify.mobius:mobius-extras:${Versions.MOBIUS}"

    /* Testing */
    const val JUNIT = "junit:junit:${Versions.JUNIT}"
    const val ROBOLECTRIC = "org.robolectric:robolectric:${Versions.ROBOLECTRIC}"
    const val ANDROIDX_TEST_JUNIT = "androidx.test.ext:junit:1.1.3"
    const val MOCKK = "io.mockk:mockk:1.12.3"
    const val THREETEN_BP = "org.threeten:threetenbp:1.3.8"
    const val UI_AUTOMATOR = "com.android.support.test.uiautomator:uiautomator-v18:2.1.3"
    const val TEST_ORCHESTRATOR = "androidx.test:orchestrator:1.3.0" // Newer version needs AGP 4.2+

    /* Qr Code (zxing) */
    const val JOURNEY_ZXING = "com.journeyapps:zxing-android-embedded:4.3.0"

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

    /* Media and content handling */
    const val PSPDFKIT = "com.pspdfkit:pspdfkit:${Versions.PSPDFKIT}"
    const val EXOPLAYER = "com.google.android.exoplayer:exoplayer:2.17.1"
    const val PHOTO_VIEW = "com.github.chrisbanes:PhotoView:${Versions.PHOTO_VIEW}"
    const val ANDROID_SVG = "com.caverock:androidsvg:1.4"
    const val RICH_EDITOR = "jp.wasabeef:richeditor-android:2.0.0"
    const val GLIDE = "com.github.bumptech.glide:glide:${Versions.GLIDE_VERSION}"
    const val GLIDE_OKHTTP = "com.github.bumptech.glide:okhttp3-integration:${Versions.GLIDE_VERSION}"
    const val GLIDE_COMPILER = "com.github.bumptech.glide:compiler:${Versions.GLIDE_VERSION}"
    const val SCALE_IMAGE_VIEW = "com.davemorrissey.labs:subsampling-scale-image-view:3.9.0"

    /* Network */
    const val RETROFIT = "com.squareup.retrofit2:retrofit:${Versions.RETROFIT}"
    const val GSON = "com.google.code.gson:gson:2.8.8"
    const val RETROFIT_GSON_ADAPTER = "com.squareup.retrofit2:converter-gson:${Versions.RETROFIT}"
    const val RETROFIT_XML_ADAPTER = "com.squareup.retrofit2:converter-simplexml:${Versions.RETROFIT}"
    const val RETROFIT_SCALAR_CONVERTER = "com.squareup.retrofit2:converter-scalars:${Versions.RETROFIT}"
    const val OKHTTP = "com.squareup.okhttp3:okhttp:${Versions.OKHTTP}"
    const val OKHTTP_LOGGING = "com.squareup.okhttp3:logging-interceptor:${Versions.OKHTTP}"
    const val OKHTTP_URL_CONNECTION = "com.squareup.okhttp3:okhttp-urlconnection:${Versions.OKHTTP}"
    const val OKIO = "com.squareup.okio:okio:2.10.0"

    /* Other */
    const val LOTTIE = "com.airbnb.android:lottie:4.1.0"
    const val SLIDING_UP_PANEL = "com.sothree.slidinguppanel:library:3.3.1"
    const val SQLDELIGHT = "com.squareup.sqldelight:android-driver:1.4.3"
    const val DISK_LRU_CACHE = "com.jakewharton:disklrucache:2.0.2"
    const val EVENTBUS = "org.greenrobot:eventbus:3.2.0"
    const val JW_THREETEN_BP = "com.jakewharton.threetenabp:threetenabp:1.1.2"
    const val PROCESS_PHOENIX = "com.jakewharton:process-phoenix:2.0.0"
    const val PAPERDB = "io.github.pilgr:paperdb:2.7.1"
    const val KEYBOARD_VISIBILITY_LISTENER = "net.yslibrary.keyboardvisibilityevent:keyboardvisibilityevent:2.2.1"
    const val APACHE_COMMONS_TEXT = "org.apache.commons:commons-text:1.6"
    const val WONDERKILN_CAMERA_KIT = "com.github.CameraKit:camerakit-android:v0.13.4"
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
