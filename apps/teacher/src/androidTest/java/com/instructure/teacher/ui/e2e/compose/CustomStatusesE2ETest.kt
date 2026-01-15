package com.instructure.teacher.ui.e2e.compose

import android.util.Log
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvas.espresso.pressBackButton
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.CustomStatusApi
import com.instructure.dataseeding.api.DifferentiationTagsApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.CanvasNetworkAdapter.adminToken
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.teacher.ui.pages.classic.PeopleListPage
import com.instructure.teacher.ui.pages.classic.PersonContextPage
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.extensions.seedData
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test
import java.lang.Thread.sleep

@HiltAndroidTest
class CustomStatusesE2ETest: TeacherComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    private var customStatusId: String? = null

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CUSTOM_STATUSES, TestCategory.E2E)
    fun testCustomStatusesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(teachers = 1, courses = 1, students = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

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

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open '${course.name}' course.")
        dashboardPage.openCourse(course.name)

        Log.d(STEP_TAG, "Navigate to the People List page.")
        courseBrowserPage.openPeopleTab()

        Log.d(ASSERTION_TAG, "Assert that '${student.name}' student is displayed and it is really a student person.")
        peopleListPage.assertPersonRole(student.name, PeopleListPage.UserRole.STUDENT)

        Log.d(STEP_TAG, "Click on '${student.name}', the student person.")
        peopleListPage.clickPerson(student)

        Log.d(ASSERTION_TAG, "Assert the that the student course info and the corresponding section name is displayed on Context Page.")
        personContextPage.assertDisplaysCourseInfo(course)
        personContextPage.assertSectionNameView(PersonContextPage.UserRole.STUDENT)

        Log.d(ASSERTION_TAG, "Assert that the assignment '${testAssignment.name}' is displayed with the custom status 'AMAZING' on Person Context Page.")
        personContextPage.assertAssignmentStatus(testAssignment.name, "AMAZING")

        Log.d(STEP_TAG, "Click on the '${testAssignment.name}' assignment to open it's details.")
        personContextPage.clickAssignment(testAssignment.name)

        Log.d(ASSERTION_TAG, "Assert that the SpeedGrader Grade Page is displayed with the custom status 'AMAZING' for the student '${student.name}', and the selected status text is 'AMAZING'.")
        speedGraderGradePage.assertCurrentStatus("AMAZING", student.name)
        speedGraderGradePage.assertSelectedStatusText("AMAZING")

        Log.d(STEP_TAG, "Navigate back to the Course Browser page.")
        pressBackButton(3)

        Log.d(STEP_TAG, "Navigate to '${course.name}' course's Assignments Tab.")
        courseBrowserPage.openAssignmentsTab()

        Log.d(STEP_TAG, "Click on '${testAssignment.name}' assignment.")
        assignmentListPage.clickAssignment(testAssignment)

        Log.d(STEP_TAG, "Open the 'All Submissions' page and click on the filter icon on the top-right corner.")
        assignmentDetailsPage.clickAllSubmissions()
        sleep(3000) // Sleep added to wait for the All Submissions page to load

        Log.d(ASSERTION_TAG, "Assert that the submission of the student '${student.name}' is displayed with the custom status tag 'AMAZING' on Assignment Submission List Page.")
        assignmentSubmissionListPage.assertCustomStatusTag("AMAZING")

        Log.d(STEP_TAG, "Click on the submission of the student '${student.name}' to open SpeedGrader Grade Page.")
        assignmentSubmissionListPage.clickSubmission(student)

        Log.d(ASSERTION_TAG, "Assert that the SpeedGrader Grade Page is displayed with the custom status 'AMAZING' for the student '${student.name}', and the selected status text is 'AMAZING'.")
        speedGraderGradePage.assertCurrentStatus("AMAZING", student.name)
        speedGraderGradePage.assertSelectedStatusText("AMAZING")

        Log.d(STEP_TAG, "Change the status from 'AMAZING' to 'None'.")
        speedGraderGradePage.selectStatus("None")
        sleep(3000) // Sleep added to wait for the status to be updated

        Log.d(ASSERTION_TAG, "The current status became 'Graded' as the submission is already graded.")
        speedGraderGradePage.assertCurrentStatus("Graded", student.name)
    }

    @E2E
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.CUSTOM_STATUSES, TestCategory.E2E)
    fun testFilterCustomStatusesAndDifferentiationTagsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(teachers = 1, courses = 1, students = 2)
        val student = data.studentsList[0]
        val student2 = data.studentsList[1]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seeding a custom status ('AMAZING') with the admin user.")
        customStatusId = CustomStatusApi.upsertCustomGradeStatus(adminToken, name = "AMAZING", color = "#FF0000")

        val groupSetId = DifferentiationTagsApi.createGroupSet(
            token = teacher.token,
            courseId = course.id.toString(),
            name = "Differentiation Tags Group Set",
            nonCollaborative = true
        )

        val firstDifferentiationTag = DifferentiationTagsApi.createGroup(
            token = teacher.token,
            groupSetId = groupSetId,
            name = "First Diff Tag"
        )

        val secondDifferentiationTag = DifferentiationTagsApi.createGroup(
            token = teacher.token,
            groupSetId = groupSetId,
            name = "Second Diff Tag"
        )

        DifferentiationTagsApi.addUserToGroup(
            token = teacher.token,
            groupId = firstDifferentiationTag.toLong(),
            userId = student.id
        )

        DifferentiationTagsApi.addUserToGroup(
            token = teacher.token,
            groupId = secondDifferentiationTag.toLong(),
            userId = student2.id
        )

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for '${course.name}' course.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG, "Student submits the assignment.")
        SubmissionsApi.submitCourseAssignment(
            courseId = course.id,
            studentToken = student.token,
            assignmentId = testAssignment.id,
            submissionType = SubmissionType.ONLINE_TEXT_ENTRY
        )

        Log.d(PREPARATION_TAG, "Teacher grades submission with custom status 'AMAZING' for '${student.name}' student.")
        SubmissionsApi.gradeSubmission(
            teacherToken = teacher.token,
            courseId = course.id,
            assignmentId = testAssignment.id,
            studentId = student.id,
            postedGrade = "12",
            customGradeStatusId = customStatusId
        )

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open '${course.name}' course.")
        dashboardPage.openCourse(course.name)

        Log.d(STEP_TAG, "Navigate to '${course.name}' course's Assignments Tab.")
        courseBrowserPage.openAssignmentsTab()

        Log.d(STEP_TAG, "Click on '${testAssignment.name}' assignment.")
        assignmentListPage.clickAssignment(testAssignment)

        Log.d(STEP_TAG, "Open the 'All Submissions' page and click on the filter icon on the top-right corner.")
        assignmentDetailsPage.clickAllSubmissions()
        assignmentSubmissionListPage.clickFilterButton()

        sleep(2000)
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