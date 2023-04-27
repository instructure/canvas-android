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
import android.util.Log
import android.view.View
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvas.espresso.CanvasTest
import com.instructure.espresso.InstructureActivityTestRule
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.activities.LoginActivity
import com.instructure.teacher.ui.espresso.TeacherHiltTestApplication_Application
import com.instructure.teacher.ui.pages.AboutPage
import com.instructure.teacher.ui.pages.AddMessagePage
import com.instructure.teacher.ui.pages.AnnouncementsListPage
import com.instructure.teacher.ui.pages.AssigneeListPage
import com.instructure.teacher.ui.pages.AssignmentDetailsPage
import com.instructure.teacher.ui.pages.AssignmentDueDatesPage
import com.instructure.teacher.ui.pages.AssignmentListPage
import com.instructure.teacher.ui.pages.AssignmentSubmissionListPage
import com.instructure.teacher.ui.pages.CalendarEventPage
import com.instructure.teacher.ui.pages.ChooseRecipientsPage
import com.instructure.teacher.ui.pages.CommentLibraryPage
import com.instructure.teacher.ui.pages.CourseBrowserPage
import com.instructure.teacher.ui.pages.CourseSettingsPage
import com.instructure.teacher.ui.pages.DashboardPage
import com.instructure.teacher.ui.pages.DiscussionsDetailsPage
import com.instructure.teacher.ui.pages.DiscussionsListPage
import com.instructure.teacher.ui.pages.EditAnnouncementPage
import com.instructure.teacher.ui.pages.EditAssignmentDetailsPage
import com.instructure.teacher.ui.pages.EditDashboardPage
import com.instructure.teacher.ui.pages.EditDiscussionsDetailsPage
import com.instructure.teacher.ui.pages.EditPageDetailsPage
import com.instructure.teacher.ui.pages.EditProfileSettingsPage
import com.instructure.teacher.ui.pages.EditQuizDetailsPage
import com.instructure.teacher.ui.pages.EditSyllabusPage
import com.instructure.teacher.ui.pages.FileListPage
import com.instructure.teacher.ui.pages.HelpPage
import com.instructure.teacher.ui.pages.InboxMessagePage
import com.instructure.teacher.ui.pages.InboxPage
import com.instructure.teacher.ui.pages.LeftSideNavigationDrawerPage
import com.instructure.teacher.ui.pages.LegalPage
import com.instructure.teacher.ui.pages.LoginFindSchoolPage
import com.instructure.teacher.ui.pages.LoginLandingPage
import com.instructure.teacher.ui.pages.LoginSignInPage
import com.instructure.teacher.ui.pages.ModulesPage
import com.instructure.teacher.ui.pages.NavDrawerPage
import com.instructure.teacher.ui.pages.NotATeacherPage
import com.instructure.teacher.ui.pages.PageListPage
import com.instructure.teacher.ui.pages.PeopleListPage
import com.instructure.teacher.ui.pages.PersonContextPage
import com.instructure.teacher.ui.pages.PostSettingsPage
import com.instructure.teacher.ui.pages.ProfileSettingsPage
import com.instructure.teacher.ui.pages.QuizDetailsPage
import com.instructure.teacher.ui.pages.QuizListPage
import com.instructure.teacher.ui.pages.QuizSubmissionListPage
import com.instructure.teacher.ui.pages.RemoteConfigSettingsPage
import com.instructure.teacher.ui.pages.SettingsPage
import com.instructure.teacher.ui.pages.SpeedGraderCommentsPage
import com.instructure.teacher.ui.pages.SpeedGraderFilesPage
import com.instructure.teacher.ui.pages.SpeedGraderGradePage
import com.instructure.teacher.ui.pages.SpeedGraderPage
import com.instructure.teacher.ui.pages.SpeedGraderQuizSubmissionPage
import com.instructure.teacher.ui.pages.StudentContextPage
import com.instructure.teacher.ui.pages.SyllabusPage
import com.instructure.teacher.ui.pages.TodoPage
import com.instructure.teacher.ui.pages.WebViewLoginPage
import dagger.hilt.android.testing.HiltAndroidRule
import instructure.rceditor.RCETextEditor
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject

abstract class TeacherTest : CanvasTest() {

    override val activityRule: InstructureActivityTestRule<out Activity>
            = TeacherActivityTestRule(LoginActivity::class.java)

    override val isTesting = BuildConfig.IS_TESTING

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun baseSetup() {
        try {
            hiltRule.inject()
        } catch (e: IllegalStateException) {
            // Catch this exception to avoid multiple injection
            Log.w("Test Inject", e.message ?: "")
        }

        val originalActivity = activityRule.activity
        val application = originalActivity.application as? TeacherHiltTestApplication_Application
        application?.workerFactory = workerFactory
    }

    /**
     * Required for auto complete of page objects within tests
     */
    val addMessagePage = AddMessagePage()
    val announcementsListPage = AnnouncementsListPage()
    val assigneeListPage = AssigneeListPage()
    val assignmentDetailsPage = AssignmentDetailsPage()
    val assignmentDueDatesPage = AssignmentDueDatesPage()
    val assignmentListPage = AssignmentListPage()
    val assignmentSubmissionListPage = AssignmentSubmissionListPage()
    val postSettingsPage = PostSettingsPage()
    val calendarEventPage = CalendarEventPage()
    val chooseRecipientsPage = ChooseRecipientsPage()
    val commentLibraryPage = CommentLibraryPage()
    val courseBrowserPage = CourseBrowserPage()
    val courseSettingsPage = CourseSettingsPage()
    val dashboardPage = DashboardPage()
    val leftSideNavigationDrawerPage = LeftSideNavigationDrawerPage()
    val editDashboardPage = EditDashboardPage()
    val settingsPage = SettingsPage()
    val legalPage = LegalPage()
    val helpPage = HelpPage()
    val aboutPage = AboutPage()
    val remoteConfigSettingsPage = RemoteConfigSettingsPage()
    val profileSettingsPage = ProfileSettingsPage()
    val editProfileSettingsPage = EditProfileSettingsPage()
    val discussionsDetailsPage = DiscussionsDetailsPage()
    val discussionsListPage = DiscussionsListPage()
    val editAnnouncementPage = EditAnnouncementPage()
    val editAssignmentDetailsPage = EditAssignmentDetailsPage()
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
    val personContextPage = PersonContextPage()
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