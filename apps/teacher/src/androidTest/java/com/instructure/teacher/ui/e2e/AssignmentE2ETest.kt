/*
 * Copyright (C) 2021 - present Instructure, Inc.
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

import android.os.SystemClock.sleep
import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.rule.GrantPermissionRule
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.FileUploadType
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.assertContainsText
import com.instructure.espresso.page.onViewWithId
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.seedAssignmentSubmission
import com.instructure.teacher.ui.utils.seedAssignments
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import com.instructure.teacher.ui.utils.uploadTextFile
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AssignmentE2ETest : TeacherComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.CAMERA
    )

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.E2E)
    fun testAssignmentsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(teachers = 1, courses = 1, students = 3, favoriteCourses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student = data.studentsList[0]
        val gradedStudent = data.studentsList[1]

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId}.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Open ${course.name} course.")
        dashboardPage.openCourse(course.name)

        Log.d(STEP_TAG,"Navigate to ${course.name} course's Assignments Tab and assert that there isn't any assignment displayed.")
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.assertDisplaysNoAssignmentsView()

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for ${course.name} course.")
        val assignment = seedAssignments(
                courseId = course.id,
                dueAt = 1.days.fromNow.iso8601,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                pointsPossible = 15.0
        )

        Log.d(PREPARATION_TAG,"Seeding 'Quiz' assignment for ${course.name} course.")
        val quizAssignment = seedAssignments(
            courseId = course.id,
            dueAt = 1.days.fromNow.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_QUIZ),
            teacherToken = teacher.token,
            pointsPossible = 15.0
        )

        Log.d(STEP_TAG,"Refresh Assignment List Page and assert that the previously seeded ${assignment[0].name} assignment has been displayed." +
                "Assert that the needs grading count under the corresponding assignment is 1.")
        composeTestRule.waitForIdle()
        assignmentListPage.refreshAssignmentList()
        composeTestRule.waitForIdle()
        assignmentListPage.assertHasAssignment(assignment[0])

        Log.d(STEP_TAG,"Click on ${assignment[0].name} assignment and assert the numbers of 'Not Submitted' and 'Needs Grading' submissions.")
        assignmentListPage.clickAssignment(assignment[0])
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertNotSubmitted(3,3)
        assignmentDetailsPage.assertNeedsGrading(0,3)

        Log.d(STEP_TAG, "Open the 'All Submissions' page and click on the filter icon on the top-right corner.")
        assignmentDetailsPage.clickAllSubmissions()
        assignmentSubmissionListPage.clickFilterButton()

        Log.d(STEP_TAG, "Filter by section (the ${course.name} course).")
        assignmentSubmissionListPage.clickFilterButton()

        Log.d(ASSERTION_TAG, "Assert that the 'Filter By...' section dialog details displayed correctly. Filter by the '${course.name}' (course) section.")
        assignmentSubmissionListPage.filterBySection(course.name)
        assignmentSubmissionListPage.clickFilterDialogOk()

        Log.d(ASSERTION_TAG, "Assert that the 'Clear filter' button is displayed as we set some filter. Assert that the filter label text is the 'All Submissions' text plus the '${course.name}' course name.")
        assignmentSubmissionListPage.assertFilterLabelText("All Submissions")

        Log.d(STEP_TAG, "Open '${student.name}' student's submission.")
        assignmentSubmissionListPage.clickSubmission(student)

        Log.d(ASSERTION_TAG, "Assert that the speed grader page of '${student.name}' student is displayed and the title is the student's name, the subtitle is 'Not submitted yet'.")
        speedGraderPage.assertSpeedGraderToolbarTitle(student.name, "Not submitted yet")

        Log.d(STEP_TAG, "Navigate back to the Assignment Submission List Page and clear the filter.")
        Espresso.pressBack()
        assignmentSubmissionListPage.clickFilterButton()
        assignmentSubmissionListPage.filterBySection(course.name)
        assignmentSubmissionListPage.clickFilterDialogOk()

        Log.d(ASSERTION_TAG, "Assert that the 'Clear filter' button is NOT displayed as we just cleared the filter. Assert that the filter label text 'All Submission'.")
        assignmentSubmissionListPage.assertFilterLabelText("All Submissions")

        Log.d(STEP_TAG,"Navigate back to Assignment List Page, open the '${assignment[0].name}' assignment and publish it. Click on Save.")
        Espresso.pressBack()
        composeTestRule.waitForIdle()
        Espresso.pressBack()
        composeTestRule.waitForIdle()
        assignmentListPage.clickAssignment(assignment[0])
        assignmentDetailsPage.openEditPage()
        editAssignmentDetailsPage.clickPublishSwitch()
        editAssignmentDetailsPage.saveAssignment()

        Log.d(STEP_TAG,"Assert that the '${assignment[0].name}' assignment has been published.")
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertPublishedStatus(false)

        Log.d(STEP_TAG,"Open Edit Page and re-publish the assignment, then click on Save. Assert that the assignment is published automatically, without refresh.")
        assignmentDetailsPage.openEditPage()
        editAssignmentDetailsPage.clickPublishSwitch()
        editAssignmentDetailsPage.saveAssignment()
        assignmentDetailsPage.assertPublishedStatus(true)

        Log.d(STEP_TAG,"Navigate back to Assignment List page. Open edit quiz page and publish ${quizAssignment[0].name} quiz assignment. Click on Save.")
        Espresso.pressBack()
        assignmentListPage.clickAssignment(quizAssignment[0])
        quizDetailsPage.openEditPage()
        editAssignmentDetailsPage.clickPublishSwitch()
        editAssignmentDetailsPage.saveAssignment()
        quizDetailsPage.assertQuizUnpublished()

        Log.d(STEP_TAG, "Navigate back to Assignment List page. Assert that the '${quizAssignment[0].name}' quiz displays as UNPUBLISHED. Open the quiz assignment again.")
        Espresso.pressBack()
        assignmentListPage.refreshAssignmentList()
        assignmentListPage.assertPublishedState(quizAssignment[0].name, false)
        assignmentListPage.clickAssignment(quizAssignment[0])

        Log.d(STEP_TAG, "Open Edit Page and re-publish the assignment, then click on Save. Assert that the quiz assignment is published automatically.")
        quizDetailsPage.openEditPage()
        editAssignmentDetailsPage.clickPublishSwitch()
        editAssignmentDetailsPage.saveAssignment()
        quizDetailsPage.assertQuizPublished()

        Log.d(STEP_TAG, "Navigate back to Assignment List page. Assert that the '${quizAssignment[0].name}' quiz displays as PUBLISHED.")
        Espresso.pressBack()
        assignmentListPage.refreshAssignmentList()
        assignmentListPage.assertPublishedState(quizAssignment[0].name, true)

        Log.d(STEP_TAG, "Open the '${assignment[0].name}' assignment.")
        assignmentListPage.clickAssignment(assignment[0])

        Log.d(PREPARATION_TAG,"Seed a submission for ${student.name} student.")
        seedAssignmentSubmission(
                submissionSeeds = listOf(SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )),
                assignmentId = assignment[0].id,
                courseId = course.id,
                studentToken = student.token
        )

        Log.d(STEP_TAG,"Refresh the page. Assert that because of the previously seeded submission, the number of 'Needs Grading' is increased and the number of 'Not Submitted' is decreased.")
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertNotSubmitted(2,3)
        assignmentDetailsPage.assertNeedsGrading(1,3)

        Log.d(PREPARATION_TAG,"Seed a submission for ${gradedStudent.name} student.")
        seedAssignmentSubmission(
                submissionSeeds = listOf(SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )),
                assignmentId = assignment[0].id,
                courseId = course.id,
                studentToken = gradedStudent.token
        )

        Log.d(STEP_TAG,"Refresh the page. Assert that because of the other submission there will be 2 'Needs Grading' and only 1 remains as 'Not Submitted'.")
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertNotSubmitted(1,3)
        assignmentDetailsPage.assertNeedsGrading(2,3)

        Log.d(PREPARATION_TAG,"Grade the previously seeded submission for '${gradedStudent.name}' student.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, assignment[0].id, gradedStudent.id, postedGrade = "15")

        Log.d(STEP_TAG,"Refresh the page. Assert that the number of 'Graded' is increased and the number of 'Not Submitted' and 'Needs Grading' are decreased.")
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertNotSubmitted(1,3)
        assignmentDetailsPage.assertNeedsGrading(1,3)
        assignmentDetailsPage.assertHasGraded(1,3)

        Log.d(STEP_TAG, "Navigate back to Assignment List Page. Assert that the '${assignment[0].name}' assignment has 1 'Needs Grading' submission.")
        Espresso.pressBack()
        assignmentListPage.refreshAssignmentList()
        assignmentListPage.assertHasAssignment(quizAssignment[0], needsGradingCount = null)
        assignmentListPage.assertHasAssignment(assignment[0], needsGradingCount = 1)

        Log.d(STEP_TAG,"Click on Search button and type '${quizAssignment[0].name}' to the search input field.")
        assignmentListPage.searchBar.clickOnSearchButton()
        assignmentListPage.searchBar.typeToSearchBar(quizAssignment[0].name)

        Log.d(STEP_TAG, "Assert that the '${quizAssignment[0].name}' quiz assignment is the only one which is displayed because it matches the search text.")
        assignmentListPage.assertHasAssignment(quizAssignment[0], needsGradingCount = null)
        assignmentListPage.assertAssignmentNotDisplayed(assignment[0].name)

        Log.d(STEP_TAG,"Clear search input field value and assert if both of the assignment are displayed again on the Assignment List Page.")
        assignmentListPage.searchBar.clickOnClearSearchButton()
        assignmentListPage.assertHasAssignment(assignment[0], needsGradingCount = 1)
        assignmentListPage.assertHasAssignment(quizAssignment[0], needsGradingCount = null)

        val newAssignmentName = "New Assignment Name"
        Log.d(STEP_TAG,"Edit ${assignment[0].name} assignment's name  to: $newAssignmentName.")
        assignmentListPage.clickAssignment(assignment[0])
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertAssignmentDetails(assignment[0])

        Log.d(STEP_TAG,"Open Edit Page again. Change the assignment's name to $newAssignmentName and it's description to 'assignment test description', then save.")
        assignmentDetailsPage.openEditPage()
        editAssignmentDetailsPage.clickAssignmentNameEditText()
        editAssignmentDetailsPage.editAssignmentName(newAssignmentName)
        editAssignmentDetailsPage.editDescription("assignment test description")
        editAssignmentDetailsPage.saveAssignment()

        Log.d(STEP_TAG,"Refresh the page. Assert that the name and description of the assignment has been changed to $newAssignmentName.")
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertAssignmentName(newAssignmentName)
        assignmentDetailsPage.assertDisplaysDescription("assignment test description")

        Log.d(STEP_TAG,"Edit $newAssignmentName assignment's points to 20.")
        assignmentDetailsPage.openEditPage()
        editAssignmentDetailsPage.clickPointsPossibleEditText()
        editAssignmentDetailsPage.editAssignmentPoints(20.0)

        Log.d(STEP_TAG,"Change grade type to 'Percentage'.")
        editAssignmentDetailsPage.clickOnDisplayGradeAsSpinner()
        editAssignmentDetailsPage.selectGradeType("Percentage")

        Log.d(STEP_TAG,"Click on the 'Due Time' section and edit the hour and minutes to 1:30 PM." +
                "Assert that the changes has been applied on Edit Assignment Details page.")
        editAssignmentDetailsPage.clickEditDueDate()
        editAssignmentDetailsPage.editDate(2022,12,12)
        editAssignmentDetailsPage.clickEditDueTime()
        editAssignmentDetailsPage.editTime(1, 30)
        editAssignmentDetailsPage.assertTimeChanged(1, 30, R.id.dueTime)

        Log.d(STEP_TAG,"Click on 'Assigned To' spinner and select ${student.name} besides 'Everyone'." +
                "Assert that ${student.name} and 'Everyone else' is selected as well.")
        editAssignmentDetailsPage.editAssignees()
        assigneeListPage.assertAssigneesSelected(listOf("Everyone"))
        assigneeListPage.toggleAssignees(listOf(student.name))
        val expectedAssignees = listOf(student.name, "Everyone else")
        assigneeListPage.assertAssigneesSelected(expectedAssignees)

        Log.d(STEP_TAG,"Save and close the assignee list. Assert that on the Assignment Details Page both the ${student.name} and the 'Everyone else' values are set.")
        assigneeListPage.saveAndClose()
        val assignText = editAssignmentDetailsPage.onViewWithId(R.id.assignTo)
        for (assignee in expectedAssignees) assignText.assertContainsText(assignee)

        Log.d(STEP_TAG,"Save the assignment.")
        editAssignmentDetailsPage.saveAssignment()

        Log.d(STEP_TAG,"Refresh the page. Assert that the points of $newAssignmentName assignment has been changed to 20.")
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertAssignmentPointsChanged("20")

        Log.d(STEP_TAG,"Assert that there are multiple due dates set, so the 'Multiple Due Dates' string is displayed on the 'Due Dates' section.")
        assignmentDetailsPage.assertMultipleDueDates()

        Log.d(STEP_TAG,"Open Due Dates Page and assert that there are 2 different due dates set.")
        assignmentDetailsPage.openAllDatesPage()
        assignmentDueDatesPage.assertDueDatesCount(2)

        Log.d(STEP_TAG,"Assert that there is a due date set for '${student.name}' student especially and another one for everyone else.")
        assignmentDueDatesPage.assertDueFor(student.name)
        assignmentDueDatesPage.assertDueFor(R.string.everyone_else)

        val dueDateForEveryoneElse = "Dec 12 at 1:30 AM"
        val dueDateForStudentSpecially = "Dec 12 at 9:30 AM"
        Log.d(STEP_TAG,"Assert that the there is a due date with '$dueDateForEveryoneElse' value and another one with '$dueDateForStudentSpecially'.")
        assignmentDueDatesPage.assertDueDateTime("Due $dueDateForEveryoneElse")
        assignmentDueDatesPage.assertDueDateTime("Due $dueDateForStudentSpecially")
    }

    @E2E
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.COMMENTS, TestCategory.E2E)
    @Stub("Failing on CI, needs to be fixed in ticket MBL-18749")
    fun testMediaCommentsE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding assignment for ${course.name} course.")
        val assignment = AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
            courseId = course.id,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
            gradingType = GradingType.POINTS,
            teacherToken = teacher.token,
            pointsPossible = 15.0,
            dueAt = 1.days.fromNow.iso8601
        ))

        Log.d(PREPARATION_TAG,"Submit '${assignment.name}' assignment for '${student.name}' student.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, assignment.id,
            submissionSeedsList = listOf(SubmissionsApi.SubmissionSeedInfo(
                amount = 1,
                submissionType = SubmissionType.ONLINE_TEXT_ENTRY
            ))
        )

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId}.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Select ${course.name} course and navigate to it's Assignments Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.openAssignmentsTab()

        Log.d(STEP_TAG,"Click on ${assignment.name} assignment.")
        assignmentListPage.clickAssignment(assignment)

        Log.d(STEP_TAG,"Open ${student.name} student's submission and switch to submission details Comments Tab.")
        assignmentDetailsPage.clickAllSubmissions()
        assignmentSubmissionListPage.clickSubmission(student)
        speedGraderPage.selectCommentsTab()

        Log.d(STEP_TAG, "Send an audio comment and assert that is displayed among the comments.")
        speedGraderCommentsPage.sendAudioComment()
        sleep(5000) // wait for audio comment submission to propagate
        speedGraderCommentsPage.assertAudioCommentDisplayed()

        Log.d(STEP_TAG, "Send a video comment and assert that is displayed among the comments.")
        speedGraderCommentsPage.sendVideoComment()
        sleep(5000) // wait for video comment submission to propagate
        speedGraderCommentsPage.assertVideoCommentDisplayed()

        Log.d(STEP_TAG, "Click on the previously uploaded audio comment. Assert that the media comment preview (and the 'Play button') is displayed.")
        speedGraderCommentsPage.clickOnAudioComment()
        speedGraderCommentsPage.assertMediaCommentPreviewDisplayed()

        Log.d(STEP_TAG, "Navigate back. Click on the previously uploaded video comment. Assert that the media comment preview (and the 'Play button') is displayed.")
        Espresso.pressBack()
        speedGraderCommentsPage.clickOnVideoComment()
        speedGraderCommentsPage.assertMediaCommentPreviewDisplayed()
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.COMMENTS, TestCategory.E2E)
    fun testAddFileCommentE2E() {

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

        Log.d(PREPARATION_TAG,"Seed a comment attachment upload.")
        val commentUploadInfo = uploadTextFile(
            assignmentId = assignment.id,
            courseId = course.id,
            token = student.token,
            fileUploadType = FileUploadType.COMMENT_ATTACHMENT
        )

        Log.d(PREPARATION_TAG, "Comment a text file as a teacher to the '${student.name}' student's submission of the '${assignment.name}' assignment.")
        SubmissionsApi.commentOnSubmission(course.id, student.token, assignment.id, fileIds = mutableListOf(commentUploadInfo.id))

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId}.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Select ${course.name} course and navigate to it's Assignments Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.openAssignmentsTab()

        Log.d(STEP_TAG,"Click on ${assignment.name} assignment and navigate to Submissions Page.")
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.clickAllSubmissions()

        Log.d(STEP_TAG,"Click on ${student.name} student's submission.")
        assignmentSubmissionListPage.clickSubmission(student)

        Log.d(STEP_TAG,"Assert that ${submissionUploadInfo.fileName} file. Navigate to Comments Tab and ${commentUploadInfo.fileName} comment attachment is displayed.")
        speedGraderPage.selectCommentsTab()
        speedGraderPage.assertCommentAttachmentDisplayedCommon(commentUploadInfo.fileName, student.shortName)
    }

}