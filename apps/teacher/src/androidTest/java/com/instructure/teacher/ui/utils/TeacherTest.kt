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
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.instructure.canvas.espresso.CanvasTest
import com.instructure.canvas.espresso.common.pages.AboutPage
import com.instructure.canvas.espresso.common.pages.CanvasNetworkSignInPage
import com.instructure.canvas.espresso.common.pages.EmailNotificationsPage
import com.instructure.canvas.espresso.common.pages.InboxPage
import com.instructure.canvas.espresso.common.pages.LegalPage
import com.instructure.canvas.espresso.common.pages.LoginFindSchoolPage
import com.instructure.canvas.espresso.common.pages.LoginLandingPage
import com.instructure.canvas.espresso.common.pages.LoginSignInPage
import com.instructure.canvas.espresso.common.pages.WrongDomainPage
import com.instructure.espresso.InstructureActivityTestRule
import com.instructure.espresso.ModuleItemInteractions
import com.instructure.espresso.Searchable
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.R
import com.instructure.teacher.activities.LoginActivity
import com.instructure.teacher.espresso.TestAppManager
import com.instructure.teacher.ui.pages.classic.AnnouncementsListPage
import com.instructure.teacher.ui.pages.classic.AssigneeListPage
import com.instructure.teacher.ui.pages.classic.AssignmentDetailsPage
import com.instructure.teacher.ui.pages.classic.AssignmentDueDatesPage
import com.instructure.teacher.ui.pages.classic.CommentLibraryPage
import com.instructure.teacher.ui.pages.classic.CourseBrowserPage
import com.instructure.teacher.ui.pages.classic.CourseSettingsPage
import com.instructure.teacher.ui.pages.classic.DashboardPage
import com.instructure.teacher.ui.pages.classic.DiscussionsDetailsPage
import com.instructure.teacher.ui.pages.classic.DiscussionsListPage
import com.instructure.teacher.ui.pages.classic.EditAnnouncementDetailsPage
import com.instructure.teacher.ui.pages.classic.EditAssignmentDetailsPage
import com.instructure.teacher.ui.pages.classic.EditDashboardPage
import com.instructure.teacher.ui.pages.classic.EditDiscussionsDetailsPage
import com.instructure.teacher.ui.pages.classic.EditPageDetailsPage
import com.instructure.teacher.ui.pages.classic.EditProfileSettingsPage
import com.instructure.teacher.ui.pages.classic.EditQuizDetailsPage
import com.instructure.teacher.ui.pages.classic.EditSyllabusPage
import com.instructure.teacher.ui.pages.classic.FileListPage
import com.instructure.teacher.ui.pages.classic.HelpPage
import com.instructure.teacher.ui.pages.classic.LeftSideNavigationDrawerPage
import com.instructure.teacher.ui.pages.classic.ModulesPage
import com.instructure.teacher.ui.pages.classic.NavDrawerPage
import com.instructure.teacher.ui.pages.classic.NotATeacherPage
import com.instructure.teacher.ui.pages.classic.PageListPage
import com.instructure.teacher.ui.pages.classic.PeopleListPage
import com.instructure.teacher.ui.pages.classic.PersonContextPage
import com.instructure.teacher.ui.pages.classic.PostSettingsPage
import com.instructure.teacher.ui.pages.classic.ProfileSettingsPage
import com.instructure.teacher.ui.pages.classic.PushNotificationsPage
import com.instructure.teacher.ui.pages.classic.QuizDetailsPage
import com.instructure.teacher.ui.pages.classic.QuizListPage
import com.instructure.teacher.ui.pages.classic.RemoteConfigSettingsPage
import com.instructure.teacher.ui.pages.classic.SpeedGraderCommentsPage
import com.instructure.teacher.ui.pages.classic.SpeedGraderQuizSubmissionPage
import com.instructure.teacher.ui.pages.classic.StudentContextPage
import com.instructure.teacher.ui.pages.classic.SyllabusPage
import com.instructure.teacher.ui.pages.classic.TodoPage
import com.instructure.teacher.ui.pages.classic.UpdateFilePermissionsPage
import com.instructure.teacher.ui.pages.classic.WebViewLoginPage
import instructure.rceditor.RCETextEditor
import org.hamcrest.Matcher
import org.junit.Before

abstract class TeacherTest : CanvasTest() {

    override val activityRule: InstructureActivityTestRule<out Activity>
            = TeacherActivityTestRule(LoginActivity::class.java)

    override val isTesting = BuildConfig.IS_TESTING

    val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    /**
     * Required for auto complete of page objects within tests
     */
    val announcementsListPage = AnnouncementsListPage(Searchable(R.id.search, R.id.search_src_text, R.id.search_close_btn))
    val assigneeListPage = AssigneeListPage()
    val assignmentDetailsPage = AssignmentDetailsPage(ModuleItemInteractions(R.id.moduleName, R.id.next, R.id.previous))
    val assignmentDueDatesPage = AssignmentDueDatesPage()
    val postSettingsPage = PostSettingsPage()
    val commentLibraryPage = CommentLibraryPage()
    val courseBrowserPage = CourseBrowserPage()
    val courseSettingsPage = CourseSettingsPage()
    val dashboardPage = DashboardPage()
    val leftSideNavigationDrawerPage = LeftSideNavigationDrawerPage()
    val editDashboardPage = EditDashboardPage()
    val pushNotificationsPage = PushNotificationsPage()
    val emailNotificationsPage = EmailNotificationsPage()
    val legalPage = LegalPage()
    val helpPage = HelpPage()
    val aboutPage = AboutPage()
    val remoteConfigSettingsPage = RemoteConfigSettingsPage()
    val profileSettingsPage = ProfileSettingsPage()
    val editProfileSettingsPage = EditProfileSettingsPage()
    val discussionDetailsPage = DiscussionsDetailsPage(ModuleItemInteractions(R.id.moduleName, R.id.next, R.id.previous))
    val discussionsListPage = DiscussionsListPage(Searchable(R.id.search, R.id.search_src_text, R.id.search_close_btn))
    val editAnnouncementDetailsPage = EditAnnouncementDetailsPage()
    val editAssignmentDetailsPage = EditAssignmentDetailsPage()
    val editDiscussionsDetailsPage = EditDiscussionsDetailsPage()
    val editPageDetailsPage = EditPageDetailsPage(ModuleItemInteractions(R.id.moduleName, R.id.next, R.id.previous))
    val editQuizDetailsPage = EditQuizDetailsPage()
    val editSyllabusPage = EditSyllabusPage()
    val inboxPage = InboxPage()
    val loginFindSchoolPage = LoginFindSchoolPage()
    val loginLandingPage = LoginLandingPage()
    val loginSignInPage = LoginSignInPage()
    val wrongDomainPage = WrongDomainPage()
    val canvasNetworkSignInPage = CanvasNetworkSignInPage()
    val moduleListPage = ModulesPage()
    val navDrawerPage = NavDrawerPage()
    val notATeacherPage = NotATeacherPage()
    val pageListPage = PageListPage(Searchable(R.id.search, R.id.search_src_text, R.id.search_close_btn))
    val peopleListPage = PeopleListPage(Searchable(R.id.search, R.id.search_src_text, R.id.search_close_btn))
    val quizDetailsPage = QuizDetailsPage(ModuleItemInteractions(R.id.moduleName, R.id.next, R.id.previous))
    val quizListPage = QuizListPage(Searchable(R.id.search, R.id.search_src_text, R.id.search_close_btn, R.id.backButton))
    val speedGraderCommentsPage = SpeedGraderCommentsPage()
    val speedGraderQuizSubmissionPage = SpeedGraderQuizSubmissionPage()
    val personContextPage = PersonContextPage()
    val studentContextPage = StudentContextPage()
    val syllabusPage = SyllabusPage()
    val todoPage = TodoPage()
    val webViewLoginPage = WebViewLoginPage()
    val fileListPage = FileListPage(Searchable(R.id.search, R.id.queryInput, R.id.clearButton, R.id.backButton))
    val updateFilePermissionsPage = UpdateFilePermissionsPage()

    @Before
    fun setupWorkerFactory() {
        val application = activityRule.activity.application as? TestAppManager
        application?.workerFactory = workerFactory
    }
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