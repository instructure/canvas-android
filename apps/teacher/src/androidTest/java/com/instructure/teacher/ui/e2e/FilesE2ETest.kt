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
package com.instructure.teacher.ui.e2e

import android.os.Environment
import android.util.Log
import androidx.test.espresso.Espresso
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
import com.instructure.dataseeding.model.*
import com.instructure.dataseeding.util.Randomizer
import com.instructure.espresso.ViewUtils
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import com.instructure.teacher.ui.utils.uploadTextFile
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.io.File
import java.io.FileWriter

@HiltAndroidTest
class FilesE2ETest: TeacherTest() {
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
        val assignment = createAssignment(course, teacher)

        Log.d(PREPARATION_TAG, "Seed a text file.")
        val submissionUploadInfo = uploadTextFile(
                assignmentId = assignment.id,
                courseId = course.id,
                token = student.token,
                fileUploadType = FileUploadType.ASSIGNMENT_SUBMISSION
        )

        Log.d(PREPARATION_TAG, "Submit the ${assignment.name} assignment.")
        submitCourseAssignment(course, assignment, submissionUploadInfo, student)

        Log.d(PREPARATION_TAG,"Seed a comment attachment upload.")
        val commentUploadInfo = uploadTextFile(
                assignmentId = assignment.id,
                courseId = course.id,
                token = student.token,
                fileUploadType = FileUploadType.COMMENT_ATTACHMENT
        )

        commentOnSubmission(student, course, assignment, commentUploadInfo)

        Log.d(PREPARATION_TAG,"Seed a discussion topic. Will add a reply with attachment below.")
        val discussionTopic = createDiscussion(course, student)

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId}.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(PREPARATION_TAG,"Create a discussion attachment file.")
        val discussionAttachmentFile = File(
                Randomizer.randomTextFileName(Environment.getExternalStorageDirectory().absolutePath))
                .apply { createNewFile() }

        Log.d(PREPARATION_TAG,"Add some content to '${discussionAttachmentFile.name}' file")
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

        Log.d(STEP_TAG,"Assert that there is a directory called 'unfiled' is displayed.")
        fileListPage.assertItemDisplayed("unfiled") // Our discussion attachment goes under "unfiled"

        Log.d(STEP_TAG,"Select 'unfiled' directory. Assert that ${discussionAttachmentFile.name} file is displayed on the File List Page.")
        fileListPage.selectItem("unfiled")
        fileListPage.assertItemDisplayed(discussionAttachmentFile.name)

        Log.d(STEP_TAG,"Navigate back to the Dashboard Page.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG,"Open ${course.name} course and navigate to Assignments Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openAssignmentsTab()

        Log.d(STEP_TAG,"Click on ${assignment.name} assignment and navigate to Submissions Page.")
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.openSubmissionsPage()

        Log.d(STEP_TAG,"Click on ${student.name} student's submission and navigate to Files Tab.")
        assignmentSubmissionListPage.clickSubmission(student)
        speedGraderPage.selectFilesTab(1)

        Log.d(STEP_TAG,"Assert that ${submissionUploadInfo.fileName} file. Navigate to Comments Tab and ${commentUploadInfo.fileName} comment attachment is displayed.")
        assignmentSubmissionListPage.assertFileDisplayed(submissionUploadInfo.fileName)
        speedGraderPage.selectCommentsTab()
        assignmentSubmissionListPage.assertCommentAttachmentDisplayedCommon(commentUploadInfo.fileName, student.shortName)

        Log.d(STEP_TAG,"Navigate back to Dashboard Page.")
        ViewUtils.pressBackButton(5)

        Log.d(STEP_TAG,"Navigate to 'Files' menu in user left-side menubar.")
        dashboardPage.gotoGlobalFiles()

        Log.d(STEP_TAG,"Assert that there is a directory called 'unfiled' is displayed.")
        fileListPage.assertItemDisplayed("unfiled")

        Log.d(STEP_TAG,"Select 'unfiled' directory. Assert that ${discussionAttachmentFile.name} file is displayed on the File List Page.")
        fileListPage.selectItem("unfiled")
        fileListPage.assertItemDisplayed(discussionAttachmentFile.name)

        Log.d(STEP_TAG,"Select ${discussionAttachmentFile.name} file.")
        fileListPage.selectItem(discussionAttachmentFile.name)

        val newFileName = "newFileName.txt"
        Log.d(STEP_TAG,"Rename ${discussionAttachmentFile.name} file to: $newFileName.")
        fileListPage.renameFile(newFileName)

        Log.d(STEP_TAG,"Navigate back to File List Page.")
        Espresso.pressBack()
        fileListPage.assertPageObjects()

        Log.d(STEP_TAG,"Assert that the file is displayed with it's new file name: $newFileName.")
        fileListPage.assertItemDisplayed(newFileName)

        Log.d(STEP_TAG,"Delete $newFileName file.")
        fileListPage.deleteFile(newFileName)
        fileListPage.assertPageObjects()

        Log.d(STEP_TAG,"Assert that empty view is displayed after deletion, because no file left to display.")
        fileListPage.assertViewEmpty()
    }

    private fun createDiscussion(
        course: CourseApiModel,
        student: CanvasUserApiModel
    ): DiscussionApiModel {
        return DiscussionTopicsApi.createDiscussion(
            courseId = course.id,
            token = student.token
        )
    }

    private fun commentOnSubmission(
        student: CanvasUserApiModel,
        course: CourseApiModel,
        assignment: AssignmentApiModel,
        commentUploadInfo: AttachmentApiModel
    ) {
        SubmissionsApi.commentOnSubmission(
            studentToken = student.token,
            courseId = course.id,
            assignmentId = assignment.id,
            fileIds = mutableListOf(commentUploadInfo.id)
        )
    }

    private fun submitCourseAssignment(
        course: CourseApiModel,
        assignment: AssignmentApiModel,
        submissionUploadInfo: AttachmentApiModel,
        student: CanvasUserApiModel
    ) {
        SubmissionsApi.submitCourseAssignment(
            submissionType = SubmissionType.ONLINE_UPLOAD,
            courseId = course.id,
            assignmentId = assignment.id,
            fileIds = mutableListOf(submissionUploadInfo.id),
            studentToken = student.token
        )
    }

    private fun createAssignment(
        course: CourseApiModel,
        teacher: CanvasUserApiModel
    ): AssignmentApiModel {
        return AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                withDescription = false,
                submissionTypes = listOf(SubmissionType.ONLINE_UPLOAD),
                allowedExtensions = listOf("txt"),
                teacherToken = teacher.token
            )
        )
    }

}