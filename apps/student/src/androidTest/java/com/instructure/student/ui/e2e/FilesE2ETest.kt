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
package com.instructure.student.ui.e2e

import android.os.Environment
import android.util.Log
import com.instructure.canvas.espresso.E2E
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.utils.weave.awaitApiResponse
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.DiscussionTopicsApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.FileUploadType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.Randomizer
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.ViewUtils
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import com.instructure.student.ui.utils.uploadTextFile
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.io.File
import java.io.FileWriter

@HiltAndroidTest
class FilesE2ETest: StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enableAndConfigureAccessibilityChecks() {
        //We don't want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.FILES, TestCategory.E2E)
    fun testFilesE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding assignment for ${course.name} course.")
        val assignment = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                withDescription = false,
                submissionTypes = listOf(SubmissionType.ONLINE_UPLOAD),
                allowedExtensions = listOf("txt"),
                teacherToken = teacher.token
        ))

        Log.d(PREPARATION_TAG, "Seed a text file.")
        val submissionUploadInfo = uploadTextFile(
                assignmentId = assignment.id,
                courseId = course.id,
                token = student.token,
                fileUploadType = FileUploadType.ASSIGNMENT_SUBMISSION
        )

        Log.d(PREPARATION_TAG,"Submit ${assignment.name} assignment for ${student.name} student.")
        SubmissionsApi.submitCourseAssignment(
                submissionType = SubmissionType.ONLINE_UPLOAD,
                courseId = course.id,
                assignmentId = assignment.id,
                fileIds = mutableListOf(submissionUploadInfo.id),
                studentToken = student.token
        )

        Log.d(STEP_TAG,"Seed a comment attachment upload.")
        val commentUploadInfo = uploadTextFile(
                assignmentId = assignment.id,
                courseId = course.id,
                token = student.token,
                fileUploadType = FileUploadType.COMMENT_ATTACHMENT
        )

        SubmissionsApi.commentOnSubmission(
                studentToken = student.token,
                courseId = course.id,
                assignmentId = assignment.id,
                fileIds = mutableListOf(commentUploadInfo.id)
        )

        Log.d(STEP_TAG,"Seed a discussion for ${course.name} course.")
        val discussionTopic = DiscussionTopicsApi.createDiscussion(
                courseId = course.id,
                token = student.token
        )

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId} , password: ${student.password}")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Create a discussion attachment file.")
        val discussionAttachmentFile = File(
                Randomizer.randomTextFileName(Environment.getExternalStorageDirectory().absolutePath))
                .apply { createNewFile() }

        Log.d(STEP_TAG,"Add some random content to the ${discussionAttachmentFile.name} file.")
        FileWriter(discussionAttachmentFile, true).apply {
            write(Randomizer.randomTextFileContents())
            flush()
            close()
        }

        Log.d(PREPARATION_TAG,"Use real API (rather than seeding) to create a reply to our discussion that contains an attachment.")
        tryWeave {
            awaitApiResponse<DiscussionEntry> {
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

        Log.d(STEP_TAG,"Navigate to 'Files' menu in user left-side menubar.")
        dashboardPage.gotoGlobalFiles()

        Log.d(STEP_TAG,"Assert that there is a directory called 'Submissions' is displayed.")
        fileListPage.assertItemDisplayed("Submissions")

        Log.d(STEP_TAG,"Select 'Submissions' directory. Assert that ${discussionAttachmentFile.name} file is displayed on the File List Page.")
        fileListPage.selectItem("Submissions")

        Log.d(STEP_TAG,"Assert that ${course.name} course is displayed.")
        fileListPage.assertItemDisplayed(course.name)

        Log.d(STEP_TAG,"Select ${course.name} course.")
        fileListPage.selectItem(course.name)

        Log.d(STEP_TAG,"Assert that ${discussionAttachmentFile.name} file is displayed on the File List Page.")
        fileListPage.assertItemDisplayed(submissionUploadInfo.fileName)

        Log.d(STEP_TAG,"Navigate back to File List Page.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG,"Assert that there is a directory called 'unfiled' is displayed.")
        fileListPage.assertItemDisplayed("unfiled") // Our discussion attachment goes under "unfiled"

        Log.d(STEP_TAG,"Select 'unfiled' directory. Assert that ${discussionAttachmentFile.name} file is displayed on the File List Page.")
        fileListPage.selectItem("unfiled")
        fileListPage.assertItemDisplayed(discussionAttachmentFile.name)

        Log.d(STEP_TAG,"Navigate back to Dashboard Page.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG,"Select ${course.name} course.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG,"Navigate to Assignments Page.")
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG,"Click on ${assignment.name} assignment.")
        assignmentListPage.clickAssignment(assignment)

        Log.d(STEP_TAG,"Navigate to Submission Details Page and open Files Tab.")
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.openFiles()

        Log.d(STEP_TAG,"Assert that ${submissionUploadInfo.fileName} file has been displayed.")
        submissionDetailsPage.assertFileDisplayed(submissionUploadInfo.fileName)

        Log.d(STEP_TAG,"Open Comments Tab. Assert that ${commentUploadInfo.fileName} file is displayed as a comment by ${student.name} student.")
        submissionDetailsPage.openComments()
        submissionDetailsPage.assertCommentAttachmentDisplayed(commentUploadInfo.fileName, student)

        Log.d(STEP_TAG,"Navigate back to Dashboard Page.")
        ViewUtils.pressBackButton(4)

        Log.d(STEP_TAG,"Navigate to 'Files' menu in user left-side menubar.")
        dashboardPage.gotoGlobalFiles()

        Log.d(STEP_TAG,"Assert that there is a directory called 'unfiled' is displayed.")
        fileListPage.assertItemDisplayed("unfiled") // Our discussion attachment goes under "unfiled"

        Log.d(STEP_TAG,"Select 'unfiled' directory. Assert that ${discussionAttachmentFile.name} file is displayed on the File List Page.")
        fileListPage.selectItem("unfiled")
        fileListPage.assertItemDisplayed(discussionAttachmentFile.name)

        val newFileName = "newTextFileName.txt"
        Log.d(STEP_TAG,"Rename ${discussionAttachmentFile.name} file to: $newFileName.")
        fileListPage.renameFile(discussionAttachmentFile.name, newFileName)

        Log.d(STEP_TAG,"Assert that the file is displayed with it's new file name: $newFileName.")
        fileListPage.assertItemDisplayed(newFileName)

        Log.d(STEP_TAG,"Delete $newFileName file.")
        fileListPage.deleteFile(newFileName)

        Log.d(STEP_TAG,"Assert that empty view is displayed after deletion.")
        fileListPage.assertViewEmpty()
    }
}