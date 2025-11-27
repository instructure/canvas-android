package com.instructure.parentapp.ui.e2e.compose

import android.util.Log
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.CustomStatusApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.CanvasNetworkAdapter.adminToken
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.extensions.seedData
import com.instructure.parentapp.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test

@HiltAndroidTest
class CustomStatusesE2ETest: ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    private var customStatusId: String? = null

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CUSTOM_STATUSES, TestCategory.E2E)
    fun testCustomStatusesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, courses = 1, teachers = 1, parents = 1)
        val course = data.coursesList[0]
        val parent = data.parentsList[0]
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]

        Log.d(PREPARATION_TAG, "Seeding a custom status ('AMAZING') with the admin user.")
        customStatusId = CustomStatusApi.upsertCustomGradeStatus(adminToken, name = "AMAZING", color = "#FF0000")

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for '${course.name}' course.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG, "Student submits the assignment.")
        SubmissionsApi.submitCourseAssignment(
            courseId = course.id,
            studentToken = student.token,
            assignmentId = testAssignment.id,
            submissionType = SubmissionType.ONLINE_TEXT_ENTRY
        )

        Log.d(PREPARATION_TAG, "Teacher grades submission with custom status 'AMAZING'.")
        SubmissionsApi.gradeSubmission(
            teacherToken = teacher.token,
            courseId = course.id,
            assignmentId = testAssignment.id,
            studentId = student.id,
            postedGrade = "12",
            customGradeStatusId = customStatusId
        )

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)

        Log.d(STEP_TAG, "Click on the '${course.name}' course.")
        coursesPage.clickCourseItem(course.name)

        Log.d(ASSERTION_TAG, "Assert that the details of the course has opened.")
        courseDetailsPage.assertCourseNameDisplayed(course)

        Log.d(ASSERTION_TAG, "Assert that the assignment '${testAssignment.name}' is displayed with the custom status 'AMAZING' on Course Details Page.")
        courseDetailsPage.assertAssignmentStatus(testAssignment.name, "AMAZING")

        Log.d(STEP_TAG, "Click on the '${testAssignment.name}' assignment to open it's details.")
        retryWithIncreasingDelay {
            courseDetailsPage.clickAssignment(testAssignment.name)
        }

        Log.d(ASSERTION_TAG, "Assert that the Assignment Details Page is displayed with the custom status 'AMAZING'.")
        assignmentDetailsPage.assertCustomStatus("AMAZING")
        assignmentDetailsPage.assertSubmissionAndRubricLabel()
    }

    @After
    fun tearDown() {
        customStatusId?.let {
            try {
                Log.d(PREPARATION_TAG, "Cleaning up the custom status we created with '$it' ID previously because 3 is the maximum limit of custom statuses.")
                CustomStatusApi.deleteCustomGradeStatus(adminToken, it)
                Log.d(PREPARATION_TAG, "Successfully deleted custom status with ID: $it")
            } catch (e: Exception) {
                Log.e(PREPARATION_TAG, "Failed to delete custom status with ID: $it", e)
                throw e
            }
        } ?: Log.w(PREPARATION_TAG, "No custom status ID to clean up - this might indicate the test failed during setup")
    }
}