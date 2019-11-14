/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.ui.interaction

import android.os.SystemClock.sleep
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.mockCanvas.AssignmentGroupType
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import org.junit.Test

class SubmissionDetailsInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    private lateinit var course: Course

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testRubrics_showCriterionDescription() {
        // Clicking the "Description" button on a rubric criterion item should show a new page with the full description
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testComments_addCommentToSubmission() {
        // Should be able to add a comment on a submission

        val data = getToCourse()
        val assignment = data.addAssignment(
                courseId =  course.id,
                groupType = AssignmentGroupType.UPCOMING,
                submissionType = Assignment.SubmissionType.ONLINE_URL,
                userSubmitted = true
        )

        courseBrowserPage.selectAssignments()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.clickSubmit()
        urlSubmissionUploadPage.submitText("https://google.com")
        sleep(1000)
        assignmentDetailsPage.verifyAssignmentSubmitted() // flaky.  Sleep above?
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.openComments()
        submissionDetailsPage.addAndSendComment("Hey!")
        submissionDetailsPage.assertCommentDisplayed("Hey!", data.users.values.first())
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testAssignments_previewAttachment() {
        // Student can preview an assignment attachment

    }

    // Video comment testing is in AssignmentsE2ETest.testMediaCommentsE2E
    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testComments_addVideoCommentToSubmission() {
        // Should be able to add a video comment on a submission

    }

    // Audio comment testing is in AssignmentsE2ETest.testMediaCommentsE2E
    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testComments_addAudioCommentToSubmission() {
        // Should be able to add a audio comment on a submission
    }

    @Stub
    @Test
    @TestMetaData(Priority.P2, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testComments_videoCommentPlayback() {
        // After recording a video comment, user should be able to view a replay
    }

    @Stub
    @Test
    @TestMetaData(Priority.P2, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testComments_audioCommentPlayback() {
        // After recording an audio comment, user should be able to hear an audio playback
    }

    // Mock a specified number of students and courses, sign in, then navigate to course browser page for
    // first course.
    private fun getToCourse(
            studentCount: Int = 1,
            courseCount: Int = 1): MockCanvas {

        // Basic info
        val data = MockCanvas.init(
                studentCount = studentCount,
                courseCount = courseCount,
                favoriteCourseCount = courseCount)
        course = data.courses.values.first()

        // Sign in
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()

        // Navigate to the (first) course
        dashboardPage.selectCourse(course)

        return data
    }
}
