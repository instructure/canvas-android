/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.utils

import com.instructure.canvas.espresso.CanvasTest
import com.instructure.canvas.espresso.common.pages.AboutPage
import com.instructure.canvas.espresso.common.pages.AssignmentDetailsPage
import com.instructure.canvas.espresso.common.pages.InboxPage
import com.instructure.canvas.espresso.common.pages.LegalPage
import com.instructure.canvas.espresso.common.pages.LoginFindSchoolPage
import com.instructure.canvas.espresso.common.pages.LoginLandingPage
import com.instructure.canvas.espresso.common.pages.LoginSignInPage
import com.instructure.espresso.ModuleItemInteractions
import com.instructure.parentapp.BuildConfig
import com.instructure.parentapp.features.login.LoginActivity
import com.instructure.parentapp.ui.pages.DashboardPage
import com.instructure.parentapp.ui.pages.HelpPage
import com.instructure.parentapp.ui.pages.LeftSideNavigationDrawerPage
import com.instructure.parentapp.ui.pages.SyllabusPage


abstract class ParentTest : CanvasTest() {

    override val isTesting = BuildConfig.IS_TESTING

    override val activityRule = ParentActivityTestRule(LoginActivity::class.java)

    //Regular pages (non-compose)
    val dashboardPage = DashboardPage()
    val leftSideNavigationDrawerPage = LeftSideNavigationDrawerPage()
    val helpPage = HelpPage()
    val assignmentDetailsPage = AssignmentDetailsPage(ModuleItemInteractions())
    val syllabusPage = SyllabusPage()

    // Common pages (it's common for all apps)
    val loginLandingPage = LoginLandingPage()
    val loginFindSchoolPage = LoginFindSchoolPage()
    val loginSignInPage = LoginSignInPage()
    val inboxPage = InboxPage()
    val legalPage = LegalPage()
    val aboutPage = AboutPage()
}