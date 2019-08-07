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
package com.instructure.student.ui.utils

import android.app.Activity
import com.instructure.canvas.espresso.CanvasTest
import com.instructure.espresso.InstructureActivityTestRule
import com.instructure.student.BuildConfig
import com.instructure.student.activity.LoginActivity
import com.instructure.student.ui.pages.*

abstract class StudentTest : CanvasTest() {

    override val activityRule: InstructureActivityTestRule<out Activity> =
        StudentActivityTestRule(LoginActivity::class.java)

    override val isTesting = BuildConfig.IS_TESTING

    /**
     * Required for auto complete of page objects within tests
     */
    val assignmentListPage = AssignmentListPage()
    val dashboardPage = DashboardPage()
    val calendarPage = CalendarPage()
    val todoPage = TodoPage()
    val loginFindSchoolPage = LoginFindSchoolPage()
    val loginLandingPage = LoginLandingPage()
    val loginSignInPage = LoginSignInPage()
    val courseBrowserPage = CourseBrowserPage()
    val assignmentDetailsPage = AssignmentDetailsPage()
}
