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
    jcenter()
}

val agpVersion= "4.0.2"

dependencies {
    implementation("com.android.tools.build:gradle:$agpVersion")
    implementation("com.android.tools.build:gradle-api:$agpVersion")
    implementation("org.javassist:javassist:3.24.1-GA")
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.ow2.asm" && requested.name == "asm") {
            useVersion("7.0")
            because("Build fails otherwise when using Robolectric 4")
        }
    }
}

plugins {
    `kotlin-dsl`
}
