/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
package com.instructure.parentapp.ui.utils

import com.instructure.canvas.espresso.CanvasTest
import com.instructure.parentapp.BuildConfig
import com.instructure.parentapp.activity.LoginActivity
import com.instructure.parentapp.ui.pages.*

abstract class ParentTest : CanvasTest() {

    override val activityRule = ParentActivityTestRule(LoginActivity::class.java)

    override val isTesting = BuildConfig.IS_TESTING

    /**
     * Required for auto complete of page objects within tests
     */
    val helpPage = HelpPage()
    val legalPage = LegalPage()
    val loginLandingPage = LoginLandingPage()
    val loginFindSchoolPage = LoginFindSchoolPage()
    val loginSignInPage = LoginSignInPage()
    val navDrawerPage = NavDrawerPage()
    val viewStudentPage = ViewStudentPage()

}
