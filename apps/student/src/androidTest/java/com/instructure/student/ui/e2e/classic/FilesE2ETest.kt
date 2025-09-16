/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.e2e.classic

import android.os.Environment
import android.util.Log
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.espresso.Espresso
import androidx.test.espresso.intent.Intents
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvas.espresso.common.pages.compose.AssignmentListPage
import com.instructure.canvas.espresso.pressBackButton
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.weave.awaitApiResponse
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.DiscussionTopicsApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.FileUploadType
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.Randomizer
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLogin
import com.instructure.student.ui.utils.extensions.uploadTextFile
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileWriter

@HiltAndroidTest
class FilesE2ETest: StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    val assignmentListPage by lazy { AssignmentListPage(composeTestRule) }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.FILES, TestCategory.E2E)
    fun testFilesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course.")
        val assignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_UPLOAD), allowedExtensions = listOf("txt"))

        Log.d(PREPARATION_TAG, "Seed a text file.")
        val submissionUploadInfo = uploadTextFile(
                assignmentId = assignment.id,
                courseId = course.id,
                token = student.token,
                fileUploadType = FileUploadType.ASSIGNMENT_SUBMISSION
        )

        Log.d(PREPARATION_TAG, "Submit '${assignment.name}' assignment for '${student.name}' student.")
        SubmissionsApi.submitCourseAssignment(course.id, student.token, assignment.id, SubmissionType.ONLINE_UPLOAD, fileIds =  mutableListOf(submissionUploadInfo.id))

        Log.d(PREPARATION_TAG, "Seed a comment attachment (file) upload.")
        val commentUploadInfo = uploadTextFile(
                assignmentId = assignment.id,
                courseId = course.id,
                token = student.token,
                fileUploadType = FileUploadType.COMMENT_ATTACHMENT
        )
        SubmissionsApi.commentOnSubmission(course.id, student.token, assignment.id, mutableListOf(commentUploadInfo.id))

        Log.d(PREPARATION_TAG, "Seed a discussion for '${course.name}' course.")
        val discussionTopic = DiscussionTopicsApi.createDiscussion(course.id, student.token)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(PREPARATION_TAG, "Create a discussion attachment file.")
        val discussionAttachmentFile = File(
                Randomizer.randomTextFileName(Environment.getExternalStorageDirectory().absolutePath))
                .apply { createNewFile() }

        Log.d(STEP_TAG, "Add some random content to the '${discussionAttachmentFile.name}' file.")
        FileWriter(discussionAttachmentFile, true).apply {
            write(Randomizer.randomTextFileContents())
            flush()
            close()
        }

        Log.d(PREPARATION_TAG, "Use real API (rather than seeding) to create a reply to our discussion that contains an attachment.")
        tryWeave {
            awaitApiResponse {
                DiscussionManager.postToDiscussionTopic(
                        canvasContext = CanvasContext.emptyCourseContext(id = course.id),
                        topicId = discussionTopic.id,
                        message = "Discussion message!",
                        attachment = discussionAttachmentFile,
                        mimeType = "text/plain",
                        callback = it)
            }
        } catch {
            Log.v(PREPARATION_TAG, "Discussion post error: '$it'")
        }

        Log.d(STEP_TAG, "Navigate to 'Files' menu in user left-side menu bar.")
        leftSideNavigationDrawerPage.clickFilesMenu()

        Log.d(ASSERTION_TAG, "Assert that there is a directory called 'Submissions' is displayed.")
        fileListPage.assertItemDisplayed("Submissions")

        Log.d(STEP_TAG, "Select 'Submissions' directory. Assert that '${discussionAttachmentFile.name}' file is displayed on the File List Page.")
        fileListPage.selectItem("Submissions")

        Log.d(ASSERTION_TAG, "Assert that '${course.name}' course is displayed.")
        fileListPage.assertItemDisplayed(course.name)

        Log.d(STEP_TAG, "Select '${course.name}' course.")
        fileListPage.selectItem(course.name)

        Log.d(ASSERTION_TAG, "Assert that '${discussionAttachmentFile.name}' file is displayed on the File List Page.")
        fileListPage.assertItemDisplayed(submissionUploadInfo.fileName)

        Log.d(STEP_TAG, "Navigate back to File List Page.")
        pressBackButton(2)

        Log.d(ASSERTION_TAG, "Assert that there is a directory called 'unfiled' is displayed.")
        fileListPage.assertItemDisplayed("unfiled") // Our discussion attachment goes under "unfiled"

        Log.d(STEP_TAG, "Select 'unfiled' directory.")
        fileListPage.selectItem("unfiled")

        Log.d(ASSERTION_TAG, "Assert that '${discussionAttachmentFile.name}' file is displayed on the File List Page.")
        fileListPage.assertItemDisplayed(discussionAttachmentFile.name)

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        pressBackButton(2)

        Log.d(STEP_TAG, "Select '${course.name}' course.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG, "Navigate to Assignments Page.")
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG, "Click on '${assignment.name}' assignment.")
        assignmentListPage.clickAssignment(assignment)

        Log.d(STEP_TAG, "Navigate to Submission Details Page and open Files Tab.")
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.openFiles()

        Log.d(ASSERTION_TAG, "Assert that '${submissionUploadInfo.fileName}' file has been displayed.")
        submissionDetailsPage.assertFileDisplayed(submissionUploadInfo.fileName)

        Log.d(STEP_TAG, "Open Comments Tab.")
        submissionDetailsPage.openComments()

        Log.d(ASSERTION_TAG, "Assert that '${commentUploadInfo.fileName}' file is displayed as a comment by '${student.name}' student.")
        submissionDetailsPage.assertCommentAttachmentDisplayed(commentUploadInfo.fileName, student)

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        pressBackButton(4)

        Log.d(STEP_TAG, "Navigate to 'Files' menu in user left-side menu bar.")
        leftSideNavigationDrawerPage.clickFilesMenu()

        Log.d(ASSERTION_TAG, "Assert that there is a directory called 'unfiled' is displayed.")
        fileListPage.assertItemDisplayed("unfiled") // Our discussion attachment goes under "unfiled"

        Log.d(STEP_TAG, "Click on 'Search' (magnifying glass) icon and type '${discussionAttachmentFile.name}', the file's name to the search input field.")
        fileListPage.searchable.clickOnSearchButton()
        fileListPage.searchable.typeToSearchBar(discussionAttachmentFile.name)

        Log.d(ASSERTION_TAG, "Assert that only 1 file matches for the search text, and it is '${discussionAttachmentFile.name}', and no directories has been shown in the result.")
        fileListPage.assertSearchResultCount(1)
        fileListPage.assertItemDisplayed(discussionAttachmentFile.name)
        fileListPage.assertItemNotDisplayed("unfiled")

        Log.d(STEP_TAG, "Press search back button to quit from search result view.")
        fileListPage.searchable.pressSearchBackButton()

        Log.d(STEP_TAG, "Select 'unfiled' directory.")
        fileListPage.selectItem("unfiled")

        Log.d(ASSERTION_TAG, "Assert that '${discussionAttachmentFile.name}' file is displayed on the File List Page.")
        fileListPage.assertItemDisplayed(discussionAttachmentFile.name)

        val newFileName = "newTextFileName.txt"
        Log.d(STEP_TAG, "Rename '${discussionAttachmentFile.name}' file to: '$newFileName'.")
        fileListPage.renameFile(discussionAttachmentFile.name, newFileName)

        Log.d(ASSERTION_TAG, "Assert that the file is displayed with its new file name: '$newFileName'.")
        fileListPage.assertItemDisplayed(newFileName)

        Log.d(STEP_TAG, "Delete '$newFileName' file.")
        fileListPage.deleteFile(newFileName)

        Log.d(ASSERTION_TAG, "Assert that empty view is displayed after deletion.")
        fileListPage.assertViewEmpty()

        Log.d(STEP_TAG, "Navigate back to global File List Page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the 'unfiled' folder has 0 items because we deleted the only item in it recently.")
        fileListPage.assertFolderSize("unfiled", 0)

        val testFolderName = "Krissinho's Test Folder"
        Log.d(STEP_TAG, "Click on Add ('+') button and then the 'Add Folder' icon, and create a new folder with the following name: '$testFolderName'.")
        fileListPage.clickAddButton()
        fileListPage.clickCreateNewFolderButton()
        fileListPage.createNewFolder(testFolderName)

        Log.d(ASSERTION_TAG, "Assert that there is a folder called '$testFolderName' is displayed.")
        fileListPage.assertItemDisplayed(testFolderName)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.FILES, TestCategory.E2E)
    fun testUploadGlobalFilesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val testFile = "samplepdf.pdf"

        Log.d(PREPARATION_TAG, "Setup the '$testFile' file on the device and clear the cache to make sure that the file names won't interfere with the possible cached ones.")
        setupFileOnDevice(testFile)
        File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "file_upload").deleteRecursively()

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to the global 'Files' Page from the left side menu.")
        leftSideNavigationDrawerPage.clickFilesMenu()

        Log.d(STEP_TAG, "Click on the 'Add' (+) icon and after that on the 'Upload File' icon.")
        fileListPage.clickAddButton()
        fileListPage.clickUploadFileButton()

        Log.d(ASSERTION_TAG, "Assert that the File Chooser Page details (title, subtitle, camera, gallery, device) are displayed correctly.")
        fileChooserPage.assertFileChooserDetails()

        Log.d(ASSERTION_TAG, "Assert that the File Choose Page title is 'Upload To My Files' as we would like to upload to the 'global' Files.")
        fileChooserPage.assertDialogTitle("Upload To My Files")

        //Note: This was a bug previously that if we attached a file, removing it, and attach it again,
        // there we placeholder numbers like ("samplepdf(1).pdf") in the file name, even though we did not uploaded the first selection.
        Log.d(PREPARATION_TAG, "Simulate file picker intent.")
        Intents.init()
        try {
            stubFilePickerIntent(testFile)
            fileChooserPage.chooseDevice()
        }
        finally {
            Intents.release()
        }

        Log.d(ASSERTION_TAG, "Assert that the '$testFile' file is displayed on the File Chooser Page.")
        fileChooserPage.assertFileDisplayed(testFile)

        Log.d(STEP_TAG, "Remove the '$testFile' file by clicking on the remove (X) icon.")
        fileChooserPage.removeFile(testFile)

        Log.d(ASSERTION_TAG, "Assert that the '$testFile' file is not displayed on the File Chooser Page.")
        fileChooserPage.assertFileNotDisplayed(testFile)

        Log.d(PREPARATION_TAG, "Simulate file picker intent (again).")
        Intents.init()
        try {
            stubFilePickerIntent(testFile)
            fileChooserPage.chooseDevice()
        }
        finally {
            Intents.release()
        }

        Log.d(ASSERTION_TAG, "Assert that the '$testFile' file is displayed on the File Chooser Page.")
        fileChooserPage.assertFileDisplayed(testFile)

        Log.d(STEP_TAG, "Click on the 'Upload' button.")
        fileChooserPage.clickUpload()

        Log.d(ASSERTION_TAG, "Assert that the file upload was successful so the file has displayed on the (global) File List Page.")
        fileListPage.assertItemDisplayed(testFile)
    }

}