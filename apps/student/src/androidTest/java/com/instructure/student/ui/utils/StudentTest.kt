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
import android.os.Environment
import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvas.espresso.CanvasTest
import com.instructure.espresso.InstructureActivityTestRule
import com.instructure.espresso.swipeRight
import com.instructure.student.BuildConfig
import com.instructure.student.R
import com.instructure.student.activity.LoginActivity
import com.instructure.student.ui.pages.*
import dagger.hilt.android.testing.HiltAndroidRule
import instructure.rceditor.RCETextEditor
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Rule
import java.io.File

abstract class StudentTest : CanvasTest() {

    override val activityRule: InstructureActivityTestRule<out Activity> =
        StudentActivityTestRule(LoginActivity::class.java)

    lateinit var originalActivity : Activity

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    // Sometimes activityRule.activity can get nulled out over time, probably as we
    // navigate away from the original login screen.  Capture the activity here so
    // that we can reference it safely later.
    @Before
    fun recordOriginalActivity() {
        originalActivity = activityRule.activity
    }

    override val isTesting = BuildConfig.IS_TESTING

    /**
     * Required for auto complete of page objects within tests
     */
    val annotationCommentListPage = AnnotationCommentListPage()
    val announcementListPage = AnnouncementListPage()
    val assignmentDetailsPage = AssignmentDetailsPage()
    val assignmentListPage = AssignmentListPage()
    val bookmarkPage = BookmarkPage()
    val calendarEventPage = CalendarEventPage()
    val canvasWebViewPage = CanvasWebViewPage()
    val courseBrowserPage = CourseBrowserPage()
    val conferenceListPage = ConferenceListPage()
    val conferenceDetailsPage = ConferenceDetailsPage()
    val elementaryCoursePage = ElementaryCoursePage()
    val courseGradesPage = CourseGradesPage()
    val dashboardPage = DashboardPage()
    val discussionDetailsPage = DiscussionDetailsPage()
    val discussionListPage = DiscussionListPage()
    val editDashboardPage = EditDashboardPage()
    val fileListPage = FileListPage()
    val fileUploadPage = FileUploadPage()
    val helpPage = HelpPage()
    val inboxConversationPage = InboxConversationPage()
    val inboxPage = InboxPage()
    val legalPage = LegalPage()
    val aboutPage = AboutPage()
    val loginFindSchoolPage = LoginFindSchoolPage()
    val loginLandingPage = LoginLandingPage()
    val loginSignInPage = LoginSignInPage()
    val moduleProgressionPage = ModuleProgressionPage()
    val modulesPage = ModulesPage()
    val newMessagePage = NewMessagePage()
    val notificationPage = NotificationPage()
    val pageListPage = PageListPage()
    val pairObserverPage = PairObserverPage()
    val pandaAvatarPage = PandaAvatarPage()
    val peopleListPage = PeopleListPage()
    val personDetailsPage = PersonDetailsPage()
    val pickerSubmissionUploadPage = PickerSubmissionUploadPage()
    val profileSettingsPage = ProfileSettingsPage()
    val qrLoginPage = QRLoginPage()
    val quizListPage = QuizListPage()
    val quizTakingPage = QuizTakingPage()
    val remoteConfigSettingsPage = RemoteConfigSettingsPage()
    val settingsPage = SettingsPage()
    val submissionDetailsPage = SubmissionDetailsPage()
    val syllabusPage = SyllabusPage()
    val todoPage = TodoPage()
    val urlSubmissionUploadPage = UrlSubmissionUploadPage()
    val elementaryDashboardPage = ElementaryDashboardPage()
    val homeroomPage = HomeroomPage()
    val schedulePage = SchedulePage()
    val gradesPage = GradesPage()
    val resourcesPage = ResourcesPage()
    val importantDatesPage = ImportantDatesPage()
    val shareExtensionTargetPage = ShareExtensionTargetPage()
    val shareExtensionStatusPage = ShareExtensionStatusPage()

    // A no-op interaction to afford us an easy, harmless way to get a11y checking to trigger.
    fun meaninglessSwipe() {
        Espresso.onView(ViewMatchers.withId(R.id.action_bar_root)).swipeRight();
    }

    // Get the number of files/avatars in our panda avatars folder
    fun getSavedPandaAvatarCount() : Int {
        val root = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), originalActivity.getString(R.string.pandaAvatarsFolderName))

        if(root.isDirectory) {
            return root.listFiles()?.size ?: 0
        }
        else {
            return 0
        }
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
