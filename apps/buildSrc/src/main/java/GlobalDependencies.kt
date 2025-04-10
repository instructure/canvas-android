@file:Suppress("unused")

object Versions {
    /* SDK Versions */
    const val COMPILE_SDK = 34
    const val MIN_SDK = 26
    const val TARGET_SDK = 34

    /* Build/tooling */
    const val ANDROID_GRADLE_TOOLS = "8.6.1"
    const val BUILD_TOOLS = "34.0.0"

    /* Testing */
    const val JUNIT = "4.13.2"
    const val ROBOLECTRIC = "4.13"
    const val JACOCO_ANDROID = "0.1.5"

    /* Kotlin */
    const val KOTLIN = "2.0.21"
    const val KOTLIN_COROUTINES = "1.9.0"

    /* Google, Play Services */
    const val GOOGLE_SERVICES = "4.4.2"

    /* Others */
    const val APOLLO = "4.1.1"
    const val PSPDFKIT = "2024.3.1"
    const val PHOTO_VIEW = "2.3.0"
    const val MOBIUS = "1.2.1"
    const val HILT = "2.52"
    const val HILT_ANDROIDX = "1.2.0"
    const val LIFECYCLE = "2.8.6"
    const val FRAGMENT = "1.8.4"
    const val WORK_MANAGER = "2.9.1"
    const val GLIDE_VERSION = "4.16.0"
    const val RETROFIT = "2.11.0"
    const val OKHTTP = "4.12.0"
    const val ROOM = "2.6.1"
    const val HAMCREST = "2.2"
    const val NAVIGATION = "2.8.3"
    const val MEDIA3 = "1.4.1"
    const val DATASTORE = "1.1.1"
    const val LOTTIE = "6.5.2"
    const val ENCRYPTED_SHARED_PREFERENCES = "1.0.0"
}

object Libs {
    /* Kotlin */
    const val KOTLIN_STD_LIB = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.KOTLIN}"
    const val KOTLIN_COROUTINES_CORE = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.KOTLIN_COROUTINES}"
    const val KOTLIN_COROUTINES_ANDROID = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.KOTLIN_COROUTINES}"
    const val KOTLIN_COROUTINES_TEST = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.KOTLIN_COROUTINES}"

    /* Apollo/GraphQL */
    const val APOLLO_RUNTIME = "com.apollographql.apollo:apollo-runtime:${Versions.APOLLO}"
    const val APOLLO_API = "com.apollographql.apollo:apollo-api:${Versions.APOLLO}"
    const val APOLLO_HTTP_CACHE = "com.apollographql.apollo:apollo-http-cache:${Versions.APOLLO}"

    /* Androidx libraries */
    const val ANDROIDX_ANNOTATION = "androidx.annotation:annotation:1.9.0"
    const val ANDROIDX_APPCOMPAT = "androidx.appcompat:appcompat:1.7.0"
    const val ANDROIDX_BROWSER = "androidx.browser:browser:1.8.0"
    const val ANDROIDX_CARDVIEW = "androidx.cardview:cardview:1.0.0"
    const val ANDROIDX_CONSTRAINT_LAYOUT = "androidx.constraintlayout:constraintlayout:2.1.4"
    const val ANDROIDX_EXIF = "androidx.exifinterface:exifinterface:1.3.7"
    const val ANDROIDX_FRAGMENT = "androidx.fragment:fragment:${Versions.FRAGMENT}"
    const val ANDROIDX_FRAGMENT_KTX = "androidx.fragment:fragment-ktx:${Versions.FRAGMENT}"
    const val ANDROIDX_PALETTE = "androidx.palette:palette:1.0.0"
    const val ANDROIDX_PERCENT = "androidx.percentlayout:percentlayout:1.0.0"
    const val ANDROIDX_RECYCLERVIEW = "androidx.recyclerview:recyclerview:1.3.2"
    const val ANDROIDX_VECTOR = "androidx.vectordrawable:vectordrawable:1.2.0"
    const val ANDROIDX_SWIPE_REFRESH_LAYOUT = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
    const val ANDROIDX_CORE_TESTING = "androidx.arch.core:core-testing:2.2.0"
    const val ANDROIDX_WORK_MANAGER = "androidx.work:work-runtime:${Versions.WORK_MANAGER}"
    const val ANDROIDX_WORK_MANAGER_KTX = "androidx.work:work-runtime-ktx:${Versions.WORK_MANAGER}"
    const val ANDROIDX_WEBKIT = "androidx.webkit:webkit:1.9.0"
    const val ANDROIDX_DATABINDING_COMPILER = "androidx.databinding:databinding-compiler:${Versions.ANDROID_GRADLE_TOOLS}" // This is bundled with the gradle plugin so we use the same version
    const val ANDROIDX_COMPOSE_ACTIVITY = "androidx.activity:activity-compose:1.9.0"
    const val DATASTORE = "androidx.datastore:datastore-preferences:${Versions.DATASTORE}"
    const val ENCRYPTED_SHARED_PREFERENCES = "androidx.security:security-crypto:${Versions.ENCRYPTED_SHARED_PREFERENCES}"

    /* Firebase */
    const val FIREBASE_BOM = "com.google.firebase:firebase-bom:33.4.0"
    const val FIREBASE_CRASHLYTICS = "com.google.firebase:firebase-crashlytics"
    const val FIREBASE_MESSAGING = "com.google.firebase:firebase-messaging"
    const val FIREBASE_CONFIG = "com.google.firebase:firebase-config"
    const val FIREBASE_CRASHLYTICS_NDK = "com.google.firebase:firebase-crashlytics-ndk"

    /* Google Dependencies */
    const val PLAY_IN_APP_UPDATES = "com.google.android.play:app-update:2.1.0"
    const val FLEXBOX_LAYOUT = "com.google.android.flexbox:flexbox:3.0.0"
    const val MATERIAL_DESIGN = "com.google.android.material:material:1.12.0"

    /* Mobius */
    const val MOBIUS_CORE = "com.spotify.mobius:mobius-core:${Versions.MOBIUS}"
    const val MOBIUS_TEST = "com.spotify.mobius:mobius-test:${Versions.MOBIUS}"
    const val MOBIUS_ANDROID = "com.spotify.mobius:mobius-android:${Versions.MOBIUS}"
    const val MOBIUS_EXTRAS = "com.spotify.mobius:mobius-extras:${Versions.MOBIUS}"

    /* Testing */
    const val JUNIT = "junit:junit:${Versions.JUNIT}"
    const val ROBOLECTRIC = "org.robolectric:robolectric:${Versions.ROBOLECTRIC}"
    const val ANDROIDX_TEST_JUNIT = "androidx.test.ext:junit:1.2.1"
    const val MOCKK = "io.mockk:mockk:1.13.13"
    const val THREETEN_BP = "org.threeten:threetenbp:1.7.0"
    const val UI_AUTOMATOR = "androidx.test.uiautomator:uiautomator:2.3.0"
    const val TEST_ORCHESTRATOR = "androidx.test:orchestrator:1.5.1"

    /* Qr Code (zxing) */
    const val JOURNEY_ZXING = "com.journeyapps:zxing-android-embedded:4.3.0"
    const val JOURNEY_ZXING_CORE = "com.google.zxing:core:3.5.2"

    /* Dependency Inejction */
    const val HILT = "com.google.dagger:hilt-android:${Versions.HILT}"
    const val HILT_COMPILER = "com.google.dagger:hilt-android-compiler:${Versions.HILT}"
    const val HILT_TESTING = "com.google.dagger:hilt-android-testing:${Versions.HILT}"
    const val HILT_TESTING_COMPILER = "com.google.dagger:hilt-android-compiler:${Versions.HILT}"
    const val HILT_ANDROIDX_COMPILER = "androidx.hilt:hilt-compiler:${Versions.HILT_ANDROIDX}"
    const val HILT_ANDROIDX_WORK = "androidx.hilt:hilt-work:${Versions.HILT_ANDROIDX}"
    const val HILT_COMPOSE_NAVIGATION = "androidx.hilt:hilt-navigation-compose:${Versions.HILT_ANDROIDX}"

    /* Android Architecture Components */
    const val VIEW_MODEL = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.LIFECYCLE}"
    const val LIVE_DATA = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.LIFECYCLE}"
    const val VIEW_MODE_SAVED_STATE = "androidx.lifecycle:lifecycle-viewmodel-savedstate:${Versions.LIFECYCLE}"
    const val LIFECYCLE_COMPILER = "androidx.lifecycle:lifecycle-compiler:${Versions.LIFECYCLE}"
    const val COMPOSE_VIEW_MODEL = "androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.LIFECYCLE}"

    /* Media and content handling */
    const val PSPDFKIT = "com.pspdfkit:pspdfkit:${Versions.PSPDFKIT}"
    const val MEDIA3 = "androidx.media3:media3-exoplayer:${Versions.MEDIA3}"
    const val MEDIA3_UI = "androidx.media3:media3-ui:${Versions.MEDIA3}"
    const val MEDIA3_HLS = "androidx.media3:media3-exoplayer-hls:${Versions.MEDIA3}"
    const val MEDIA3_DASH = "androidx.media3:media3-exoplayer-dash:${Versions.MEDIA3}"
    const val MEDIA3_SMOOTH_STREAMING = "androidx.media3:media3-exoplayer-smoothstreaming:${Versions.MEDIA3}"
    const val PHOTO_VIEW = "com.github.chrisbanes:PhotoView:${Versions.PHOTO_VIEW}"
    const val ANDROID_SVG = "com.caverock:androidsvg:1.4"
    const val RICH_EDITOR = "jp.wasabeef:richeditor-android:2.0.0"
    const val GLIDE = "com.github.bumptech.glide:glide:${Versions.GLIDE_VERSION}"
    const val GLIDE_OKHTTP = "com.github.bumptech.glide:okhttp3-integration:${Versions.GLIDE_VERSION}"
    const val GLIDE_COMPILER = "com.github.bumptech.glide:compiler:${Versions.GLIDE_VERSION}"
    const val GLIDE_COMPOSE = "com.github.bumptech.glide:compose:1.0.0-beta01"
    const val SCALE_IMAGE_VIEW = "com.davemorrissey.labs:subsampling-scale-image-view:3.10.0"

    /* Network */
    const val RETROFIT = "com.squareup.retrofit2:retrofit:${Versions.RETROFIT}"
    const val GSON = "com.google.code.gson:gson:2.10.1"
    const val RETROFIT_GSON_ADAPTER = "com.squareup.retrofit2:converter-gson:${Versions.RETROFIT}"
    const val RETROFIT_XML_ADAPTER = "com.squareup.retrofit2:converter-simplexml:${Versions.RETROFIT}"
    const val RETROFIT_SCALAR_CONVERTER = "com.squareup.retrofit2:converter-scalars:${Versions.RETROFIT}"
    const val OKHTTP = "com.squareup.okhttp3:okhttp:${Versions.OKHTTP}"
    const val OKHTTP_LOGGING = "com.squareup.okhttp3:logging-interceptor:${Versions.OKHTTP}"
    const val OKHTTP_URL_CONNECTION = "com.squareup.okhttp3:okhttp-urlconnection:${Versions.OKHTTP}"
    const val OKIO = "com.squareup.okio:okio:3.9.1"

    /* Other */
    const val LOTTIE = "com.airbnb.android:lottie:${Versions.LOTTIE}"
    const val LOTTIE_COMPOSE = "com.airbnb.android:lottie-compose:${Versions.LOTTIE}"
    const val SLIDING_UP_PANEL = "com.sothree.slidinguppanel:library:3.3.1"
    const val DISK_LRU_CACHE = "com.jakewharton:disklrucache:2.0.2"
    const val EVENTBUS = "org.greenrobot:eventbus:3.3.1"
    const val JW_THREETEN_BP = "com.jakewharton.threetenabp:threetenabp:1.4.6"
    const val PROCESS_PHOENIX = "com.jakewharton:process-phoenix:2.1.2"
    const val KEYBOARD_VISIBILITY_LISTENER = "net.yslibrary.keyboardvisibilityevent:keyboardvisibilityevent:2.2.1"
    const val APACHE_COMMONS_TEXT = "org.apache.commons:commons-text:1.12.0"
    const val CAMERA_VIEW = "com.otaliastudios:cameraview:2.7.2"

    const val PENDO = "sdk.pendo.io:pendoIO:3.6+"

    const val ROOM = "androidx.room:room-runtime:${Versions.ROOM}"
    const val ROOM_COMPILER = "androidx.room:room-compiler:${Versions.ROOM}"
    const val ROOM_COROUTINES = "androidx.room:room-ktx:${Versions.ROOM}"
    const val ROOM_TEST = "androidx.room:room-testing:${Versions.ROOM}"

    const val HAMCREST = "org.hamcrest:hamcrest:${Versions.HAMCREST}"

    const val RRULE = "org.scala-saddle:google-rfc-2445:20110304"

    // Compose
    const val COMPOSE_BOM = "androidx.compose:compose-bom:2024.09.02"
    const val COMPOSE_MATERIAL = "androidx.compose.material:material"
    const val COMPOSE_MATERIAL_ICONS = "androidx.compose.material:material-icons-core"
    const val COMPOSE_PREVIEW = "androidx.compose.ui:ui-tooling-preview"
    const val COMPOSE_TOOLING = "androidx.compose.ui:ui-tooling"
    const val COMPOSE_UI = "androidx.compose.ui:ui-android"
    const val COMPOSE_UI_TEST = "androidx.compose.ui:ui-test-junit4"
    const val COMPOSE_UI_TEST_MANIFEST = "androidx.compose.ui:ui-test-manifest"
    const val COMPOSE_MATERIAL_3 = "androidx.compose.material3:material3"

    // Navigation
    const val NAVIGATION_FRAGMENT = "androidx.navigation:navigation-fragment-ktx:${Versions.NAVIGATION}"
    const val NAVIGATION_KTX = "androidx.navigation:navigation-ui-ktx:${Versions.NAVIGATION}"
    const val NAVIGATION_COMPOSE = "androidx.navigation:navigation-compose:${Versions.NAVIGATION}"

    const val KOTLIN_SERIALIZABLE = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0"
}

object Plugins {
    // We should upgrade this to 3.0.0, but it requires AGP 8.1+, which we are currently not supporting due to the ProjectTransformers
    const val FIREBASE_CRASHLYTICS =  "com.google.firebase:firebase-crashlytics-gradle:2.9.2"
    const val ANDROID_GRADLE_TOOLS = "com.android.tools.build:gradle:${Versions.ANDROID_GRADLE_TOOLS}"
    const val APOLLO = "com.apollographql.apollo:apollo-gradle-plugin:${Versions.APOLLO}"
    const val KOTLIN = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.KOTLIN}"
    const val GOOGLE_SERVICES = "com.google.gms:google-services:${Versions.GOOGLE_SERVICES}"
    const val JACOCO_ANDROID = "com.dicedmelon.gradle:jacoco-android:${Versions.JACOCO_ANDROID}"
    const val HILT = "com.google.dagger:hilt-android-gradle-plugin:${Versions.HILT}"
}
