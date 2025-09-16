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
import com.instructure.canvas.espresso.common.pages.AssignmentReminderPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarEventCreateEditPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarEventDetailsPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarFilterPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarScreenPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarToDoCreateUpdatePage
import com.instructure.canvas.espresso.common.pages.compose.CalendarToDoDetailsPage
import com.instructure.canvas.espresso.common.pages.compose.GradesPage
import com.instructure.canvas.espresso.common.pages.compose.InboxComposePage
import com.instructure.canvas.espresso.common.pages.compose.InboxDetailsPage
import com.instructure.canvas.espresso.common.pages.compose.InboxSignatureSettingsPage
import com.instructure.canvas.espresso.common.pages.compose.RecipientPickerPage
import com.instructure.canvas.espresso.common.pages.compose.SettingsPage
import com.instructure.parentapp.features.login.LoginActivity
import com.instructure.parentapp.ui.pages.compose.AddStudentBottomPage
import com.instructure.parentapp.ui.pages.compose.AlertsPage
import com.instructure.parentapp.ui.pages.compose.AnnouncementDetailsPage
import com.instructure.parentapp.ui.pages.compose.CourseDetailsPage
import com.instructure.parentapp.ui.pages.compose.CoursesPage
import com.instructure.parentapp.ui.pages.compose.CreateAccountPage
import com.instructure.parentapp.ui.pages.compose.ManageStudentsPage
import com.instructure.parentapp.ui.pages.compose.NotAParentPage
import com.instructure.parentapp.ui.pages.compose.PairingCodePage
import com.instructure.parentapp.ui.pages.compose.ParentInboxCoursePickerPage
import com.instructure.parentapp.ui.pages.compose.QrPairingPage
import com.instructure.parentapp.ui.pages.compose.StudentAlertSettingsPage
import com.instructure.parentapp.ui.pages.compose.SummaryPage
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
    protected val gradesPage = GradesPage(composeTestRule)
    protected val calendarScreenPage = CalendarScreenPage(composeTestRule)
    protected val calendarEventDetailsPage = CalendarEventDetailsPage(composeTestRule)
    protected val calendarEventCreateEditPage = CalendarEventCreateEditPage(composeTestRule)
    protected val calendarToDoCreateUpdatePage = CalendarToDoCreateUpdatePage(composeTestRule)
    protected val calendarToDoDetailsPage = CalendarToDoDetailsPage(composeTestRule)
    protected val calendarFilterPage = CalendarFilterPage(composeTestRule)
    protected val assignmentReminderPage = AssignmentReminderPage(composeTestRule)
    protected val inboxSignatureSettingsPage = InboxSignatureSettingsPage(composeTestRule)

    override fun displaysPageObjects() = Unit
}
