/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.ui.utils

import android.app.Activity
import com.instructure.canvas.espresso.CanvasTest
import com.instructure.espresso.InstructureActivityTestRule
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.activities.LoginActivity
import com.instructure.teacher.ui.pages.*

abstract class TeacherTest : CanvasTest() {

    override val activityRule: InstructureActivityTestRule<out Activity>
            = TeacherActivityTestRule(LoginActivity::class.java)

    override val isTesting = BuildConfig.IS_TESTING

    /**
     * Required for auto complete of page objects within tests
     */
    val coursesListPage = CoursesListPage()
    val allCoursesListPage = AllCoursesListPage()
    val assignmentListPage = AssignmentListPage()
    val assignmentSubmissionListPage = AssignmentSubmissionListPage()
    val assignmentDetailsPage = AssignmentDetailsPage()
    val assignmentDueDatesPage = AssignmentDueDatesPage()
    val courseBrowserPage = CourseBrowserPage()
    val editCoursesListPage = EditCoursesListPage()
    val courseSettingsPage = CourseSettingsPage()
    val editAssignmentDetailsPage = EditAssignmentDetailsPage()
    val assigneeListPage = AssigneeListPage()
    val loginLandingPage = LoginLandingPage()
    val loginFindSchoolPage = LoginFindSchoolPage()
    val loginSignInPage = LoginSignInPage()
    val notATeacherPage = NotATeacherPage()
    val inboxPage = InboxPage()
    val navDrawerPage = NavDrawerPage()
    val speedGraderPage = SpeedGraderPage()
    val speedGraderGradePage = SpeedGraderGradePage()
    val speedGraderCommentsPage = SpeedGraderCommentsPage()
    val speedGraderFilesPage = SpeedGraderFilesPage()
    val quizListPage = QuizListPage()
    val quizDetailsPage = QuizDetailsPage()
    val editQuizDetailsPage = EditQuizDetailsPage()
    val discussionsListPage = DiscussionsListPage()
    val quizSubmissionListPage = QuizSubmissionListPage()
    val inboxMessagePage = InboxMessagePage()
    val addMessagePage = AddMessagePage()
    val chooseRecipientsPage = ChooseRecipientsPage()
    val speedGraderQuizSubmissionPage = SpeedGraderQuizSubmissionPage()
    val webViewLoginPage = WebViewLoginPage()
    val announcementsListPage = AnnouncementsListPage()
    val peopleListPage = PeopleListPage()
    val studentContextPage = StudentContextPage()
    val pageListPage = PageListPage()
    val syllabusPage = SyllabusPage()
    val calendarEventPage = CalendarEventPage()
    val dashboardPage = DashboardPage()
    val editSyllabusPage = EditSyllabusPage()

}
