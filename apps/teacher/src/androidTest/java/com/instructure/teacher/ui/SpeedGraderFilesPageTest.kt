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
 */
package com.instructure.teacher.ui

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addSubmissionForAssignment
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class SpeedGraderFilesPageTest : TeacherComposeTest() {

    // Just good enough to mock the *representation* of a file, not to mock the file itself.
    val attachment = Attachment(
            id = 131313,
            contentType = "text/plain",
            filename = "sampleFile",
            displayName = "sampleFile",
            url = "http://fake.blah/somePath" // Code/Test will crash w/o a non-null url
    )

    @Test
    override fun displaysPageObjects() {
        goToSpeedGraderFilesPage(submissionCount = 1)
        speedGraderFilesPage.assertPageObjects()
    }

    @Test
    fun displaysEmptyFilesView() {
        goToSpeedGraderFilesPage()
        speedGraderFilesPage.assertDisplaysEmptyView()
    }

    @Test
    fun displaysFilesList() {
        val submissions = goToSpeedGraderFilesPage(submissionCount = 1)
        speedGraderFilesPage.assertHasFiles(mutableListOf(attachment))
    }

    @Test
    fun displaysSelectedFile() {
        goToSpeedGraderFilesPage(submissionCount = 1)
        val position = 0

        speedGraderFilesPage.selectFile(position)
        speedGraderFilesPage.assertFileSelected(position)
    }

    private fun goToSpeedGraderFilesPage(submissionCount: Int = 0): MockCanvas {
        val data = MockCanvas.init(teacherCount = 1, studentCount = 1, courseCount = 1, favoriteCourseCount = 1)
        val teacher = data.teachers[0]
        val course = data.courses.values.first()
        val student = data.students[0]

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val assignment = data.addAssignment(
                courseId = course.id,
                submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_UPLOAD)
        )

        repeat(submissionCount) {
            val submission = data.addSubmissionForAssignment(
                    assignmentId = assignment.id,
                    userId = student.id,
                    type = Assignment.SubmissionType.ONLINE_UPLOAD.apiString,
                    attachment = attachment
            )
        }

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        dashboardPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.openAllSubmissionsPage()
        assignmentSubmissionListPage.clickSubmission(student)

        speedGraderPage.selectFilesTab(assignment.submission?.attachments?.size ?: 0)
        return data
    }
}
