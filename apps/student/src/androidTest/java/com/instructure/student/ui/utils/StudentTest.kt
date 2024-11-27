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
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.View
import androidx.core.content.FileProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.instructure.canvas.espresso.CanvasTest
import com.instructure.canvas.espresso.common.pages.InboxPage
import com.instructure.espresso.InstructureActivityTestRule
import com.instructure.espresso.ModuleItemInteractions
import com.instructure.espresso.Searchable
import com.instructure.espresso.pages.common.LoginFindSchoolPage
import com.instructure.espresso.pages.common.LoginLandingPage
import com.instructure.espresso.pages.common.LoginSignInPage
import com.instructure.espresso.swipeRight
import com.instructure.pandautils.utils.Const
import com.instructure.student.BuildConfig
import com.instructure.student.R
import com.instructure.student.activity.LoginActivity
import com.instructure.student.ui.pages.AboutPage
import com.instructure.student.ui.pages.AllCoursesPage
import com.instructure.student.ui.pages.AnnotationCommentListPage
import com.instructure.student.ui.pages.AnnouncementListPage
import com.instructure.student.ui.pages.AssignmentListPage
import com.instructure.student.ui.pages.BookmarkPage
import com.instructure.student.ui.pages.CanvasWebViewPage
import com.instructure.student.ui.pages.ConferenceDetailsPage
import com.instructure.student.ui.pages.ConferenceListPage
import com.instructure.student.ui.pages.CourseBrowserPage
import com.instructure.student.ui.pages.CourseGradesPage
import com.instructure.student.ui.pages.DashboardPage
import com.instructure.student.ui.pages.DiscussionListPage
import com.instructure.student.ui.pages.ElementaryCoursePage
import com.instructure.student.ui.pages.ElementaryDashboardPage
import com.instructure.student.ui.pages.FileChooserPage
import com.instructure.student.ui.pages.FileListPage
import com.instructure.student.ui.pages.GoToQuizPage
import com.instructure.student.ui.pages.GradesPage
import com.instructure.student.ui.pages.GroupBrowserPage
import com.instructure.student.ui.pages.HelpPage
import com.instructure.student.ui.pages.HomeroomPage
import com.instructure.student.ui.pages.ImportantDatesPage
import com.instructure.student.ui.pages.InboxConversationPage
import com.instructure.student.ui.pages.LeftSideNavigationDrawerPage
import com.instructure.student.ui.pages.LegalPage
import com.instructure.student.ui.pages.ModuleProgressionPage
import com.instructure.student.ui.pages.ModulesPage
import com.instructure.student.ui.pages.NewMessagePage
import com.instructure.student.ui.pages.NotificationPage
import com.instructure.student.ui.pages.PageDetailsPage
import com.instructure.student.ui.pages.PageListPage
import com.instructure.student.ui.pages.PairObserverPage
import com.instructure.student.ui.pages.PandaAvatarPage
import com.instructure.student.ui.pages.PeopleListPage
import com.instructure.student.ui.pages.PersonDetailsPage
import com.instructure.student.ui.pages.PickerSubmissionUploadPage
import com.instructure.student.ui.pages.ProfileSettingsPage
import com.instructure.student.ui.pages.PushNotificationsPage
import com.instructure.student.ui.pages.QRLoginPage
import com.instructure.student.ui.pages.QuizListPage
import com.instructure.student.ui.pages.QuizTakingPage
import com.instructure.student.ui.pages.RemoteConfigSettingsPage
import com.instructure.student.ui.pages.ResourcesPage
import com.instructure.student.ui.pages.SchedulePage
import com.instructure.student.ui.pages.ShareExtensionStatusPage
import com.instructure.student.ui.pages.ShareExtensionTargetPage
import com.instructure.student.ui.pages.StudentAssignmentDetailsPage
import com.instructure.student.ui.pages.SubmissionDetailsPage
import com.instructure.student.ui.pages.SyllabusPage
import com.instructure.student.ui.pages.TextSubmissionUploadPage
import com.instructure.student.ui.pages.TodoPage
import com.instructure.student.ui.pages.UrlSubmissionUploadPage
import com.instructure.student.ui.pages.offline.ManageOfflineContentPage
import com.instructure.student.ui.pages.offline.NativeDiscussionDetailsPage
import com.instructure.student.ui.pages.offline.OfflineSyncSettingsPage
import com.instructure.student.ui.pages.offline.SyncProgressPage
import instructure.rceditor.RCETextEditor
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf
import java.io.File

abstract class StudentTest : CanvasTest() {

    val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    override val isTesting = BuildConfig.IS_TESTING

    override val activityRule: InstructureActivityTestRule<out Activity> =
        StudentActivityTestRule(LoginActivity::class.java)

    /**
     * Required for auto complete of page objects within tests
     */
    val annotationCommentListPage = AnnotationCommentListPage()
    val announcementListPage = AnnouncementListPage(Searchable(R.id.search, R.id.search_src_text, R.id.search_close_btn))
    val assignmentDetailsPage = StudentAssignmentDetailsPage(ModuleItemInteractions(R.id.moduleName, R.id.next_item, R.id.prev_item))
    val assignmentListPage = AssignmentListPage(Searchable(R.id.search, R.id.search_src_text))
    val bookmarkPage = BookmarkPage()
    val canvasWebViewPage = CanvasWebViewPage()
    val courseBrowserPage = CourseBrowserPage()
    val groupBrowserPage = GroupBrowserPage()
    val conferenceListPage = ConferenceListPage()
    val conferenceDetailsPage = ConferenceDetailsPage()
    val elementaryCoursePage = ElementaryCoursePage()
    val courseGradesPage = CourseGradesPage()
    val dashboardPage = DashboardPage()
    val leftSideNavigationDrawerPage = LeftSideNavigationDrawerPage()
    val discussionDetailsPage = com.instructure.student.ui.pages.DiscussionDetailsPage(ModuleItemInteractions(R.id.moduleName, R.id.next_item, R.id.prev_item))
    val nativeDiscussionDetailsPage = NativeDiscussionDetailsPage(ModuleItemInteractions(R.id.moduleName, R.id.next_item, R.id.prev_item))
    val discussionListPage = DiscussionListPage(Searchable(R.id.search, R.id.search_src_text, R.id.search_close_btn))
    val allCoursesPage = AllCoursesPage()
    val fileListPage = FileListPage(Searchable(R.id.search, R.id.queryInput, R.id.clearButton, R.id.backButton))
    val fileChooserPage = FileChooserPage()
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
    val pageListPage = PageListPage(Searchable(R.id.search, R.id.search_src_text, R.id.search_close_btn))
    val pageDetailsPage = PageDetailsPage(ModuleItemInteractions(R.id.moduleName, R.id.next_item, R.id.prev_item))
    val pairObserverPage = PairObserverPage()
    val pandaAvatarPage = PandaAvatarPage()
    val peopleListPage = PeopleListPage()
    val personDetailsPage = PersonDetailsPage()
    val pickerSubmissionUploadPage = PickerSubmissionUploadPage()
    val profileSettingsPage = ProfileSettingsPage()
    val qrLoginPage = QRLoginPage()
    val quizListPage = QuizListPage()
    val quizTakingPage = QuizTakingPage()
    val goToQuizPage = GoToQuizPage(ModuleItemInteractions(R.id.moduleName, R.id.next_item, R.id.prev_item))
    val remoteConfigSettingsPage = RemoteConfigSettingsPage()
    val pushNotificationsPage = PushNotificationsPage()
    val submissionDetailsPage = SubmissionDetailsPage()
    val textSubmissionUploadPage = TextSubmissionUploadPage()
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
    val offlineSyncSettingsPage = OfflineSyncSettingsPage()
    val manageOfflineContentPage = ManageOfflineContentPage()
    val syncProgressPage = SyncProgressPage()

    // A no-op interaction to afford us an easy, harmless way to get a11y checking to trigger.
    fun meaninglessSwipe() {
        Espresso.onView(ViewMatchers.withId(R.id.action_bar_root)).swipeRight()
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

    fun setupFileOnDevice(fileName: String): Uri {
        File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "file_upload").deleteRecursively()
        copyAssetFileToExternalCache(activityRule.activity, fileName)

        val dir = activityRule.activity.externalCacheDir
        val file = File(dir?.path, fileName)

        val instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        return FileProvider.getUriForFile(
            instrumentationContext,
            "com.instructure.candroid" + Const.FILE_PROVIDER_AUTHORITY,
            file
        )
    }

    fun stubFilePickerIntent(fileName: String) {
        val resultData = Intent()
        val dir = activityRule.activity.externalCacheDir
        val file = File(dir?.path, fileName)
        val newFileUri = FileProvider.getUriForFile(
            activityRule.activity,
            "com.instructure.candroid" + Const.FILE_PROVIDER_AUTHORITY,
            file
        )
        resultData.data = newFileUri

        Intents.intending(
            AllOf.allOf(
                IntentMatchers.hasAction(Intent.ACTION_GET_CONTENT),
                IntentMatchers.hasType("*/*"),
            )
        ).respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, resultData))
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
