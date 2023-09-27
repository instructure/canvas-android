/*
 * Copyright (C) 2020 - present Instructure, Inc.
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

import android.os.SystemClock.sleep
import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.rule.GrantPermissionRule
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.AssignmentGroupsApi
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.AttachmentApiModel
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.FileUploadType
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionApiModel
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.pages.AssignmentListPage
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.ViewUtils
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import com.instructure.student.ui.utils.uploadTextFile
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AssignmentsE2ETest: StudentTest() {
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
    fun testPointsGradeTextAssignmentE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for ${course.name} course.")
        val pointsTextAssignment = createAssignment(course.id, teacher, GradingType.POINTS, 15.0, 1.days.fromNow.iso8601)

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Select course: ${course.name}.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG,"Navigate to course Assignments Page.")
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG,"Verify that our assignments are present, along with any grade/date info. Click on assignment ${pointsTextAssignment.name}.")
        assignmentListPage.assertHasAssignment(pointsTextAssignment)
        assignmentListPage.clickAssignment(pointsTextAssignment)

        Log.d(STEP_TAG, "Assert that the corresponding views are displayed on the Assignment Details Page, and there is no submission yet.")
        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertStatusNotSubmitted()

        Log.d(STEP_TAG, "Assert that 'Submission & Rubric' label is displayed and navigate to Submission Details Page.")
        assignmentDetailsPage.assertSubmissionAndRubricLabel()
        assignmentDetailsPage.goToSubmissionDetails()

        Log.d(STEP_TAG, "Assert that there is no submission yet for the '${pointsTextAssignment.name}' assignment.")
        submissionDetailsPage.assertNoSubmissionEmptyView()
        Espresso.pressBack()

        Log.d(PREPARATION_TAG,"Submit assignment: ${pointsTextAssignment.name} for student: ${student.name}.")
        submitAssignment(pointsTextAssignment, course, student)

        assignmentDetailsPage.refresh()
        assignmentDetailsPage.assertStatusSubmitted()
        assignmentDetailsPage.assertSubmissionAndRubricLabel()

        Log.d(PREPARATION_TAG,"Grade submission: ${pointsTextAssignment.name} with 13 points.")
        val textGrade = gradeSubmission(teacher, course, pointsTextAssignment.id, student, "13")

        Log.d(STEP_TAG,"Refresh the page. Assert that the assignment ${pointsTextAssignment.name} has been graded with 13 points.")
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.assertAssignmentGraded("13")

        Log.d(STEP_TAG,"Navigate back to Assignments Page and assert that the assignment ${pointsTextAssignment.name} can be seen there with the corresponding grade.")
        Espresso.pressBack()
        assignmentListPage.refresh()
        assignmentListPage.assertHasAssignment(pointsTextAssignment, textGrade.grade)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.E2E)
    fun testLetterGradeTextAssignmentE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for ${course.name} course.")
        val letterGradeTextAssignment = createAssignment(course.id, teacher, GradingType.LETTER_GRADE, 20.0)

        Log.d(PREPARATION_TAG,"Submit assignment: ${letterGradeTextAssignment.name} for student: ${student.name}.")
        submitAssignment(letterGradeTextAssignment, course, student)

        Log.d(PREPARATION_TAG,"Grade submission: ${letterGradeTextAssignment.name} with 13 points.")
        val submissionGrade = gradeSubmission(teacher, course, letterGradeTextAssignment.id, student, "13")

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Select ${course.name} course and navigate to it's Assignments Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG,"Assert that ${letterGradeTextAssignment.name} assignment is displayed with the corresponding grade: ${submissionGrade.grade}.")
        assignmentListPage.assertHasAssignment(letterGradeTextAssignment, submissionGrade.grade)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.E2E)
    fun testPercentageFileAssignmentWithCommentE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding assignment for ${course.name} course.")
        val percentageFileAssignment = createAssignment(course.id, teacher, GradingType.PERCENT, 25.0, allowedExtensions = listOf("txt", "pdf", "jpg"), submissionType = listOf(SubmissionType.ONLINE_UPLOAD))

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Select ${course.name} course and navigate to it's Assignments Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG,"Assert that ${percentageFileAssignment.name} assignment is displayed.")
        assignmentListPage.assertHasAssignment(percentageFileAssignment)

        Log.d(STEP_TAG,"Click on ${percentageFileAssignment.name} assignment.")
        assignmentListPage.clickAssignment(percentageFileAssignment)

        Log.d(PREPARATION_TAG, "Seed a text file.")
        val uploadInfo = uploadTextFile(courseId = course.id, assignmentId = percentageFileAssignment.id, token = student.token, fileUploadType = FileUploadType.ASSIGNMENT_SUBMISSION)

        Log.d(PREPARATION_TAG,"Submit ${percentageFileAssignment.name} assignment for ${student.name} student.")
        submitCourseAssignment(course, percentageFileAssignment, uploadInfo, student)

        Log.d(STEP_TAG,"Refresh the page. Assert that the ${percentageFileAssignment.name} assignment has been submitted.")
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.assertAssignmentSubmitted()

        Log.d(PREPARATION_TAG,"Grade ${percentageFileAssignment.name} assignment with 22 percentage.")
        gradeSubmission(teacher, course, percentageFileAssignment, student,"22")

        Log.d(STEP_TAG,"Refresh the page. Assert that the ${percentageFileAssignment.name} assignment has been graded with 22 percentage.")
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.assertAssignmentGraded("22")

        Log.d(STEP_TAG,"Navigate to submission details Comments Tab.")
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.openComments()

        Log.d(STEP_TAG,"Assert that ${uploadInfo.fileName} file has been displayed as a comment.")
        submissionDetailsPage.assertCommentDisplayed(uploadInfo.fileName, student)

        val newComment = "My comment!!"
        Log.d(STEP_TAG,"Add a new comment ($newComment) and send it.")
        submissionDetailsPage.addAndSendComment(newComment)
        sleep(2000) // Give the comment time to propagate

        Log.d(STEP_TAG,"Assert that $newComment is displayed.")
        submissionDetailsPage.assertCommentDisplayed(newComment, student)

        Log.d(STEP_TAG, "Open the 'Files' tab of the submission and assert if the file is present there as well.")
        submissionDetailsPage.openFiles()

        Log.d(STEP_TAG,"Assert that ${uploadInfo.fileName} file has been displayed.")
        submissionDetailsPage.assertFileDisplayed(uploadInfo.fileName)
    }

    private fun submitCourseAssignment(
        course: CourseApiModel,
        percentageFileAssignment: AssignmentApiModel,
        uploadInfo: AttachmentApiModel,
        student: CanvasUserApiModel
    ) {
        SubmissionsApi.submitCourseAssignment(
            submissionType = SubmissionType.ONLINE_UPLOAD,
            courseId = course.id,
            assignmentId = percentageFileAssignment.id,
            fileIds = listOf(uploadInfo.id).toMutableList(),
            studentToken = student.token
        )
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.E2E)
    fun testMultipleAssignmentsE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding assignment for ${course.name} course.")
        val letterGradeTextAssignment = createAssignment(course.id, teacher, GradingType.LETTER_GRADE, 20.0)

        Log.d(PREPARATION_TAG,"Submit ${letterGradeTextAssignment.name} assignment for ${student.name} student.")
        submitAssignment(letterGradeTextAssignment, course, student)

        Log.d(PREPARATION_TAG,"Grade ${letterGradeTextAssignment.name} assignment with 16.")
        gradeSubmission(teacher, course, letterGradeTextAssignment, student, "16")

        Log.d(PREPARATION_TAG,"Seeding assignment for ${course.name} course.")
        val pointsTextAssignment = createAssignment(course.id, teacher, GradingType.POINTS, 15.0, 1.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG,"Submit ${pointsTextAssignment.name} assignment for ${student.name} student.")
        submitAssignment(pointsTextAssignment, course, student)

        Log.d(PREPARATION_TAG,"Grade ${pointsTextAssignment.name} assignment with 13 points.")
        gradeSubmission(teacher, course, pointsTextAssignment.id, student, "13")

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Select ${course.name} course and navigate to it's Assignments Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG,"Assert that ${pointsTextAssignment.name} assignment is displayed with the corresponding grade: 13.")
        assignmentListPage.assertHasAssignment(pointsTextAssignment,"13")

        Log.d(STEP_TAG,"Assert that ${letterGradeTextAssignment.name} assignment is displayed with the corresponding grade: 16.")
        assignmentListPage.assertHasAssignment(letterGradeTextAssignment, "16")
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.E2E)
    fun testFilterAndSortAssignmentsE2E() {
        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course.")
        val upcomingAssignment = createAssignment(course.id, teacher, GradingType.LETTER_GRADE, 20.0)

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course.")
        val missingAssignment = createAssignment(course.id, teacher, GradingType.LETTER_GRADE, 20.0, 2.days.ago.iso8601)

        Log.d(PREPARATION_TAG,"Seeding a GRADED assignment for ${course.name} course.")
        val gradedAssignment = createAssignment(course.id, teacher, GradingType.LETTER_GRADE, 20.0)

        Log.d(PREPARATION_TAG,"Grade the '${gradedAssignment.name}' with '11' points out of 20.")
        gradeSubmission(teacher, course, gradedAssignment, student, "11")

        Log.d(PREPARATION_TAG,"Create an Assignment Group for '${course.name}' course.")
        val assignmentGroup = createAssignmentGroup(teacher, course)

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course.")
        val otherTypeAssignment = createAssignment(course.id, teacher, GradingType.LETTER_GRADE, 20.0, assignmentGroupId = assignmentGroup.id)

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Select ${course.name} course and navigate to it's Assignments Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()
        assignmentListPage.assertPageObjects()

        Log.d(STEP_TAG, "Assert that the corresponding assignment are displayed on the Assignment List Page.")
        assignmentListPage.assertHasAssignment(upcomingAssignment)
        assignmentListPage.assertHasAssignment(missingAssignment)
        assignmentListPage.assertHasAssignment(otherTypeAssignment)
        assignmentListPage.assertHasAssignment(gradedAssignment)

        Log.d(STEP_TAG, "Click on the 'Filter' menu on the toolbar.")
        assignmentListPage.clickFilterMenu()

        Log.d(STEP_TAG, "Filter the MISSING assignments.")
        assignmentListPage.filterAssignments(AssignmentListPage.AssignmentType.MISSING)

        Log.d(STEP_TAG, "Assert that the '${missingAssignment.name}' MISSING assignment is displayed and the others at NOT.")
        assignmentListPage.assertHasAssignment(missingAssignment)
        assignmentListPage.assertAssignmentNotDisplayed(upcomingAssignment.name)
        assignmentListPage.assertAssignmentNotDisplayed(otherTypeAssignment.name)
        assignmentListPage.assertAssignmentNotDisplayed(gradedAssignment.name)

        Log.d(STEP_TAG, "Click on the 'Filter' menu on the toolbar.")
        assignmentListPage.clickFilterMenu()

        Log.d(STEP_TAG, "Filter the GRADED assignments.")
        assignmentListPage.filterAssignments(AssignmentListPage.AssignmentType.GRADED)

        Log.d(STEP_TAG, "Assert that the '${gradedAssignment.name}' GRADED assignment is displayed.")
        assignmentListPage.assertHasAssignment(gradedAssignment)
        assignmentListPage.assertAssignmentNotDisplayed(upcomingAssignment.name)
        assignmentListPage.assertAssignmentNotDisplayed(otherTypeAssignment.name)
        assignmentListPage.assertAssignmentNotDisplayed(missingAssignment.name)

        Log.d(STEP_TAG, "Click on the 'Filter' menu on the toolbar.")
        assignmentListPage.clickFilterMenu()

        Log.d(STEP_TAG, "Set back the filter to show ALL the assignments like by default.")
        assignmentListPage.filterAssignments(AssignmentListPage.AssignmentType.ALL)
        assignmentListPage.assertPageObjects()

        Log.d(STEP_TAG, "Assert that by default, the 'Sort by Time' is selected.")
        assignmentListPage.assertSortByButtonShowsSortByTime()

        Log.d(STEP_TAG, "Select 'Sort by Type' sorting.")
        assignmentListPage.selectSortByType()

        Log.d(STEP_TAG, "Assert that after the selection, the button is really changed to 'Sort by Type'.")
        assignmentListPage.assertPageObjects()
        assignmentListPage.assertSortByButtonShowsSortByType()

        Log.d(STEP_TAG, "Assert that still all the assignment are displayed and the corresponding groups (Assignments, Discussions) as well.")
        assignmentListPage.assertAssignmentItemCount(4, 2) //Two groups: Assignments and Discussions
        assignmentListPage.assertAssignmentGroupDisplayed("Assignments")
        assignmentListPage.assertAssignmentGroupDisplayed("Discussions")
        assignmentListPage.assertHasAssignment(upcomingAssignment)
        assignmentListPage.assertHasAssignment(missingAssignment)
        assignmentListPage.assertHasAssignment(otherTypeAssignment)
        assignmentListPage.assertHasAssignment(gradedAssignment)

        Log.d(STEP_TAG, "Collapse the 'Assignments' assignment group and assert that it's items are not displayed when the group is collapsed.")
        assignmentListPage.expandCollapseAssignmentGroup("Assignments")
        assignmentListPage.assertAssignmentNotDisplayed(upcomingAssignment.name)
        assignmentListPage.assertAssignmentNotDisplayed(missingAssignment.name)
        assignmentListPage.assertAssignmentNotDisplayed(gradedAssignment.name)

        Log.d(STEP_TAG, "Assert that the other group's item is still displayed.")
        assignmentListPage.assertHasAssignment(otherTypeAssignment)

        Log.d(STEP_TAG, "Expand the 'Assignments' assignment group.")
        assignmentListPage.expandCollapseAssignmentGroup("Assignments")

        Log.d(STEP_TAG, "Click on the 'Search' (magnifying glass) icon at the toolbar.")
        assignmentListPage.searchable.clickOnSearchButton()

        Log.d(STEP_TAG, "Type the name of the '${missingAssignment.name}' assignment.")
        assignmentListPage.searchable.typeToSearchBar(missingAssignment.name.drop(5))

        Log.d(STEP_TAG, "Assert that the '${missingAssignment.name}' assignment has been found by previously typed search string.")
        sleep(3000) // Allow the search input to propagate
        assignmentListPage.assertHasAssignment(missingAssignment)
        assignmentListPage.assertAssignmentNotDisplayed(upcomingAssignment.name)
        assignmentListPage.assertAssignmentNotDisplayed(otherTypeAssignment.name)
        assignmentListPage.assertAssignmentNotDisplayed(gradedAssignment.name)
    }

    private fun createAssignmentGroup(
        teacher: CanvasUserApiModel,
        course: CourseApiModel
    ) = AssignmentGroupsApi.createAssignmentGroup(
        token = teacher.token,
        courseId = course.id,
        name = "Discussions",
        position = null,
        groupWeight = null,
        sisSourceId = null
    )

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.COMMENTS, TestCategory.E2E)
    fun testMediaCommentsE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding assignment for ${course.name} course.")
        val assignment = createAssignment(course.id, teacher, GradingType.POINTS, 15.0, 1.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG,"Submit ${assignment.name} assignment for ${student.name} student.")
        submitAssignment(assignment, course, student)

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Select ${course.name} course and navigate to it's Assignments Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG,"Click on ${assignment.name} assignment.")
        assignmentListPage.clickAssignment(assignment)

        Log.d(STEP_TAG,"Navigate to submission details Comments Tab.")
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.openComments()

        submissionDetailsPage.addAndSendVideoComment()
        sleep(3000) // wait for video comment submission to propagate
        submissionDetailsPage.assertVideoCommentDisplayed()

        Log.d(STEP_TAG,"Send an audio comment.")
        submissionDetailsPage.addAndSendAudioComment()
        sleep(3000) // Wait for audio comment submission to propagate

        Log.d(STEP_TAG,"Assert that the audio comment has been displayed.")
        submissionDetailsPage.assertAudioCommentDisplayed()
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.COMMENTS, TestCategory.E2E)
    fun testAddFileCommentE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding assignment for ${course.name} course.")
        val assignment = createAssignment(course.id, teacher, GradingType.POINTS, 15.0, 1.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG,"Submit ${assignment.name} assignment for ${student.name} student.")
        submitAssignment(assignment, course, student)

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Seed a comment attachment upload.")
        val commentUploadInfo = uploadTextFile(
            assignmentId = assignment.id,
            courseId = course.id,
            token = student.token,
            fileUploadType = FileUploadType.COMMENT_ATTACHMENT
        )
        commentOnSubmission(student, course, assignment, commentUploadInfo)

        Log.d(STEP_TAG,"Select ${course.name} course and navigate to it's Assignments Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG,"Click on ${assignment.name} assignment.")
        assignmentListPage.clickAssignment(assignment)

        Log.d(STEP_TAG,"Assert that ${commentUploadInfo.fileName} file is displayed as a comment by ${student.name} student.")
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.openComments()
        submissionDetailsPage.assertCommentAttachmentDisplayed(commentUploadInfo.fileName, student)

        Log.d(STEP_TAG,"Navigate to Submission Details Page and open Files Tab.")
        submissionDetailsPage.openFiles()
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.E2E)
    fun testSubmissionAttemptSelection() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for ${course.name} course.")
        val pointsTextAssignment = createAssignment(course.id, teacher, GradingType.POINTS, 15.0, 1.days.fromNow.iso8601)

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Select course: ${course.name}.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG, "Navigate to course Assignments Page.")
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG, "Verify that our assignments are present," +
                "along with any grade/date info. Click on assignment ${pointsTextAssignment.name}.")
        assignmentListPage.assertHasAssignment(pointsTextAssignment)
        assignmentListPage.clickAssignment(pointsTextAssignment)

        Log.d(PREPARATION_TAG,"Submit assignment: ${pointsTextAssignment.name} for student: ${student.name}.")
        submitAssignment(pointsTextAssignment, course, student)

        Log.d(STEP_TAG, "Refresh the page.")
        assignmentDetailsPage.refresh()

        Log.d(STEP_TAG, "Assert that when only there is one attempt, the spinner is not displayed.")
        assignmentDetailsPage.assertNoAttemptSpinner()

        Log.d(PREPARATION_TAG,"Generate another submission for assignment: ${pointsTextAssignment.name} for student: ${student.name}.")
        submitAssignment(pointsTextAssignment, course, student)

        Log.d(STEP_TAG, "Refresh the page.")
        assignmentDetailsPage.refresh()

        Log.d(STEP_TAG, "Assert that the spinner is displayed and the last/newest attempt ('Attempt 2') is selected.")
        assignmentDetailsPage.assertAttemptSpinnerDisplayed()
        assignmentDetailsPage.assertSelectedAttempt(2)

        Log.d(STEP_TAG, "Go to the Submission Details Page and assert that the selected attempt is 'Attempt 2'.")
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.assertSelectedAttempt("Attempt 2")

        Log.d(STEP_TAG, "Navigate back to the Assignment Details Page. Select the other attempt, 'Attempt 1', and assert if it's displayed as the selected one.")
        Espresso.pressBack()
        assignmentDetailsPage.selectAttempt(1)
        assignmentDetailsPage.assertSelectedAttempt(1)

        Log.d(STEP_TAG, "Go to the Submission Details Page and assert that the selected attempt is 'Attempt 1'.")
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.assertSelectedAttempt("Attempt 1")
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SUBMISSIONS, TestCategory.E2E)
    fun testCommentsBelongToSubmissionAttempts() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for ${course.name} course.")
        val pointsTextAssignment = createAssignment(course.id, teacher,  GradingType.POINTS, 15.0, 1.days.fromNow.iso8601)

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Select course: ${course.name}.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG,"Navigate to course Assignments Page.")
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG,"Verify that our assignments are present, along with any grade/date info. Click on assignment ${pointsTextAssignment.name}.")
        assignmentListPage.assertHasAssignment(pointsTextAssignment)
        assignmentListPage.clickAssignment(pointsTextAssignment)

        Log.d(STEP_TAG, "Assert that the corresponding views are displayed on the Assignment Details Page, and there is no submission yet.")
        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertStatusNotSubmitted()

        Log.d(STEP_TAG, "Assert that 'Submission & Rubric' label is displayed and navigate to Submission Details Page.")
        assignmentDetailsPage.assertSubmissionAndRubricLabel()
        assignmentDetailsPage.goToSubmissionDetails()

        Log.d(STEP_TAG, "Assert that there is no submission yet for the '${pointsTextAssignment.name}' assignment. Navigate back to Assignment Details page.")
        submissionDetailsPage.assertNoSubmissionEmptyView()
        Espresso.pressBack()

        Log.d(PREPARATION_TAG,"Submit assignment: ${pointsTextAssignment.name} for student: ${student.name}.")
        submitAssignment(pointsTextAssignment, course, student)

        Log.d(STEP_TAG, "Refresh the Assignment Details Page. Assert that the assignment's status is submitted and the 'Submission and Rubric' label is displayed.")
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.assertStatusSubmitted()
        assignmentDetailsPage.assertSubmissionAndRubricLabel()

        Log.d(PREPARATION_TAG,"Make another submission for assignment: ${pointsTextAssignment.name} for student: ${student.name}.")
        val secondSubmissionAttempt = submitAssignment(pointsTextAssignment, course, student)

        Log.d(STEP_TAG, "Refresh the Assignment Details Page. Assert that the assignment's status is submitted and the 'Submission and Rubric' label is displayed.")
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.assertStatusSubmitted()
        assignmentDetailsPage.assertSubmissionAndRubricLabel()

        Log.d(STEP_TAG, "Assert that the spinner is displayed and the last/newest attempt is selected.")
        assignmentDetailsPage.assertAttemptSpinnerDisplayed()
        assignmentDetailsPage.assertSelectedAttempt(2)

        Log.d(STEP_TAG,"Navigate to submission details Comments Tab.")
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.openComments()

        Log.d(STEP_TAG,"Assert that ${secondSubmissionAttempt[0].body} text submission has been displayed as a comment.")
        submissionDetailsPage.assertTextSubmissionDisplayedAsComment()

        val newComment = "Comment for second attempt"
        Log.d(STEP_TAG,"Add a new comment ($newComment) and send it.")
        submissionDetailsPage.addAndSendComment(newComment)
        sleep(2000) // Give the comment time to propagate

        Log.d(STEP_TAG,"Assert that $newComment is displayed.")
        submissionDetailsPage.assertCommentDisplayed(newComment, student)

        Log.d(STEP_TAG, "Select 'Attempt 1' and open 'Comments' tab. Assert that '$newComment' is NOT displayed because it belongs to 'Attempt 2'.")
        submissionDetailsPage.selectAttempt("Attempt 1")
        submissionDetailsPage.assertSelectedAttempt("Attempt 1")
        submissionDetailsPage.openComments()
        submissionDetailsPage.assertCommentNotDisplayed(newComment, student)

        Log.d(STEP_TAG,"Assert that '${secondSubmissionAttempt[0].body}' text submission has been displayed as a comment.")
        submissionDetailsPage.assertTextSubmissionDisplayedAsComment()

        Log.d(STEP_TAG, "Select 'Attempt 2' and open 'Comments' tab. Assert that '$newComment' is displayed because it belongs to 'Attempt 2'.")
        submissionDetailsPage.selectAttempt("Attempt 2")
        submissionDetailsPage.assertSelectedAttempt("Attempt 2")
        submissionDetailsPage.openComments()
        submissionDetailsPage.assertCommentDisplayed(newComment, student)

        Log.d(STEP_TAG,"Assert that '${secondSubmissionAttempt[0].body}' text submission has been displayed as a comment.")
        submissionDetailsPage.assertTextSubmissionDisplayedAsComment()
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.E2E)
    fun showOnlyLetterGradeOnDashboardAndAssignmentListPageE2E() {
        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for ${course.name} course.")
        val pointsTextAssignment = createAssignment(course.id, teacher,  GradingType.POINTS, 15.0, 1.days.fromNow.iso8601)

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(PREPARATION_TAG,"Grade submission: ${pointsTextAssignment.name} with 12 points.")
        gradeSubmission(teacher, course, pointsTextAssignment.id, student, "12")

        Log.d(STEP_TAG, "Refresh the Dashboard page. Assert that the course grade is 80%.")
        dashboardPage.refresh()
        dashboardPage.assertCourseGrade(course.name, "80%")

        Log.d(PREPARATION_TAG, "Update ${course.name} course's settings: Enable restriction for quantitative data.")
        var restrictQuantitativeDataMap = mutableMapOf<String, Boolean>()
        restrictQuantitativeDataMap["restrict_quantitative_data"] = true
        CoursesApi.updateCourseSettings(course.id, restrictQuantitativeDataMap)

        Log.d(STEP_TAG, "Refresh the Dashboard page. Assert that the course grade is B-, as it is converted to letter grade because of the restriction.")
        dashboardPage.refresh()
        dashboardPage.assertCourseGrade(course.name, "B-")

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for ${course.name} course.")
        val percentageAssignment = createAssignment(course.id, teacher,  GradingType.PERCENT, 15.0, 1.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG,"Grade submission: ${percentageAssignment.name} with 66% of the maximum points (aka. 10).")
        gradeSubmission(teacher, course, percentageAssignment.id, student, "10")

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for ${course.name} course.")
        val letterGradeAssignment = createAssignment(course.id, teacher,  GradingType.LETTER_GRADE, 15.0, 1.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG,"Grade submission: ${letterGradeAssignment.name} with C.")
        gradeSubmission(teacher, course, letterGradeAssignment.id, student, "C")

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for ${course.name} course.")
        val passFailAssignment = createAssignment(course.id, teacher,  GradingType.PASS_FAIL, 15.0, 1.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG,"Grade submission: ${passFailAssignment.name} with 'Incomplete'.")
        gradeSubmission(teacher, course, passFailAssignment.id, student, "Incomplete")

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for ${course.name} course.")
        val gpaScaleAssignment = createAssignment(course.id, teacher,  GradingType.GPA_SCALE, 15.0, 1.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG,"Grade submission: ${gpaScaleAssignment.name} with 3.7.")
        gradeSubmission(teacher, course, gpaScaleAssignment.id, student, "3.7")

        Log.d(STEP_TAG, "Refresh the Dashboard page to let the newly added submissions and their grades propagate.")
        dashboardPage.refresh()

        Log.d(STEP_TAG,"Select course: ${course.name}.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG,"Navigate to course Assignments Page.")
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG, "Assert that all the different types of assignments' grades has been converted properly.")
        assignmentListPage.assertAssignmentDisplayedWithGrade(pointsTextAssignment.name, "B-")
        assignmentListPage.assertAssignmentDisplayedWithGrade(percentageAssignment.name, "D")
        assignmentListPage.assertAssignmentDisplayedWithGrade(letterGradeAssignment.name, "C")
        assignmentListPage.assertAssignmentDisplayedWithGrade(passFailAssignment.name, "Incomplete")
        assignmentListPage.assertAssignmentDisplayedWithGrade(gpaScaleAssignment.name, "F")

        Log.d(STEP_TAG, "Click on '${pointsTextAssignment.name}' assignment and assert that the corresponding letter grade is displayed on it's details page. Navigate back to Assignment List Page.")
        assignmentListPage.clickAssignment(pointsTextAssignment)
        assignmentDetailsPage.assertGradeDisplayed("B-")
        assignmentDetailsPage.assertScoreNotDisplayed()
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on '${percentageAssignment.name}' assignment and assert that the corresponding letter grade is displayed on it's details page. Navigate back to Assignment List Page.")
        assignmentListPage.clickAssignment(percentageAssignment)
        assignmentDetailsPage.assertGradeDisplayed("D")
        assignmentDetailsPage.assertScoreNotDisplayed()
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on '${letterGradeAssignment.name}' assignment and assert that the corresponding letter grade is displayed on it's details page. Navigate back to Assignment List Page.")
        assignmentListPage.clickAssignment(letterGradeAssignment)
        assignmentDetailsPage.assertGradeDisplayed("C")
        assignmentDetailsPage.assertScoreNotDisplayed()
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on '${passFailAssignment.name}' assignment and assert that the corresponding letter grade is displayed on it's details page. Navigate back to Assignment List Page.")
        assignmentListPage.clickAssignment(passFailAssignment)
        assignmentDetailsPage.assertGradeDisplayed("Incomplete")
        assignmentDetailsPage.assertScoreNotDisplayed()
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on '${gpaScaleAssignment.name}' assignment and assert that the corresponding letter grade is displayed on it's details page. Navigate back to Assignment List Page.")
        assignmentListPage.clickAssignment(gpaScaleAssignment)
        assignmentDetailsPage.assertGradeDisplayed("F")
        assignmentDetailsPage.assertScoreNotDisplayed()
        Espresso.pressBack()

        Log.d(PREPARATION_TAG, "Update ${course.name} course's settings: Disable restriction for quantitative data.")
        restrictQuantitativeDataMap["restrict_quantitative_data"] = false
        CoursesApi.updateCourseSettings(course.id, restrictQuantitativeDataMap)

        Log.d(STEP_TAG, "Refresh the Assignment List Page. Assert that all the different types of assignments' grades" +
                "has been shown as their original grade types, since the restriction has been turned off.")
        assignmentListPage.refresh()
        assignmentListPage.assertAssignmentDisplayedWithGrade(pointsTextAssignment.name, "12/15")
        assignmentListPage.assertAssignmentDisplayedWithGrade(percentageAssignment.name, "66.67%")
        assignmentListPage.assertAssignmentDisplayedWithGrade(letterGradeAssignment.name, "11.4/15 (C)")
        assignmentListPage.assertAssignmentDisplayedWithGrade(passFailAssignment.name, "Incomplete")
        assignmentListPage.assertAssignmentDisplayedWithGrade(gpaScaleAssignment.name, "3.7/15 (F)")

        Log.d(STEP_TAG, "Click on '${pointsTextAssignment.name}' assignment and assert that the corresponding grade and score is displayed on it's details page. Navigate back to Assignment List Page.")
        assignmentListPage.clickAssignment(pointsTextAssignment)
        assignmentDetailsPage.assertScoreDisplayed("12")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on '${percentageAssignment.name}' assignment and assert that the corresponding grade and score is displayed on it's details page. Navigate back to Assignment List Page.")
        assignmentListPage.clickAssignment(percentageAssignment)
        assignmentDetailsPage.assertScoreDisplayed("10")
        assignmentDetailsPage.assertGradeDisplayed("66.67%")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on '${letterGradeAssignment.name}' assignment and assert that the corresponding grade and score is displayed on it's details page. Navigate back to Assignment List Page.")
        assignmentListPage.clickAssignment(letterGradeAssignment)
        assignmentDetailsPage.assertScoreDisplayed("11.4")
        assignmentDetailsPage.assertGradeDisplayed("C")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on '${passFailAssignment.name}' assignment and assert that the corresponding grade is displayed on it's details page, and no score displayed since it's a pass/fail assignment. Navigate back to Assignment List Page.")
        assignmentListPage.clickAssignment(passFailAssignment)
        assignmentDetailsPage.assertGradeDisplayed("Incomplete")
        assignmentDetailsPage.assertScoreNotDisplayed()
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on '${gpaScaleAssignment.name}' assignment and assert that the corresponding grade and score is displayed on it's details page. Navigate back to Assignment List Page.")
        assignmentListPage.clickAssignment(gpaScaleAssignment)
        assignmentDetailsPage.assertScoreDisplayed("3.7")
        assignmentDetailsPage.assertGradeDisplayed("F")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG, "Assert that the course grade is F, as it is converted to letter grade because the disability of the restriction has not propagated yet.")
        dashboardPage.assertCourseGrade(course.name, "F")

        Log.d(STEP_TAG, "Refresh the Dashboard page (to allow the disabled restriction to propagate). Assert that the course grade is 49.47%, since we can now show percentage and numeric data.")
        dashboardPage.refresh()
        dashboardPage.assertCourseGrade(course.name, "49.47%")
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GRADES, TestCategory.E2E)
    fun showOnlyLetterGradeOnGradesPageE2E() {
        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for ${course.name} course.")
        val pointsTextAssignment = createAssignment(course.id, teacher, GradingType.POINTS, 15.0, 1.days.fromNow.iso8601)

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(PREPARATION_TAG, "Grade submission: ${pointsTextAssignment.name} with 12 points.")
        gradeSubmission(teacher, course, pointsTextAssignment.id, student, "12")

        Log.d(PREPARATION_TAG, "Update ${course.name} course's settings: Enable restriction for quantitative data.")
        var restrictQuantitativeDataMap = mutableMapOf<String, Boolean>()
        restrictQuantitativeDataMap["restrict_quantitative_data"] = true
        CoursesApi.updateCourseSettings(course.id, restrictQuantitativeDataMap)

        Log.d(STEP_TAG, "Refresh the Dashboard page. Assert that the course grade is B-, as it is converted to letter grade because of the restriction.")
        dashboardPage.refresh()
        dashboardPage.assertCourseGrade(course.name, "B-")

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for ${course.name} course.")
        val percentageAssignment = createAssignment(course.id, teacher, GradingType.PERCENT, 15.0, 1.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG, "Grade submission: ${percentageAssignment.name} with 66% of the maximum points (aka. 10).")
        gradeSubmission(teacher, course, percentageAssignment.id, student, "10")

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for ${course.name} course.")
        val letterGradeAssignment = createAssignment(
            course.id,
            teacher,
            GradingType.LETTER_GRADE,
            15.0,
            1.days.fromNow.iso8601
        )

        Log.d(PREPARATION_TAG, "Grade submission: ${letterGradeAssignment.name} with C.")
        gradeSubmission(teacher, course, letterGradeAssignment.id, student, "C")

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for ${course.name} course.")
        val passFailAssignment = createAssignment(
            course.id,
            teacher,
            GradingType.PASS_FAIL,
            15.0,
            1.days.fromNow.iso8601
        )

        Log.d(PREPARATION_TAG, "Grade submission: ${passFailAssignment.name} with 'Incomplete'.")
        gradeSubmission(teacher, course, passFailAssignment.id, student, "Incomplete")

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for ${course.name} course.")
        val gpaScaleAssignment = createAssignment(
            course.id,
            teacher,
            GradingType.GPA_SCALE,
            15.0,
            1.days.fromNow.iso8601
        )

        Log.d(PREPARATION_TAG, "Grade submission: ${gpaScaleAssignment.name} with 3.7.")
        gradeSubmission(teacher, course, gpaScaleAssignment.id, student, "3.7")

        Log.d(STEP_TAG, "Refresh the Dashboard page to let the newly added submissions and their grades propagate.")
        dashboardPage.refresh()

        Log.d(STEP_TAG, "Select course: ${course.name}. Select 'Grades' menu.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectGrades()

        Log.d(STEP_TAG, "Assert that the Total Grade is F and all of the assignment grades are displayed properly (so they have been converted to letter grade).")
        courseGradesPage.assertTotalGrade(ViewMatchers.withText("F"))
        courseGradesPage.assertAssignmentDisplayed(pointsTextAssignment.name, "B-")
        courseGradesPage.assertAssignmentDisplayed(percentageAssignment.name, "D")
        courseGradesPage.assertAssignmentDisplayed(letterGradeAssignment.name, "C")
        courseGradesPage.assertAssignmentDisplayed(passFailAssignment.name, "Incomplete")
        courseGradesPage.assertAssignmentDisplayed(gpaScaleAssignment.name, "F")

        Log.d(PREPARATION_TAG, "Update ${course.name} course's settings: Enable restriction for quantitative data.")
        restrictQuantitativeDataMap["restrict_quantitative_data"] = false
        CoursesApi.updateCourseSettings(course.id, restrictQuantitativeDataMap)

        Log.d(STEP_TAG, "Refresh the Course Grade Page.")
        courseGradesPage.refresh()
        courseGradesPage.refresh() //Need another refresh, aka. swipeDown because somehow first is going to the bottom and only swipes to the top, but the actual refresh does not happen.

        Log.d(STEP_TAG, "Assert that the Total Grade is 49.47% and all of the assignment grades are displayed properly. We now show numeric grades because restriction to quantitative data has been disabled.")
        courseGradesPage.assertTotalGrade(ViewMatchers.withText("49.47%"))
        courseGradesPage.assertAssignmentDisplayed(pointsTextAssignment.name, "12/15")
        courseGradesPage.assertAssignmentDisplayed(percentageAssignment.name, "66.67%")
        courseGradesPage.assertAssignmentDisplayed(letterGradeAssignment.name, "11.4/15 (C)")
        courseGradesPage.assertAssignmentDisplayed(passFailAssignment.name, "Incomplete")
        courseGradesPage.assertAssignmentDisplayed(gpaScaleAssignment.name, "3.7/15 (F)")
    }

        private fun createAssignment(
        courseId: Long,
        teacher: CanvasUserApiModel,
        gradingType: GradingType,
        pointsPossible: Double,
        dueAt: String = EMPTY_STRING,
        allowedExtensions: List<String>? = null,
        assignmentGroupId: Long? = null,
        submissionType: List<SubmissionType> = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
    ): AssignmentApiModel {
        return AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
                courseId = courseId,
                submissionTypes = submissionType,
                gradingType = gradingType,
                teacherToken = teacher.token,
                pointsPossible = pointsPossible,
                dueAt = dueAt,
                allowedExtensions = allowedExtensions,
                assignmentGroupId = assignmentGroupId
            )
        )
    }

    private fun submitAssignment(
        assignment: AssignmentApiModel,
        course: CourseApiModel,
        student: CanvasUserApiModel
    ): List<SubmissionApiModel> {
        return SubmissionsApi.seedAssignmentSubmission(
            SubmissionsApi.SubmissionSeedRequest(
                assignmentId = assignment.id,
                courseId = course.id,
                studentToken = student.token,
                submissionSeedsList = listOf(
                    SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                    )
                )
            )
        )
    }

    private fun gradeSubmission(
        teacher: CanvasUserApiModel,
        course: CourseApiModel,
        assignment: AssignmentApiModel,
        student: CanvasUserApiModel,
        postedGrade: String,
        excused: Boolean = false
    ) {
        SubmissionsApi.gradeSubmission(
            teacherToken = teacher.token,
            courseId = course.id,
            assignmentId = assignment.id,
            studentId = student.id,
            postedGrade = postedGrade,
            excused = excused
        )
    }

    private fun gradeSubmission(
        teacher: CanvasUserApiModel,
        course: CourseApiModel,
        assignmentId: Long,
        student: CanvasUserApiModel,
        postedGrade: String
    ) = SubmissionsApi.gradeSubmission(
        teacherToken = teacher.token,
        courseId = course.id,
        assignmentId = assignmentId,
        studentId = student.id,
        postedGrade = postedGrade,
        excused = false
    )

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
}