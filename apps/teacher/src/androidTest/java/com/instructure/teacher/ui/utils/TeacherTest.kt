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

import com.instructure.canvas.espresso.CanvasTest
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.activities.LoginActivity
import com.instructure.teacher.ui.pages.*
import org.junit.Before

abstract class TeacherTest : CanvasTest() {

    override val activityRule = TeacherActivityTestRule(LoginActivity::class.java)

    override val isTesting = BuildConfig.IS_TESTING

    /**
     * Required for auto complete of page objects within tests
     */
    var coursesListPage = CoursesListPage()
    var allCoursesListPage = AllCoursesListPage()
    var assignmentListPage = AssignmentListPage()
    var assignmentSubmissionListPage = AssignmentSubmissionListPage()
    var assignmentDetailsPage = AssignmentDetailsPage()
    var assignmentDueDatesPage = AssignmentDueDatesPage()
    var courseBrowserPage = CourseBrowserPage()
    var editCoursesListPage = EditCoursesListPage()
    var courseSettingsPage = CourseSettingsPage()
    var editAssignmentDetailsPage = EditAssignmentDetailsPage()
    var assigneeListPage = AssigneeListPage()
    var loginLandingPage = LoginLandingPage()
    var loginFindSchoolPage = LoginFindSchoolPage()
    var loginSignInPage = LoginSignInPage()
    var notATeacherPage = NotATeacherPage()
    var inboxPage = InboxPage()
    var navDrawerPage = NavDrawerPage()
    var speedGraderPage = SpeedGraderPage()
    var speedGraderGradePage = SpeedGraderGradePage()
    var speedGraderCommentsPage = SpeedGraderCommentsPage()
    var speedGraderFilesPage = SpeedGraderFilesPage()
    var quizListPage = QuizListPage()
    var quizDetailsPage = QuizDetailsPage()
    var editQuizDetailsPage = EditQuizDetailsPage()
    var discussionsListPage = DiscussionsListPage()
    var quizSubmissionListPage = QuizSubmissionListPage()
    var inboxMessagePage = InboxMessagePage()
    var addMessagePage = AddMessagePage()
    var chooseRecipientsPage = ChooseRecipientsPage()
    var speedGraderQuizSubmissionPage = SpeedGraderQuizSubmissionPage()
    var webViewLoginPage = WebViewLoginPage()
    var announcementsListPage = AnnouncementsListPage()
    var peopleListPage = PeopleListPage()
    var studentContextPage = StudentContextPage()
    var pageListPage = PageListPage()

}
