/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
 *
 */
repositories {
    google()
    mavenCentral()
}

val agpVersion= "8.6.1"
val kotlinVersion = "2.0.21"

dependencies {
    implementation("com.android.tools.build:gradle:$agpVersion")
    implementation("com.android.tools.build:gradle-api:$agpVersion")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:$kotlinVersion")
}

plugins {
    `kotlin-dsl`
}