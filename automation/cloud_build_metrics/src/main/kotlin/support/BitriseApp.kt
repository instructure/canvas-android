//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//



@file:Suppress("ClassName")

package support

import api.bitrise.BitriseAppObject
import api.bitrise.BitriseApps
import api.cloud.GenericApp
import normal.NormalApp

/** Helper object that finds apps on Bitrise and verifies titles **/
object BitriseApp : GenericApp {
    override val apps: List<BitriseAppObject> by lazy {
        BitriseApps.getAppsForOrg()
    }

    override val normalApps: List<NormalApp> by lazy {
        apps.map { it.toNormalApp() }
    }

    override val pullRequestApps by lazy {
        listOf(
                Android.teacher,
                Automation.dataSeedingApi,
                Android.parent,
                Android.student,
                Android.Automation.teacherEspresso,
                Android.polling
        )
    }

    private fun get(appName: String, id: String): BitriseAppObject {
        return getGeneric(appName, id)
    }

    // TODO: Auto generate the apps from the API & auto generate the unit tests.
    object Android {
        val parent by lazy { get("android-parent", "444f0fbcf3593f0d") }
        val polling by lazy { get("android-polling", "f36c5318b38ba3d8") }
        val student by lazy { get("android-student", "c94e0393a4d8cc19") }
        val teacher by lazy { get("android-teacher", "18a96964420643c0") }

        object OpenSource {
            val parent by lazy { get("Android Open Source Parent", "3875692b192c4eb3") }
            val student by lazy { get("Android Open Source Student", "43a4503585e48423") }
            val teacher by lazy { get("Android Open Source Teacher", "758ed6bba9746be5") }
        }

        object Automation {
            val teacherEspresso by lazy { get("Android Teacher Espresso", "cd43401ce6a38048") }
            val teacherFlank by lazy { get("Android Teacher Flank", "0c02545cdba74514") }
            val teacherRobo by lazy { get("android-teacher-robo", "70a43db684ec9e4f") }
            val parentRobo by lazy { get("android-parent-robo", "5473eb1f7875e0cf") }
            val translations by lazy { get("android-translations", "d5f245ba69e1567f") }
        }
    }

    object iOS {
        val parent by lazy { get("iOS Parent", "bd385a53a16abb46") }
        val student by lazy { get("iOS Student", "88a3641e29e4745c") }
        val teacher by lazy { get("iOS Teacher", "13a8648ad7201c0d") }

        object Automation {
            val jest by lazy { get("iOS Teacher - BB - Jest", "72a41ea6e1d3f173") }
            val translations by lazy { get("iOS Translations", "8ddaa31e1baf48dc") }
        }
    }

    object Automation {
        val cloudBuildMetrics by lazy { get("cloud_build_metrics", "d7d5e0d1dc2f33b8") }
        val dataSeedingApi by lazy { get("data seeding api", "2da00d17df4642d6") }
        val mobileQa by lazy { get("mobile_qa", "58159e359f1ecda7") }
        val mobileQaNightly by lazy { get("mobile_qa_nightly_beta", "96ce0c796755fe20") }
        val testAdvisoryBoard by lazy { get("test_advisory_board", "d528939eac6fe1db") }
        val testJobForCloudBuildMetrics by lazy { get("Test Job for Cloud Build Metrics", "4c2f997f51ab7b62") }
    }

    object Practice {
        val mobile by lazy { get("px-mobile-app", "a4dbdddbff8736d8") }
        val mobileDummy by lazy { get("px-mobile-app (dummy)", "8ae8891599ebbb4e") }
    }
}
