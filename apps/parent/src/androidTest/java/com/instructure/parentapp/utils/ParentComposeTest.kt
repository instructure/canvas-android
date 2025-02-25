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

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.instructure.canvas.espresso.common.pages.compose.InboxComposePage
import com.instructure.canvas.espresso.common.pages.compose.InboxDetailsPage
import com.instructure.canvas.espresso.common.pages.compose.RecipientPickerPage
import com.instructure.canvas.espresso.common.pages.compose.SettingsPage
import com.instructure.parentapp.features.login.LoginActivity
import com.instructure.parentapp.ui.pages.AddStudentBottomPage
import com.instructure.parentapp.ui.pages.AlertsPage
import com.instructure.parentapp.ui.pages.AnnouncementDetailsPage
import com.instructure.parentapp.ui.pages.CourseDetailsPage
import com.instructure.parentapp.ui.pages.CoursesPage
import com.instructure.parentapp.ui.pages.CreateAccountPage
import com.instructure.parentapp.ui.pages.ManageStudentsPage
import com.instructure.parentapp.ui.pages.PairingCodePage
import com.instructure.parentapp.ui.pages.ParentInboxCoursePickerPage
import com.instructure.parentapp.ui.pages.QrPairingPage
import com.instructure.parentapp.ui.pages.StudentAlertSettingsPage
import com.instructure.parentapp.ui.pages.SummaryPage
import com.instructure.parentapp.ui.pages.compose.NotAParentPage
import org.junit.Rule


abstract class ParentComposeTest : ParentTest() {

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<LoginActivity>()

    //Compose pages
    protected val alertsPage = AlertsPage(composeTestRule)
    protected val manageStudentsPage = ManageStudentsPage(composeTestRule)
    protected val studentAlertSettingsPage = StudentAlertSettingsPage(composeTestRule)
    protected val addStudentBottomPage = AddStudentBottomPage(composeTestRule)
    protected val pairingCodePage = PairingCodePage(composeTestRule)
    protected val qrPairingPage = QrPairingPage(composeTestRule)
    protected val coursesPage = CoursesPage(composeTestRule)
    protected val notAParentPage = NotAParentPage(composeTestRule)
    protected val courseDetailsPage = CourseDetailsPage(composeTestRule)
    protected val summaryPage = SummaryPage(composeTestRule)
    protected val announcementDetailsPage = AnnouncementDetailsPage(composeTestRule)
    protected val createAccountPage = CreateAccountPage(composeTestRule)
    protected val inboxDetailsPage = InboxDetailsPage(composeTestRule)
    protected val inboxComposeMessagePage = InboxComposePage(composeTestRule)
    protected val inboxRecipientPickerPage = RecipientPickerPage(composeTestRule)
    protected val inboxCoursePickerPage = ParentInboxCoursePickerPage(composeTestRule)
    protected val settingsPage = SettingsPage(composeTestRule)

    override fun displaysPageObjects() = Unit
}
