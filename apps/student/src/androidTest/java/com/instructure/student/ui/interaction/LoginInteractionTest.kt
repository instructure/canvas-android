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

import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.student.R
import com.instructure.student.ui.utils.StudentTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class LoginInteractionTest : StudentTest() {

    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.LOGIN, TestCategory.INTERACTION)
    fun testLogin_canFindSchool() {
        loginLandingPage.clickFindMySchoolButton()
        loginFindSchoolPage.assertPageObjects()

        if(isTabletDevice()) loginFindSchoolPage.assertHintText(R.string.schoolInstructureCom)
        else loginFindSchoolPage.assertHintText(R.string.loginHint)

        loginFindSchoolPage.enterDomain("harv")
        loginFindSchoolPage.assertSchoolSearchResults("City Harvest Church (Singapore)")
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.LOGIN, TestCategory.INTERACTION)
    fun testLogin_qrTutorialPageLoads() {
        // Should be able to view and assert page objects on the QR tutorial page
        loginLandingPage.clickQRCodeButton()
        qrLoginPage.assertPageObjects()
        qrLoginPage.clickForA11y()
    }
}
