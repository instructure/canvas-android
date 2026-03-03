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
        val data = seedData(teachers = 1, courses = 1, students = 3)
        val student = data.studentsList[0]
        val student2 = data.studentsList[1]
        val studentWithoutTag = data.studentsList[2]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seeding a custom status ('AMAZING') with the admin user.")
        customStatusId = CustomStatusApi.upsertCustomGradeStatus(adminToken, name = "AMAZING", color = "#FF0000")

        Log.d(PREPARATION_TAG, "Create 'Differentiation Tags Group Set' differentiation group set for '${course.name}' course.")
        val groupSetId = DifferentiationTagsApi.createGroupSet(
            token = teacher.token,
            courseId = course.id.toString(),
            name = "Differentiation Tags Group Set",
            nonCollaborative = true
        )

        Log.d(PREPARATION_TAG, "Seeding 'First Diff Tag' differentiation tags for '${course.name}' course.")
        val firstDifferentiationTag = DifferentiationTagsApi.createGroup(
            token = teacher.token,
            groupSetId = groupSetId,
            name = "First Diff Tag"
        )

        Log.d(PREPARATION_TAG, "Seeding 'Second Diff Tag' differentiation tags for '${course.name}' course.")
        val secondDifferentiationTag = DifferentiationTagsApi.createGroup(
            token = teacher.token,
            groupSetId = groupSetId,
            name = "Second Diff Tag"
        )

        Log.d(PREPARATION_TAG, "Seeding 'Third Diff Tag' differentiation tags for '${course.name}' course.")
        val thirdDifferentiationTag = DifferentiationTagsApi.createGroup(
            token = teacher.token,
            groupSetId = groupSetId,
            name = "Third Diff Tag"
        )

        Log.d(PREPARATION_TAG, "Assigning 'First Diff Tag' differentiation tag to '${student.name}' student.")
        DifferentiationTagsApi.addUserToGroup(
            token = teacher.token,
            groupId = firstDifferentiationTag.toLong(),
            userId = student.id
        )

        Log.d(PREPARATION_TAG, "Assigning 'Second Diff Tag' differentiation tag to '${student2.name}' student.")
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

        Log.d(STEP_TAG, "Open the 'All Submissions' page.")
        assignmentDetailsPage.clickAllSubmissions()

        Log.d(ASSERTION_TAG, "Assert that all the 3 students are displayed by default on the 'All Submissions' page before applying any filter.")
        assignmentSubmissionListPage.assertHasSubmission(3)
        assignmentSubmissionListPage.assertHasStudentSubmission(student)
        assignmentSubmissionListPage.assertHasStudentSubmission(student2)
        assignmentSubmissionListPage.assertHasStudentSubmission(studentWithoutTag)

        Log.d(STEP_TAG, "Click on the filter icon on the top-right corner again.")
        assignmentSubmissionListPage.clickFilterButton()

        Log.d(ASSERTION_TAG, "Assert that all the custom status filter text is displayed among the filtering options.")
        assignmentSubmissionListPage.assertCustomStatusFilterOption("AMAZING")

        Log.d(ASSERTION_TAG, "Assert that all the corresponding differentiation tag filter texts are displayed among the filtering options.")
        assignmentSubmissionListPage.assertDifferentiationTagFilterOption("Students without Differentiation tags")
        assignmentSubmissionListPage.assertDifferentiationTagFilterOption("First Diff Tag")
        assignmentSubmissionListPage.assertDifferentiationTagFilterOption("Second Diff Tag")
        assignmentSubmissionListPage.assertDifferentiationTagFilterOption("Third Diff Tag")

        // Check 'First Diff Tag' differentiation tag filter option
        Log.d(STEP_TAG, "Select the 'First Diff Tag' differentiation tag filter and click on 'Done'.")
        assignmentSubmissionListPage.clickDifferentiationTagFilter("First Diff Tag")
        assignmentSubmissionListPage.clickFilterDialogDone()

        Log.d(ASSERTION_TAG, "Assert that there is 1 submission displayed, and it is for '${student.name}' student since we applied a filter to the 'First Diff Tag' differentiation tag only.")
        assignmentSubmissionListPage.assertHasSubmission(1)
        assignmentSubmissionListPage.assertHasStudentSubmission(student)
        assignmentSubmissionListPage.assertCustomStatusTag("AMAZING") // The displayed submission has the custom status tag 'AMAZING'
        assignmentSubmissionListPage.assertStudentSubmissionNotDisplayed(student2)

        // Check 'Second Diff Tag' differentiation tag filter option
        Log.d(STEP_TAG, "Click on the filter icon on the top-right corner again.")
        assignmentSubmissionListPage.clickFilterButton()

        Log.d(STEP_TAG, "Unselect the 'First Diff Tag' and select the 'Second Diff Tag' differentiation tag filter and click on 'Done'.")
        assignmentSubmissionListPage.clickDifferentiationTagFilter("First Diff Tag")
        assignmentSubmissionListPage.clickDifferentiationTagFilter("Second Diff Tag")
        assignmentSubmissionListPage.clickFilterDialogDone()

        Log.d(ASSERTION_TAG, "Assert that there is 1 submission displayed, and it is for '${student2.name}' student since we applied a filter to the 'Second Diff Tag' differentiation tag only.")
        assignmentSubmissionListPage.assertHasSubmission(1)
        assignmentSubmissionListPage.assertHasStudentSubmission(student2)
        assignmentSubmissionListPage.assertStudentSubmissionNotDisplayed(student)

        // Check 'Students without Differentiation tags' filter option
        Log.d(STEP_TAG, "Click on the filter icon on the top-right corner again.")
        assignmentSubmissionListPage.clickFilterButton()

        Log.d(STEP_TAG, "Unselect the 'Second Diff Tag' and select the 'Students without Differentiation tags' differentiation tag filter and click on 'Done'.")
        assignmentSubmissionListPage.clickDifferentiationTagFilter("Second Diff Tag")
        assignmentSubmissionListPage.clickDifferentiationTagFilter("Students without Differentiation tags")
        assignmentSubmissionListPage.clickFilterDialogDone()

        Log.d(ASSERTION_TAG, "Assert that there is 1 submission displayed, and it is for '${studentWithoutTag.name}' student since we applied the 'Students without Differentiation tags' filter.")
        assignmentSubmissionListPage.assertHasSubmission(1)
        assignmentSubmissionListPage.assertHasStudentSubmission(studentWithoutTag)
        assignmentSubmissionListPage.assertStudentSubmissionNotDisplayed(student)
        assignmentSubmissionListPage.assertStudentSubmissionNotDisplayed(student2)

        // Check 'AMAZING' custom status filter option
        Log.d(STEP_TAG, "Click on the filter icon on the top-right corner again.")
        assignmentSubmissionListPage.clickFilterButton()

        Log.d(STEP_TAG, "Unselect the 'Students without Differentiation tags' differentiation tag and select 'AMAZING' custom status filter and click on 'Done'.")
        assignmentSubmissionListPage.clickDifferentiationTagFilter("Students without Differentiation tags")
        assignmentSubmissionListPage.clickFilterCustomStatus("AMAZING")
        assignmentSubmissionListPage.clickFilterDialogDone()

        Log.d(ASSERTION_TAG, "Assert that there is 1 submission displayed, and it is for '${student.name}' student since we applied a filter to the 'First Diff Tag' differentiation tag only.")
        assignmentSubmissionListPage.assertHasSubmission(1)
        assignmentSubmissionListPage.assertHasStudentSubmission(student)
        assignmentSubmissionListPage.assertCustomStatusTag("AMAZING") // The displayed submission has the custom status tag 'AMAZING'
        assignmentSubmissionListPage.assertStudentSubmissionNotDisplayed(student2)

        // Check 'Third Diff Tag' differentiation tag filter option
        Log.d(STEP_TAG, "Click on the filter icon on the top-right corner again.")
        assignmentSubmissionListPage.clickFilterButton()

        Log.d(STEP_TAG, "Unselect the 'AMAZING' custom status filter option and select the 'Third Diff Tag' differentiation tag and click on 'Done'.")
        assignmentSubmissionListPage.clickFilterCustomStatus("AMAZING")
        assignmentSubmissionListPage.clickDifferentiationTagFilter("Third Diff Tag")
        assignmentSubmissionListPage.clickFilterDialogDone()

        Log.d(ASSERTION_TAG, "Assert that there is no submission displayed since there are no students with 'Third Diff Tag' differentiation tag, so the empty view is displayed.")
        assignmentSubmissionListPage.assertHasNoSubmission()
        assignmentSubmissionListPage.assertEmptyViewDisplayed()

        // Check 'Missing' (aka. 'Not Submitted') status filter option AND 'Second Diff Tag' differentiation tag filter option together
        // Important info: Filter groups behave with AND logic between them and OR logic within them.
        Log.d(STEP_TAG, "Click on the filter icon on the top-right corner again.")
        assignmentSubmissionListPage.clickFilterButton()

        Log.d(STEP_TAG, "Select the 'Missing' status filter and 'Second Diff Tag' differentiation tag and click on 'Done'.")
        assignmentSubmissionListPage.clickFilterNotSubmitted()
        assignmentSubmissionListPage.clickDifferentiationTagFilter("Second Diff Tag")
        assignmentSubmissionListPage.clickFilterDialogDone()

        Log.d(ASSERTION_TAG, "Assert that there is 1 submission displayed, one for '${student2.name}' student since we applied the 'Missing' status filter and 'Second Diff Tag' differentiation tag filter simultaneously.")
        assignmentSubmissionListPage.assertHasSubmission(1)
        assignmentSubmissionListPage.assertHasStudentSubmission(student2)
        assignmentSubmissionListPage.assertStudentSubmissionNotDisplayed(student)
        assignmentSubmissionListPage.assertStudentSubmissionNotDisplayed(studentWithoutTag)

        // Check 'AMAZING' custom status filter option AND 'Second Diff Tag' differentiation tag filter option together to check the AND logic between filter groups
        Log.d(STEP_TAG, "Click on the filter icon on the top-right corner again.")
        assignmentSubmissionListPage.clickFilterButton()

        Log.d(STEP_TAG, "Unselect the 'Missing' status filter select 'AMAZING' custom status filter and click on 'Done'.")
        assignmentSubmissionListPage.clickFilterNotSubmitted()
        assignmentSubmissionListPage.clickFilterCustomStatus("AMAZING")
        assignmentSubmissionListPage.clickFilterDialogDone()

        Log.d(ASSERTION_TAG, "Assert that there is no submission displayed since there is no student submission which has the 'AMAZING' custom status and the 'Second Diff Tag' differentiation tag simultaneously.")
        assignmentSubmissionListPage.assertHasNoSubmission()
        assignmentSubmissionListPage.assertEmptyViewDisplayed()
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