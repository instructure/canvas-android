/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
 */
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.Stub
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import org.junit.Test

class SettingsInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, true)
    fun testHelp_askQuestion() {
        // Should open a dialog and send a question for the selected course
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, true)
    fun testHelp_searchCanvasGuides() {
        // Should open the Canvas guides in a WebView
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, true)
    fun testHelp_reportAProblem() {
        // Should send an error report
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, true)
    fun testHelp_submitFeatureIdea() {
        // Should send a pre-filled email intent. Should be addressed to mobilesupport@instructure.com.
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, true)
    fun testHelp_shareYourLove() {
        // Should send an intent to open the listing for Student App in the Play Store
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, true)
    fun testLegal_showOpenSourceLicenses() {
        // Should display a list of open source dependencies used in the app, along with their licenses
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, true)
    fun testLegal_showTermsOfUse() {
        // Should display terms of use in a WebView
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, true)
    fun testLegal_showPrivacyPolicy() {
        // Should display the privacy policy in a WebView
    }
}
