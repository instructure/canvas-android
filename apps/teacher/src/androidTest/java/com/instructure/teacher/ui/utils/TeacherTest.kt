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
import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvas.espresso.CanvasTest
import com.instructure.espresso.InstructureActivityTestRule
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.activities.LoginActivity
import com.instructure.teacher.ui.pages.*
import dagger.hilt.android.testing.HiltAndroidRule
import instructure.rceditor.RCETextEditor
import org.hamcrest.Matcher
import org.junit.Rule

abstract class TeacherTest : CanvasTest() {

    override val activityRule: InstructureActivityTestRule<out Activity>
            = TeacherActivityTestRule(LoginActivity::class.java)

    override val isTesting = BuildConfig.IS_TESTING

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    /**
     * Required for auto complete of page objects within tests
     */
    val addMessagePage = AddMessagePage()
    val allCoursesListPage = AllCoursesListPage()
    val announcementsListPage = AnnouncementsListPage()
    val assigneeListPage = AssigneeListPage()
    val assignmentDetailsPage = AssignmentDetailsPage()
    val assignmentDueDatesPage = AssignmentDueDatesPage()
    val assignmentListPage = AssignmentListPage()
    val assignmentSubmissionListPage = AssignmentSubmissionListPage()
    val calendarEventPage = CalendarEventPage()
    val chooseRecipientsPage = ChooseRecipientsPage()
    val commentLibraryPage = CommentLibraryPage()
    val courseBrowserPage = CourseBrowserPage()
    val courseSettingsPage = CourseSettingsPage()
    val coursesListPage = CoursesListPage()
    val dashboardPage = DashboardPage()
    val discussionsDetatailsPage = DiscussionsDetailsPage()
    val discussionsListPage = DiscussionsListPage()
    val editAnnouncementPage = EditAnnouncementPage()
    val editAssignmentDetailsPage = EditAssignmentDetailsPage()
    val editCoursesListPage = EditCoursesListPage()
    val editDiscussionsDetailsPage = EditDiscussionsDetailsPage()
    val editPageDetailsPage = EditPageDetailsPage()
    val editQuizDetailsPage = EditQuizDetailsPage()
    val editSyllabusPage = EditSyllabusPage()
    val inboxMessagePage = InboxMessagePage()
    val inboxPage = InboxPage()
    val loginFindSchoolPage = LoginFindSchoolPage()
    val loginLandingPage = LoginLandingPage()
    val loginSignInPage = LoginSignInPage()
    val modulesPage = ModulesPage()
    val navDrawerPage = NavDrawerPage()
    val notATeacherPage = NotATeacherPage()
    val pageListPage = PageListPage()
    val peopleListPage = PeopleListPage()
    val quizDetailsPage = QuizDetailsPage()
    val quizListPage = QuizListPage()
    val quizSubmissionListPage = QuizSubmissionListPage()
    val speedGraderCommentsPage = SpeedGraderCommentsPage()
    val speedGraderFilesPage = SpeedGraderFilesPage()
    val speedGraderGradePage = SpeedGraderGradePage()
    val speedGraderPage = SpeedGraderPage()
    val speedGraderQuizSubmissionPage = SpeedGraderQuizSubmissionPage()
    val studentContextPage = StudentContextPage()
    val syllabusPage = SyllabusPage()
    val todoPage = TodoPage()
    val webViewLoginPage = WebViewLoginPage()
    val fileListPage = FileListPage()

}

/*
 * Custom action to enter text into an RCETextEditor
 * This had to go here, instead of CustomActions, because CustomActions is not aware of RCETExtEditor.
 */
class TypeInRCETextEditor(val text: String) : ViewAction {
    override fun getDescription(): String {
        return "Enters text into an RCETextEditor"
    }

    override fun getConstraints(): Matcher<View> {
        return ViewMatchers.isAssignableFrom(RCETextEditor::class.java)
    }

    override fun perform(uiController: UiController?, view: View?) {
        when(view) {
            is RCETextEditor -> view.applyHtml(text)
        }
    }
}