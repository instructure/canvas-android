plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    kotlin("plugin.serialization") version "2.1.20"
    id("jacoco")
}

android {
    namespace = "com.instructure.horizon"
    compileSdk = Versions.COMPILE_SDK
    buildToolsVersion = Versions.BUILD_TOOLS

    defaultConfig {
        minSdk = Versions.MIN_SDK

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
    ksp(Libs.HILT_COMPILER)
    implementation(Libs.HILT_ANDROIDX_WORK)
    ksp(Libs.HILT_ANDROIDX_COMPILER)

    implementation(Libs.NUTRIENT)

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

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/*\$ViewInjector*.*",
        "**/*\$ViewBinder*.*",
        "**/Lambda$*.class",
        "**/Lambda.class",
        "**/*Lambda.class",
        "**/*Lambda*.class",
        "**/*_MembersInjector.class",
        "**/Dagger*Component*.*",
        "**/*Module_*Factory.class",
        "**/di/module/*",
        "**/*_Factory*.*",
        "**/*Module*.*",
        "**/*Dagger*.*",
        "**/*Hilt*.*",
        "**/hilt_aggregated_deps/**",
        "**/*_HiltModules*.*",
        "**/*_ComponentTreeDeps*.*",
        "**/*_Impl*.*",
        "**/*Screen*.*",
        "**/*Ui*.*",
        "**/*Navigation*.*",
        "**/*Activity*.*",
        "**/*Fragment*.*",
        "**/*Composable*.*",
        "**/*Preview*.*",
        "**/horizonui/**",
        "**/model/**",
        "**/navigation/**"
    )

    val debugTree = fileTree("${layout.buildDirectory.get().asFile}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
        include("**/features/**/*ViewModel*.class")
        include("**/features/**/*Repository*.class")
    }

    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(layout.buildDirectory.get().asFile) {
        include("jacoco/testDebugUnitTest.exec")
    })
}