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



package support

import org.junit.Test

class BitriseAppTest {

    @Test
    fun appsFetchedSuccessfully() {
        BitriseApp.Android.parent
        BitriseApp.Android.polling
        BitriseApp.Android.student
        BitriseApp.Android.teacher

        BitriseApp.Android.OpenSource.parent
        BitriseApp.Android.OpenSource.student
        BitriseApp.Android.OpenSource.teacher

        BitriseApp.Android.Automation.teacherEspresso
        BitriseApp.Android.Automation.teacherFlank
        BitriseApp.Android.Automation.teacherRobo
        BitriseApp.Android.Automation.parentRobo
        BitriseApp.Android.Automation.translations

        BitriseApp.iOS.parent
        BitriseApp.iOS.student
        BitriseApp.iOS.teacher

        BitriseApp.iOS.Automation.jest
        BitriseApp.iOS.Automation.translations

        BitriseApp.Automation.cloudBuildMetrics
        BitriseApp.Automation.dataSeedingApi
        BitriseApp.Automation.mobileQa
        BitriseApp.Automation.mobileQaNightly
        BitriseApp.Automation.testAdvisoryBoard

        BitriseApp.Practice.mobile
        BitriseApp.Practice.mobileDummy
    }
}
