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
import com.instructure.canvas.espresso.common.pages.AboutPage
import com.instructure.canvas.espresso.common.pages.CanvasNetworkSignInPage
import com.instructure.canvas.espresso.common.pages.EmailNotificationsPage
import com.instructure.canvas.espresso.common.pages.InboxPage
import com.instructure.canvas.espresso.common.pages.LegalPage
import com.instructure.canvas.espresso.common.pages.LoginFindSchoolPage
import com.instructure.canvas.espresso.common.pages.LoginLandingPage
import com.instructure.canvas.espresso.common.pages.LoginSignInPage
import com.instructure.espresso.InstructureActivityTestRule
import com.instructure.espresso.ModuleItemInteractions
import com.instructure.espresso.Searchable
import com.instructure.espresso.swipeRight
import com.instructure.pandautils.utils.Const
import com.instructure.student.BuildConfig
import com.instructure.student.R
import com.instructure.student.activity.LoginActivity
import com.instructure.student.ui.pages.classic.AllCoursesPage
import com.instructure.student.ui.pages.classic.AnnotationCommentListPage
import com.instructure.student.ui.pages.classic.AnnouncementListPage
import com.instructure.student.ui.pages.classic.BookmarkPage
import com.instructure.student.ui.pages.classic.CanvasWebViewPage
import com.instructure.student.ui.pages.classic.ConferenceDetailsPage
import com.instructure.student.ui.pages.classic.ConferenceListPage
import com.instructure.student.ui.pages.classic.CourseBrowserPage
import com.instructure.student.ui.pages.classic.CourseGradesPage
import com.instructure.student.ui.pages.classic.DashboardPage
import com.instructure.student.ui.pages.classic.DiscussionDetailsPage
import com.instructure.student.ui.pages.classic.DiscussionListPage
import com.instructure.student.ui.pages.classic.FileChooserPage
import com.instructure.student.ui.pages.classic.FileListPage
import com.instructure.student.ui.pages.classic.GoToQuizPage
import com.instructure.student.ui.pages.classic.GradesPage
import com.instructure.student.ui.pages.classic.GroupBrowserPage
import com.instructure.student.ui.pages.classic.HelpPage
import com.instructure.student.ui.pages.classic.LeftSideNavigationDrawerPage
import com.instructure.student.ui.pages.classic.ModuleProgressionPage
import com.instructure.student.ui.pages.classic.ModulesPage
import com.instructure.student.ui.pages.classic.NotificationPage
import com.instructure.student.ui.pages.classic.PageDetailsPage
import com.instructure.student.ui.pages.classic.PageListPage
import com.instructure.student.ui.pages.classic.PairObserverPage
import com.instructure.student.ui.pages.classic.PandaAvatarPage
import com.instructure.student.ui.pages.classic.PeopleListPage
import com.instructure.student.ui.pages.classic.PersonDetailsPage
import com.instructure.student.ui.pages.classic.PickerSubmissionUploadPage
import com.instructure.student.ui.pages.classic.ProfileSettingsPage
import com.instructure.student.ui.pages.classic.PushNotificationsPage
import com.instructure.student.ui.pages.classic.QRLoginPage
import com.instructure.student.ui.pages.classic.QuizListPage
import com.instructure.student.ui.pages.classic.QuizTakingPage
import com.instructure.student.ui.pages.classic.RemoteConfigSettingsPage
import com.instructure.student.ui.pages.classic.ShareExtensionStatusPage
import com.instructure.student.ui.pages.classic.ShareExtensionTargetPage
import com.instructure.student.ui.pages.classic.StudentAssignmentDetailsPage
import com.instructure.student.ui.pages.classic.SubmissionDetailsPage
import com.instructure.student.ui.pages.classic.SyllabusPage
import com.instructure.student.ui.pages.classic.TextSubmissionUploadPage
import com.instructure.student.ui.pages.classic.TodoPage
import com.instructure.student.ui.pages.classic.UrlSubmissionUploadPage
import com.instructure.student.ui.pages.classic.k5.ElementaryCoursePage
import com.instructure.student.ui.pages.classic.k5.ElementaryDashboardPage
import com.instructure.student.ui.pages.classic.k5.HomeroomPage
import com.instructure.student.ui.pages.classic.k5.ImportantDatesPage
import com.instructure.student.ui.pages.classic.k5.ResourcesPage
import com.instructure.student.ui.pages.classic.k5.SchedulePage
import com.instructure.student.ui.pages.classic.offline.ManageOfflineContentPage
import com.instructure.student.ui.pages.classic.offline.NativeDiscussionDetailsPage
import com.instructure.student.ui.pages.classic.offline.OfflineSyncSettingsPage
import com.instructure.student.ui.pages.classic.offline.SyncProgressPage
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
    val discussionDetailsPage = DiscussionDetailsPage(ModuleItemInteractions(R.id.moduleName, R.id.next_item, R.id.prev_item))
    val nativeDiscussionDetailsPage = NativeDiscussionDetailsPage(ModuleItemInteractions(R.id.moduleName, R.id.next_item, R.id.prev_item))
    val discussionListPage = DiscussionListPage(Searchable(R.id.search, R.id.search_src_text, R.id.search_close_btn))
    val allCoursesPage = AllCoursesPage()
    val fileListPage = FileListPage(Searchable(R.id.search, R.id.queryInput, R.id.clearButton, R.id.backButton))
    val fileChooserPage = FileChooserPage()
    val helpPage = HelpPage()
    val inboxPage = InboxPage()
    val legalPage = LegalPage()
    val aboutPage = AboutPage()
    val loginFindSchoolPage = LoginFindSchoolPage()
    val loginLandingPage = LoginLandingPage()
    val canvasNetworkSignInPage = CanvasNetworkSignInPage()
    val loginSignInPage = LoginSignInPage()
    val moduleProgressionPage = ModuleProgressionPage()
    val modulesPage = ModulesPage()
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
    val quizListPage = QuizListPage(Searchable(R.id.search, R.id.search_src_text, R.id.search_close_btn, R.id.backButton))
    val quizTakingPage = QuizTakingPage()
    val goToQuizPage = GoToQuizPage(ModuleItemInteractions(R.id.moduleName, R.id.next_item, R.id.prev_item))
    val remoteConfigSettingsPage = RemoteConfigSettingsPage()
    val pushNotificationsPage = PushNotificationsPage()
    val emailNotificationsPage = EmailNotificationsPage()
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
