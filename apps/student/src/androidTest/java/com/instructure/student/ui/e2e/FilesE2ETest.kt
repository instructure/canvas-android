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
import androidx.test.espresso.Espresso.pressBack
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
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import com.instructure.student.ui.utils.uploadTextFile
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.io.File
import java.io.FileWriter

// Tests that files (assignment uploads, assignment comment attachments, discussion attachments)
// are properly displayed
@HiltAndroidTest
class FilesE2ETest: StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.FILES, TestCategory.E2E, false)
    fun testFilesE2E() {

        // Seed basic data
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        // Seed a text assignment/file/submission
        val assignment = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                withDescription = false,
                submissionTypes = listOf(SubmissionType.ONLINE_UPLOAD),
                allowedExtensions = listOf("txt"),
                teacherToken = teacher.token
        ))

        val submissionUploadInfo = uploadTextFile(
                assignmentId = assignment.id,
                courseId = course.id,
                token = student.token,
                fileUploadType = FileUploadType.ASSIGNMENT_SUBMISSION
        )

        val submission = SubmissionsApi.submitCourseAssignment(
                submissionType = SubmissionType.ONLINE_UPLOAD,
                courseId = course.id,
                assignmentId = assignment.id,
                fileIds = mutableListOf(submissionUploadInfo.id),
                studentToken = student.token
        )

        // Seed a comment attachment upload
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

        // Seed a discussion topic; will add a reply with attachment below
        val discussionTopic = DiscussionTopicsApi.createDiscussion(
                courseId = course.id,
                token = student.token
        )

        // At this point, sign in our student.  Login is necessary for the "real" API call
        // below to work correctly.
        tokenLogin(student)
        dashboardPage.waitForRender()

        // Create a discussion attachment file
        val discussionAttachmentFile = File(
                Randomizer.randomTextFileName(Environment.getExternalStorageDirectory().absolutePath))
                .apply { createNewFile() }

        // Add contents to file
        FileWriter(discussionAttachmentFile, true).apply {
            write(Randomizer.randomTextFileContents())
            flush()
            close()
        }

        // Use a "normal" api (rather than seeding) to create a reply to our
        // discussion that contains an attachment.
        val result = tryWeave {
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
            Log.v("FilesE2E", "Discussion post error: $it")
        }

        //
        // OK, let's get to testing
        //

        // Let's make sure that our submitted file and our discussion attachment are displayed
        // in the main files list.
        //
        // The fileListPage is a little different in that it keeps getting used over and over again,
        // recursively, as we traverse the file tree.
        dashboardPage.gotoGlobalFiles()
        fileListPage.assertItemDisplayed("Submissions")
        fileListPage.selectItem("Submissions")
        fileListPage.assertItemDisplayed(course.name)
        fileListPage.selectItem(course.name)
        fileListPage.assertItemDisplayed(submissionUploadInfo.fileName)
        pressBack() // Back to Submissions
        pressBack() // Back to main file list
        fileListPage.assertItemDisplayed("unfiled") // Our discussion attachment goes under "unfiled"
        fileListPage.selectItem("unfiled")
        fileListPage.assertItemDisplayed(discussionAttachmentFile.name)
        pressBack() // Back to main file list
        pressBack() // Back to dashboard

        // Let's check that our submission file and assignment comment attachment are shown in the assignment details
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.openFiles()
        submissionDetailsPage.assertFileDisplayed(submissionUploadInfo.fileName)
        submissionDetailsPage.openComments()
        submissionDetailsPage.assertCommentAttachmentDisplayed(commentUploadInfo.fileName,student)
        pressBack() // Back to assignment details
        pressBack() // Back to assignment list
        pressBack() // Back to course browser page

        // I'd like to go into discussions and verify that our reply-with-attachment shows up,
        // but that info is in a webview and thus would not be easy to verify.

        // Test renaming and deleting the discussion attachment
        pressBack() // Back to Dashboard
        dashboardPage.gotoGlobalFiles()
        fileListPage.assertItemDisplayed("unfiled") // Our discussion attachment goes under "unfiled"
        fileListPage.selectItem("unfiled")
        fileListPage.assertItemDisplayed(discussionAttachmentFile.name)
        val newFileName = "blah.txt"
        fileListPage.renameFile(discussionAttachmentFile.name, newFileName)
        fileListPage.assertItemDisplayed(newFileName)
        fileListPage.deleteFile(newFileName)
        fileListPage.assertViewEmpty()
    }
}