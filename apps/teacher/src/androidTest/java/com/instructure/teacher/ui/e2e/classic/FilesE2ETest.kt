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
package com.instructure.teacher.ui.e2e.classic

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.uiautomator.UiSelector
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
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
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.Randomizer
import com.instructure.espresso.triggerWorkManagerJobs
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.extensions.seedData
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import com.instructure.teacher.ui.utils.extensions.uploadTextFile
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter

@HiltAndroidTest
class FilesE2ETest: TeacherComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.FILES, TestCategory.E2E)
    fun testFilesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seed a text assignment/file/submission.")
        val assignment = AssignmentsApi.createAssignment(course.id, teacher.token, submissionTypes = listOf(SubmissionType.ONLINE_UPLOAD), allowedExtensions = listOf("txt"))

        Log.d(PREPARATION_TAG, "Seed a text file.")
        val submissionUploadInfo = uploadTextFile(
                assignmentId = assignment.id,
                courseId = course.id,
                token = student.token,
                fileUploadType = FileUploadType.ASSIGNMENT_SUBMISSION
        )

        Log.d(PREPARATION_TAG, "Submit the '${assignment.name}' assignment.")
        SubmissionsApi.submitCourseAssignment(course.id, student.token, assignment.id, submissionType = SubmissionType.ONLINE_UPLOAD, fileIds = mutableListOf(submissionUploadInfo.id))

        Log.d(PREPARATION_TAG, "Seed a comment attachment upload.")
        val commentUploadInfo = uploadTextFile(
                assignmentId = assignment.id,
                courseId = course.id,
                token = student.token,
                fileUploadType = FileUploadType.COMMENT_ATTACHMENT
        )

        Log.d(PREPARATION_TAG, "Comment a text file as a teacher to the '${student.name}' student's submission of the '${assignment.name}' assignment.")
        SubmissionsApi.commentOnSubmission(course.id, student.token, assignment.id, fileIds = mutableListOf(commentUploadInfo.id))

        Log.d(PREPARATION_TAG, "Seed a discussion topic. Will add a reply with attachment below.")
        val discussionTopic= DiscussionTopicsApi.createDiscussion(course.id, student.token)

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(PREPARATION_TAG, "Create a discussion attachment file.")
        val discussionAttachmentFile = File(
                Randomizer.randomTextFileName(Environment.getExternalStorageDirectory().absolutePath))
                .apply { createNewFile() }

        Log.d(PREPARATION_TAG, "Add some content to '${discussionAttachmentFile.name}' file")
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
            Log.v(PREPARATION_TAG, "Discussion post error: $it")
        }

        Log.d(STEP_TAG, "Navigate to 'Files' menu in user left-side menu.")
        leftSideNavigationDrawerPage.clickFilesMenu()

        Log.d(ASSERTION_TAG, "Assert that there is a directory called 'unfiled' is displayed.")
        fileListPage.assertItemDisplayed("unfiled") // Our discussion attachment goes under "unfiled"

        Log.d(STEP_TAG, "Select 'unfiled' directory.")
        fileListPage.selectItem("unfiled")

        Log.d(ASSERTION_TAG, "Assert that '${discussionAttachmentFile.name}' file is displayed on the File List Page.")
        fileListPage.assertItemDisplayed(discussionAttachmentFile.name)

        Log.d(STEP_TAG, "Navigate back to the Dashboard Page.")
        pressBackButton(2)

        Log.d(STEP_TAG, "Open '${course.name}' course and navigate to Assignments Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openAssignmentsTab()

        Log.d(STEP_TAG, "Click on '${assignment.name}' assignment and navigate to Submissions Page.")
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.clickAllSubmissions()

        Log.d(STEP_TAG, "Click on '${student.name}' student's submission and navigate to Files Tab.")
        assignmentSubmissionListPage.clickSubmission(student)

        Log.d(ASSERTION_TAG, "Assert that the '${submissionUploadInfo.fileName}' file has selected.")
        speedGraderPage.assertSelectedAttachmentItemDisplayed(submissionUploadInfo.fileName)

        Log.d(ASSERTION_TAG, "Assert that Comments label is displayed with value '1' because only 1 comment was seeded.")
        speedGraderPage.assertCommentsLabelDisplayed(1)

        Log.d(ASSERTION_TAG, "Assert that '${commentUploadInfo.fileName}' comment attachment is displayed.")
        speedGraderPage.assertCommentAttachmentDisplayed(commentUploadInfo.fileName)

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        pressBackButton(5)

        Log.d(STEP_TAG, "Navigate to 'Files' menu in user left-side menu.")
        leftSideNavigationDrawerPage.clickFilesMenu()

        Log.d(ASSERTION_TAG, "Assert that there is a directory called 'unfiled' is displayed.")
        fileListPage.assertItemDisplayed("unfiled")

        Log.d(STEP_TAG, "Click on 'Search' (magnifying glass) icon and type '${discussionAttachmentFile.name}', the file's name to the search input field.")
        fileListPage.searchable.clickOnSearchButton()
        fileListPage.searchable.typeToSearchBar(discussionAttachmentFile.name)

        Log.d(ASSERTION_TAG, "Assert that only 1 file matches for the search text, and it is '${discussionAttachmentFile.name}', and no directories has been shown in the result.")
        fileListPage.assertSearchResultCount(1)
        fileListPage.assertItemDisplayed(discussionAttachmentFile.name)
        fileListPage.assertItemNotDisplayed("unfiled")

        Log.d(STEP_TAG, "Click on 'Reset' search (cross) icon.")
        fileListPage.searchable.pressSearchBackButton()

        Log.d(ASSERTION_TAG, "Assert that all the root level directories and files are displayed (1).")
        fileListPage.assertFileListCount(1)

        Log.d(STEP_TAG, "Select 'unfiled' directory.")
        fileListPage.selectItem("unfiled")

        Log.d(ASSERTION_TAG, "Assert that '${discussionAttachmentFile.name}' file is displayed on the File List Page.")
        fileListPage.assertItemDisplayed(discussionAttachmentFile.name)

        Log.d(STEP_TAG, "Select '${discussionAttachmentFile.name}' file.")
        fileListPage.selectItem(discussionAttachmentFile.name)

        val newFileName = "newFileName.txt"
        Log.d(STEP_TAG, "Rename '${discussionAttachmentFile.name}' file to: '$newFileName'.")
        fileListPage.renameFile(newFileName)

        Log.d(STEP_TAG, "Navigate back to File List Page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the File List Page is displayed correctly.")
        fileListPage.assertPageObjects()

        Log.d(ASSERTION_TAG, "Assert that the file is displayed with it's new file name: '$newFileName'.")
        fileListPage.assertItemDisplayed(newFileName)

        Log.d(STEP_TAG, "Delete '$newFileName' file.")
        fileListPage.deleteFile(newFileName)

        Log.d(ASSERTION_TAG, "Assert that the File List Page is displayed correctly.")
        fileListPage.assertPageObjects()

        Log.d(ASSERTION_TAG, "Assert that empty view is displayed after deletion, because no file left to display.")
        fileListPage.assertViewEmpty()

        val newFolderName = "testfolder"
        Log.d(STEP_TAG, "Navigate back to File List Page and create a new folder with name: '$newFolderName'.")
        Espresso.pressBack()
        fileListPage.createFolder(newFolderName)

        Log.d(ASSERTION_TAG, "Assert that '$newFolderName' (recently created) folder is displayed.")
        fileListPage.assertItemDisplayed(newFolderName)

        Log.d(STEP_TAG, "Click on 'Search' (magnifying glass) icon and type '$newFolderName', the file's name to the search input field.")
        fileListPage.searchable.clickOnSearchButton()
        fileListPage.searchable.typeToSearchBar(newFolderName)

        Log.d(ASSERTION_TAG, "Assert that empty view is displayed after deletion, because no folders will not be displayed in search result.")
        fileListPage.assertViewEmpty()

        Log.d(STEP_TAG, "Press back button (top one) to escape from Search 'view'.")
        fileListPage.searchable.pressSearchBackButton()

        Log.d(STEP_TAG, "Select '$newFolderName' folder and delete it.")
        fileListPage.deleteFolder(newFolderName)

        Log.d(ASSERTION_TAG, "Assert that it has been disappeared from the File List Page.")
        fileListPage.assertItemNotDisplayed(newFolderName)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.FILES, TestCategory.E2E)
    fun testCommentAttachmentUploadWithUIE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seed a text assignment/file/submission.")
        val assignment = AssignmentsApi.createAssignment(course.id, teacher.token, submissionTypes = listOf(SubmissionType.ONLINE_UPLOAD), allowedExtensions = listOf("txt", "pdf"))

        Log.d(PREPARATION_TAG, "Seed a text file.")
        val submissionUploadInfo = uploadTextFile(
                assignmentId = assignment.id,
                courseId = course.id,
                token = student.token,
                fileUploadType = FileUploadType.ASSIGNMENT_SUBMISSION
        )

        Log.d(PREPARATION_TAG, "Submit the '${assignment.name}' assignment.")
        SubmissionsApi.submitCourseAssignment(course.id, student.token, assignment.id, submissionType = SubmissionType.ONLINE_UPLOAD, fileIds = mutableListOf(submissionUploadInfo.id))

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open '${course.name}' course and navigate to Assignments Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openAssignmentsTab()

        Log.d(STEP_TAG, "Click on '${assignment.name}' assignment and navigate to Submissions Page.")
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.clickAllSubmissions()

        Log.d(STEP_TAG, "Click on '${student.name}' student's submission.")
        assignmentSubmissionListPage.clickSubmission(student)

        Log.d(ASSERTION_TAG, "Assert that the '${submissionUploadInfo.fileName}' file has selected.")
        speedGraderPage.assertSelectedAttachmentItemDisplayed(submissionUploadInfo.fileName)

        Log.d(PREPARATION_TAG, "Create a PDF file for comment attachment test.")
        val pdfFileName = "test_comment_${System.currentTimeMillis()}.pdf"
        val pdfFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), pdfFileName)
        pdfFile.createNewFile()

        Log.d(PREPARATION_TAG, "Write content to PDF file '${pdfFile.name}'.")
        PdfDocument().apply {
            val pageInfo = PdfDocument.PageInfo.Builder(300, 300, 1).create()
            val page = startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            paint.color = Color.BLACK
            paint.textSize = 12f
            canvas.drawText("Test PDF Comment Attachment", 10f, 25f, paint)
            finishPage(page)
            writeTo(FileOutputStream(pdfFile))
            close()
        }

        Log.d(STEP_TAG, "Click on comment attachment button.")
        speedGraderPage.clickCommentAttachmentButton()

        Log.d(STEP_TAG, "Select 'Choose Files' from attachment type dialog.")
        speedGraderPage.clickChooseFilesOption()

        Log.d(STEP_TAG, "Select 'Device' as file source.")
        fileChooserPage.chooseDevice()

        Log.d(STEP_TAG, "Select the PDF file from Android file picker using UIAutomator.")
        val pdfFileObject = device.findObject(UiSelector().textContains(pdfFileName))
        if (pdfFileObject.exists()) {
            Log.d(STEP_TAG, "Found PDF file with exact name, clicking...")
            pdfFileObject.click()
        } else {
            Log.d(STEP_TAG, "PDF file not immediately visible, trying to navigate to Downloads...")
            val showRootsButton = device.findObject(UiSelector().descriptionContains("Show roots"))
            if (showRootsButton.exists()) {
                showRootsButton.click()
            }

            val downloadsItem = device.findObject(UiSelector().textContains("Downloads"))
            if (downloadsItem.exists()) {
                downloadsItem.click()
            }

            val pdfFileObject2 = device.findObject(UiSelector().textContains(pdfFileName))
            if (pdfFileObject2.exists()) {
                pdfFileObject2.click()
            }
        }
        device.waitForIdle()

        Log.d(STEP_TAG, "Click 'UPLOAD' button.")
        fileChooserPage.clickUpload()
        triggerWorkManagerJobs("FileUploadWorker")

        Log.d(ASSERTION_TAG, "Assert that PDF comment attachment '${pdfFile.name}' is displayed.")
        Thread.sleep(5000) // Wait for upload to complete and comment to be sent
        speedGraderPage.assertCommentAttachmentDisplayed(pdfFile.name)

        Log.d(ASSERTION_TAG, "Assert that Comments label is displayed with value '1' because one comment with attachment was uploaded.")
        speedGraderPage.assertCommentsLabelDisplayed(1)
    }

}