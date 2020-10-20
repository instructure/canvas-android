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
import android.content.Context
import android.os.Environment
import androidx.test.espresso.Espresso
import android.view.View
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
import instructure.rceditor.RCETextEditor
import org.hamcrest.Matcher
import org.junit.Before
import java.io.File

abstract class StudentTest : CanvasTest() {

    override val activityRule: InstructureActivityTestRule<out Activity> =
        StudentActivityTestRule(LoginActivity::class.java)
    lateinit var originalActivity : Activity

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
    val assignmentListPage = AssignmentListPage()
    val dashboardPage = DashboardPage()
    val allCoursesPage = AllCoursesPage()
    val editFavoritesPage = EditFavoritesPage()
    val calendarPage = CalendarPage()
    val todoPage = TodoPage()
    val inboxPage = InboxPage()
    val inboxConversationPage = InboxConversationPage()
    val newMessagePage = NewMessagePage()
    val settingsPage = SettingsPage()
    val pairObserverPage = PairObserverPage()
    val legalPage = LegalPage()
    val helpPage = HelpPage()
    val loginFindSchoolPage = LoginFindSchoolPage()
    val loginLandingPage = LoginLandingPage()
    val loginSignInPage = LoginSignInPage()
    val qrLoginPage = QRLoginPage()
    val courseBrowserPage = CourseBrowserPage()
    val assignmentDetailsPage = AssignmentDetailsPage()
    val submissionDetailsPage = SubmissionDetailsPage()
    val peopleListPage = PeopleListPage()
    val personDetailsPage = PersonDetailsPage()
    val modulesPage = ModulesPage()
    val syllabusPage = SyllabusPage()
    val fileListPage = FileListPage()
    val discussionListPage = DiscussionListPage()
    val discussionDetailsPage = DiscussionDetailsPage()
    val pageListPage = PageListPage()
    val quizListPage = QuizListPage()
    val urlSubmissionUploadPage = UrlSubmissionUploadPage()
    val courseGradesPage = CourseGradesPage()
    val moduleProgressionPage = ModuleProgressionPage()
    val canvasWebViewPage = CanvasWebViewPage()
    val fileUploadPage = FileUploadPage()
    val annotationCommentListPage = AnnotationCommentListPage()
    val pickerSubmissionUploadPage = PickerSubmissionUploadPage()
    val remoteConfigSettingsPage = RemoteConfigSettingsPage()
    val profileSettingsPage = ProfileSettingsPage()
    val calendarEventPage = CalendarEventPage()
    val quizTakingPage = QuizTakingPage()
    val pandaAvatarPage = PandaAvatarPage()

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
