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
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.checkToastText
import com.instructure.dataseeding.api.AssignmentGroupsApi
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.FileUploadType
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.pandautils.utils.toFormattedString
import com.instructure.student.R
import com.instructure.student.ui.pages.AssignmentListPage
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.ViewUtils
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import com.instructure.student.ui.utils.uploadTextFile
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import java.util.Calendar

@HiltAndroidTest
class AssignmentsE2ETest: StudentComposeTest() {

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
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.E2E, SecondaryFeatureCategory.ASSIGNMENT_REMINDER)
    fun testAssignmentCustomReminderE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val futureDueDate = 2.days.fromNow
        val pastDueDate = 2.days.ago

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for '${course.name}' course with 2 days ahead due date.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, dueAt = futureDueDate.iso8601, pointsPossible = 15.0, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for '${course.name}' course with 2 days past due date.")
        val alreadyPastAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, dueAt = pastDueDate.iso8601, pointsPossible = 15.0, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Select course: '${course.name}'.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG,"Navigate to course Assignments Page.")
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG,"Click on assignment '${testAssignment.name}'.")
        assignmentListPage.clickAssignment(testAssignment)

        Log.d(STEP_TAG, "Assert that the corresponding views are displayed on the Assignment Details Page. Assert that the reminder section is displayed as well.")
        assignmentDetailsPage.assertPageObjects()
        reminderPage.assertReminderSectionDisplayed()

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        Log.d(STEP_TAG, "Select '1 Hour Before' and assert that the reminder has been picked up and displayed on the Assignment Details Page.")
        val reminderDateOneHour = futureDueDate.apply { add(Calendar.HOUR, -1) }
        reminderPage.clickCustomReminderOption()
        reminderPage.selectDate(reminderDateOneHour)
        reminderPage.selectTime(reminderDateOneHour)
        reminderPage.assertReminderDisplayedWithText(reminderDateOneHour.time.toFormattedString())

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        Log.d(STEP_TAG, "Select '1 Hour Before' again, and assert that a toast message is occurring which warns that we cannot pick up the same time reminder twice.")
        reminderPage.clickCustomReminderOption()
        reminderPage.selectDate(reminderDateOneHour)
        reminderPage.selectTime(reminderDateOneHour)
        checkToastText(R.string.reminderAlreadySet, activityRule.activity)

        Log.d(STEP_TAG, "Remove the '1 Hour Before' reminder, confirm the deletion dialog and assert that the '1 Hour Before' reminder is not displayed any more.")
        reminderPage.removeReminderWithText(reminderDateOneHour.time.toFormattedString())
        reminderPage.assertReminderNotDisplayedWithText(reminderDateOneHour.time.toFormattedString())
        futureDueDate.apply { add(Calendar.HOUR, 1) }

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        Log.d(STEP_TAG, "Select '1 Week Before' and assert that a toast message is occurring which warns that we cannot pick up a reminder which has already passed (for example cannot pick '1 Week Before' reminder for an assignment which ends tomorrow).")
        val reminderDateOneWeek = futureDueDate.apply { add(Calendar.WEEK_OF_YEAR, -1) }
        reminderPage.clickCustomReminderOption()
        reminderPage.selectDate(reminderDateOneWeek)
        reminderPage.selectTime(reminderDateOneWeek)
        reminderPage.assertReminderNotDisplayedWithText(reminderDateOneWeek.time.toFormattedString())
        checkToastText(R.string.reminderInPast, activityRule.activity)
        futureDueDate.apply { add(Calendar.WEEK_OF_YEAR, 1) }

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        Log.d(STEP_TAG, "Select '1 Day Before' and assert that the reminder has been picked up and displayed on the Assignment Details Page.")
        val reminderDateOneDay = futureDueDate.apply { add(Calendar.DAY_OF_MONTH, -1) }
        reminderPage.clickCustomReminderOption()
        reminderPage.selectDate(reminderDateOneDay)
        reminderPage.selectTime(reminderDateOneDay)
        reminderPage.assertReminderDisplayedWithText(reminderDateOneDay.time.toFormattedString())

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        reminderPage.clickCustomReminderOption()
        reminderPage.selectDate(reminderDateOneDay)
        reminderPage.selectTime(reminderDateOneDay)

        Log.d(STEP_TAG, "Assert that a toast message is occurring which warns that we cannot pick up the same time reminder twice. (Because 1 days and 24 hours is the same)")
        checkToastText(R.string.reminderAlreadySet, activityRule.activity)

        futureDueDate.apply { add(Calendar.DAY_OF_MONTH, 1) }

        Log.d(STEP_TAG, "Navigate back to Assignment List Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Click on assignment '${alreadyPastAssignment.name}'.")
        assignmentListPage.clickAssignment(alreadyPastAssignment)

        Log.d(STEP_TAG, "Assert that the reminder section is NOT displayed, because the '${alreadyPastAssignment.name}' assignment has already passed..")
        reminderPage.assertReminderSectionDisplayed()
    }

    @E2E
    @Test
    @Stub
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.E2E, SecondaryFeatureCategory.ASSIGNMENT_REMINDER)
    fun testAssignmentBeforeReminderE2E() {
        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val futureDueDate = 2.days.fromNow
        val pastDueDate = 2.days.ago

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for '${course.name}' course with 2 days ahead due date.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, dueAt = futureDueDate.iso8601, pointsPossible = 15.0, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for '${course.name}' course with 2 days past due date.")
        val alreadyPastAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, dueAt = pastDueDate.iso8601, pointsPossible = 15.0, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Select course: '${course.name}'.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG,"Navigate to course Assignments Page.")
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG,"Click on assignment '${testAssignment.name}'.")
        assignmentListPage.clickAssignment(testAssignment)

        Log.d(STEP_TAG, "Assert that the corresponding views are displayed on the Assignment Details Page. Assert that the reminder section is displayed as well.")
        assignmentDetailsPage.assertPageObjects()
        reminderPage.assertReminderSectionDisplayed()

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        Log.d(STEP_TAG, "Select '1 Hour Before' and assert that the reminder has been picked up and displayed on the Assignment Details Page.")
        val reminderDateOneHour = futureDueDate.apply { add(Calendar.HOUR, -1) }
        reminderPage.clickBeforeReminderOption("1 Hour Before")
        reminderPage.assertReminderDisplayedWithText(reminderDateOneHour.time.toFormattedString())

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        Log.d(STEP_TAG, "Select '1 Hour Before' again, and assert that a toast message is occurring which warns that we cannot pick up the same time reminder twice.")
        reminderPage.clickBeforeReminderOption("1 Hour Before")
        checkToastText(R.string.reminderAlreadySet, activityRule.activity)

        Log.d(STEP_TAG, "Remove the '1 Hour Before' reminder, confirm the deletion dialog and assert that the '1 Hour Before' reminder is not displayed any more.")
        reminderPage.removeReminderWithText(reminderDateOneHour.time.toFormattedString())
        reminderPage.assertReminderNotDisplayedWithText(reminderDateOneHour.time.toFormattedString())
        futureDueDate.apply { add(Calendar.HOUR, 1) }

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        Log.d(STEP_TAG, "Select '1 Week Before' and assert that a toast message is occurring which warns that we cannot pick up a reminder which has already passed (for example cannot pick '1 Week Before' reminder for an assignment which ends tomorrow).")
        val reminderDateOneWeek = futureDueDate.apply { add(Calendar.WEEK_OF_YEAR, -1) }
        reminderPage.clickBeforeReminderOption("1 Week Before")
        reminderPage.assertReminderNotDisplayedWithText(reminderDateOneWeek.time.toFormattedString())
        composeTestRule.waitForIdle()
        checkToastText(R.string.reminderInPast, activityRule.activity)
        composeTestRule.waitForIdle()
        futureDueDate.apply { add(Calendar.WEEK_OF_YEAR, 1) }

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        Log.d(STEP_TAG, "Select '1 Day Before' and assert that the reminder has been picked up and displayed on the Assignment Details Page.")
        val reminderDateOneDay = futureDueDate.apply { add(Calendar.DAY_OF_MONTH, -1) }
        reminderPage.clickBeforeReminderOption("1 Day Before")
        reminderPage.assertReminderDisplayedWithText(reminderDateOneDay.time.toFormattedString())

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        reminderPage.clickBeforeReminderOption("1 Day Before")

        Log.d(STEP_TAG, "Assert that a toast message is occurring which warns that we cannot pick up the same time reminder twice. (Because 1 days and 24 hours is the same)")
        composeTestRule.waitForIdle()
        checkToastText(R.string.reminderAlreadySet, activityRule.activity)
        composeTestRule.waitForIdle()

        futureDueDate.apply { add(Calendar.DAY_OF_MONTH, 1) }

        Log.d(STEP_TAG, "Navigate back to Assignment List Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Click on assignment '${alreadyPastAssignment.name}'.")
        assignmentListPage.clickAssignment(alreadyPastAssignment)

        Log.d(STEP_TAG, "Assert that the reminder section is NOT displayed, because the '${alreadyPastAssignment.name}' assignment has already passed..")
        reminderPage.assertReminderSectionDisplayed()
    }

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
        val pointsTextAssignment = AssignmentsApi.createAssignment(
            courseId = course.id,
            teacherToken = teacher.token,
            gradingType = GradingType.POINTS,
            pointsPossible = 15.0,
            dueAt = 1.days.fromNow.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

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
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, pointsTextAssignment.id, submissionSeedsList = listOf(SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        assignmentDetailsPage.refresh()
        assignmentDetailsPage.assertStatusSubmitted()
        assignmentDetailsPage.assertSubmissionAndRubricLabel()

        Log.d(PREPARATION_TAG,"Grade submission: ${pointsTextAssignment.name} with 13 points.")
        val textGrade = SubmissionsApi.gradeSubmission(teacher.token, course.id, pointsTextAssignment.id, student.id, postedGrade = "13")

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
        val data = seedData(teachers = 1, courses = 1, students = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for ${course.name} course.")
        val letterGradeTextAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.LETTER_GRADE, pointsPossible = 20.0, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Submit assignment: ${letterGradeTextAssignment.name} for student: ${student.name}.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, letterGradeTextAssignment.id, submissionSeedsList = listOf(SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(PREPARATION_TAG,"Grade submission: ${letterGradeTextAssignment.name} with 13 points.")
        val submissionGrade = SubmissionsApi.gradeSubmission(teacher.token, course.id, letterGradeTextAssignment.id, student.id, postedGrade = "13")

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
        val data = seedData(teachers = 1, courses = 1, students = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding assignment for ${course.name} course.")
        val percentageFileAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.PERCENT, pointsPossible = 25.0, allowedExtensions = listOf("txt", "pdf", "jpg"), submissionTypes = listOf(SubmissionType.ONLINE_UPLOAD))

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
        val uploadInfo = uploadTextFile(
            courseId = course.id,
            assignmentId = percentageFileAssignment.id,
            token = student.token,
            fileUploadType = FileUploadType.ASSIGNMENT_SUBMISSION
        )

        Log.d(PREPARATION_TAG,"Submit ${percentageFileAssignment.name} assignment for ${student.name} student.")
        SubmissionsApi.submitCourseAssignment(course.id, student.token, percentageFileAssignment.id, SubmissionType.ONLINE_UPLOAD, fileIds = mutableListOf(uploadInfo.id))

        Log.d(STEP_TAG,"Refresh the page. Assert that the ${percentageFileAssignment.name} assignment has been submitted.")
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.assertAssignmentSubmitted()

        Log.d(PREPARATION_TAG,"Grade ${percentageFileAssignment.name} assignment with 22 percentage.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, percentageFileAssignment.id, student.id, postedGrade = "22")

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

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.E2E)
    fun testMultipleAssignmentsE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(teachers = 1, courses = 1, students = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course.")
        val letterGradeTextAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.LETTER_GRADE, pointsPossible = 20.0, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Submit '${letterGradeTextAssignment.name}' assignment for '${student.name}' student.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, letterGradeTextAssignment.id, submissionSeedsList = listOf(SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(PREPARATION_TAG,"Grade '${letterGradeTextAssignment.name}' assignment with 16.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, letterGradeTextAssignment.id, student.id, postedGrade = "16")

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course.")
        val pointsTextAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Submit '${pointsTextAssignment.name}' assignment for '${student.name}' student.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, pointsTextAssignment.id, submissionSeedsList = listOf(SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(PREPARATION_TAG,"Grade '${pointsTextAssignment.name}' assignment with 13 points.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, pointsTextAssignment.id, student.id, postedGrade = "13")

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Select '${course.name}' course and navigate to it's Assignments Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG,"Assert that '${pointsTextAssignment.name}' assignment is displayed with the corresponding grade: 13.")
        assignmentListPage.assertHasAssignment(pointsTextAssignment,"13")

        Log.d(STEP_TAG,"Assert that '${letterGradeTextAssignment.name}' assignment is displayed with the corresponding grade: 16.")
        assignmentListPage.assertHasAssignment(letterGradeTextAssignment, "16")
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.E2E)
    fun testFilterAndSortAssignmentsE2E() {
        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(teachers = 1, courses = 1, students = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course.")
        val upcomingAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.LETTER_GRADE, pointsPossible = 20.0, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course.")
        val missingAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.LETTER_GRADE, pointsPossible = 20.0, dueAt = 2.days.ago.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Seeding a GRADED assignment for ${course.name} course.")
        val gradedAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.LETTER_GRADE, pointsPossible = 20.0, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Grade the '${gradedAssignment.name}' with '11' points out of 20.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, gradedAssignment.id, student.id, postedGrade = "11")

        Log.d(PREPARATION_TAG,"Create an Assignment Group for '${course.name}' course.")
        val assignmentGroup = AssignmentGroupsApi.createAssignmentGroup(teacher.token, course.id, name = "Discussions")

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course.")
        val otherTypeAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.LETTER_GRADE, pointsPossible = 20.0, assignmentGroupId = assignmentGroup.id, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

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

        Log.d(STEP_TAG, "Filter the 'MISSING' assignments.")
        assignmentListPage.filterAssignments(AssignmentListPage.AssignmentType.MISSING)

        Log.d(STEP_TAG, "Assert that the '${missingAssignment.name}' MISSING assignment is displayed and the others at NOT.")
        assignmentListPage.assertHasAssignment(missingAssignment)
        assignmentListPage.assertAssignmentNotDisplayed(upcomingAssignment.name)
        assignmentListPage.assertAssignmentNotDisplayed(otherTypeAssignment.name)
        assignmentListPage.assertAssignmentNotDisplayed(gradedAssignment.name)

        Log.d(STEP_TAG, "Filter the 'GRADED' assignments.")
        assignmentListPage.filterAssignments(AssignmentListPage.AssignmentType.GRADED)

        Log.d(STEP_TAG, "Assert that the '${gradedAssignment.name}' GRADED assignment is displayed.")
        assignmentListPage.assertHasAssignment(gradedAssignment)
        assignmentListPage.assertAssignmentNotDisplayed(upcomingAssignment.name)
        assignmentListPage.assertAssignmentNotDisplayed(otherTypeAssignment.name)
        assignmentListPage.assertAssignmentNotDisplayed(missingAssignment.name)

        Log.d(STEP_TAG, "Set back the filter to show 'ALL' the assignments like by default.")
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

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.COMMENTS, TestCategory.E2E)
    fun testMediaCommentsE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(teachers = 1, courses = 1, students = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course.")
        val assignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Submit '${assignment.name}' assignment for '${student.name}' student.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, assignment.id, submissionSeedsList = listOf(SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Select '${course.name}' course and navigate to it's Assignments Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG,"Click on '${assignment.name}' assignment.")
        assignmentListPage.clickAssignment(assignment)

        Log.d(STEP_TAG,"Navigate to submission details Comments Tab.")
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.openComments()

        submissionDetailsPage.addAndSendVideoComment()
        sleep(5000) // wait for video comment submission to propagate
        submissionDetailsPage.assertVideoCommentDisplayed()

        Log.d(STEP_TAG,"Send an audio comment.")
        submissionDetailsPage.addAndSendAudioComment()
        sleep(5000) // Wait for audio comment submission to propagate

        Log.d(STEP_TAG,"Assert that the audio comment has been displayed.")
        submissionDetailsPage.assertAudioCommentDisplayed()
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.COMMENTS, TestCategory.E2E)
    fun testAddFileCommentE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(teachers = 1, courses = 1, students = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course.")
        val assignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Submit '${assignment.name}' assignment for '${student.name}' student.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, assignment.id, submissionSeedsList = listOf(SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Seed a comment attachment upload.")
        val commentUploadInfo = uploadTextFile(
            courseId = course.id,
            assignmentId = assignment.id,
            token = student.token,
            fileUploadType = FileUploadType.COMMENT_ATTACHMENT
        )
        SubmissionsApi.commentOnSubmission(course.id, student.token, assignment.id, mutableListOf(commentUploadInfo.id))

        Log.d(STEP_TAG,"Select '${course.name}' course and navigate to it's Assignments Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG,"Click on '${assignment.name}' assignment.")
        assignmentListPage.clickAssignment(assignment)

        Log.d(STEP_TAG,"Assert that '${commentUploadInfo.fileName}' file is displayed as a comment by '${student.name}' student.")
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
        val data = seedData(teachers = 1, courses = 1, students = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for '${course.name}' course.")
        val pointsTextAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Select course: '${course.name}'.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG, "Navigate to course Assignments Page.")
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG, "Verify that our assignments are present," +
                "along with any grade/date info. Click on assignment '${pointsTextAssignment.name}'.")
        assignmentListPage.assertHasAssignment(pointsTextAssignment)
        assignmentListPage.clickAssignment(pointsTextAssignment)

        Log.d(PREPARATION_TAG,"Submit assignment: '${pointsTextAssignment.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, pointsTextAssignment.id, submissionSeedsList = listOf(SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(STEP_TAG, "Refresh the page.")
        assignmentDetailsPage.refresh()

        Log.d(STEP_TAG, "Assert that when only there is one attempt, the spinner is not displayed.")
        assignmentDetailsPage.assertNoAttemptSpinner()

        Log.d(PREPARATION_TAG,"Generate another submission for assignment: '${pointsTextAssignment.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, pointsTextAssignment.id, submissionSeedsList = listOf(SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(STEP_TAG, "Refresh the page.")
        assignmentDetailsPage.refresh()

        Log.d(STEP_TAG, "Assert that the spinner is displayed and the last/newest attempt ('Attempt 2') is selected.")
        assignmentDetailsPage.assertAttemptSpinnerDisplayed()
        assignmentDetailsPage.assertAttemptInformation()

        Log.d(STEP_TAG, "Go to the Submission Details Page and assert that the selected attempt is 'Attempt 2'.")
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.assertSelectedAttempt("Attempt 2")

        Log.d(STEP_TAG, "Navigate back to the Assignment Details Page. Select the other attempt, 'Attempt 1', and assert if it's displayed as the selected one.")
        Espresso.pressBack()
        assignmentDetailsPage.selectAttempt(1)
        assignmentDetailsPage.assertAttemptInformation()
        assignmentDetailsPage.assertSelectedAttempt(1)

        Log.d(STEP_TAG, "Go to the Submission Details Page and assert that the selected attempt is 'Attempt 1'.")
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.assertSelectedAttempt("Attempt 1")
    }

    @Stub
    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SUBMISSIONS, TestCategory.E2E)
    fun testCommentsBelongToSubmissionAttempts() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(teachers = 1, courses = 1, students = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for '${course.name}' course.")
        val pointsTextAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Select course: '${course.name}'.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG,"Navigate to course Assignments Page.")
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG,"Verify that our assignments are present, along with any grade/date info. Click on assignment '${pointsTextAssignment.name}'.")
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

        Log.d(PREPARATION_TAG,"Submit assignment: '${pointsTextAssignment.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, pointsTextAssignment.id, submissionSeedsList = listOf(SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(STEP_TAG, "Refresh the Assignment Details Page. Assert that the assignment's status is submitted and the 'Submission and Rubric' label is displayed.")
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.assertStatusSubmitted()
        assignmentDetailsPage.assertSubmissionAndRubricLabel()

        Log.d(PREPARATION_TAG,"Make another submission for assignment: '${pointsTextAssignment.name}' for student: '${student.name}'.")
        val secondSubmissionAttempt = SubmissionsApi.seedAssignmentSubmission(course.id, student.token, pointsTextAssignment.id, submissionSeedsList = listOf(SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(STEP_TAG, "Refresh the Assignment Details Page. Assert that the assignment's status is submitted and the 'Submission and Rubric' label is displayed.")
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.assertStatusSubmitted()
        assignmentDetailsPage.assertSubmissionAndRubricLabel()

        Log.d(STEP_TAG, "Assert that the spinner is displayed and the last/newest attempt is selected.")
        assignmentDetailsPage.assertAttemptSpinnerDisplayed()
        assignmentDetailsPage.assertAttemptInformation()
        assignmentDetailsPage.assertSelectedAttempt(2)

        Log.d(STEP_TAG,"Navigate to submission details Comments Tab.")
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.openComments()

        Log.d(STEP_TAG,"Assert that '${secondSubmissionAttempt[0].body}' text submission has been displayed as a comment.")
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
        val data = seedData(teachers = 1, courses = 1, students = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for ${course.name} course.")
        val pointsTextAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt =  1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(PREPARATION_TAG,"Grade submission: ${pointsTextAssignment.name} with 12 points.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, pointsTextAssignment.id, student.id, postedGrade =  "12")

        Log.d(STEP_TAG, "Refresh the Dashboard page. Assert that the course grade is 80%.")
        dashboardPage.refresh()
        dashboardPage.assertCourseGrade(course.name, "80%")

        Log.d(PREPARATION_TAG, "Update ${course.name} course's settings: Enable restriction for quantitative data.")
        val restrictQuantitativeDataMap = mutableMapOf<String, Boolean>()
        restrictQuantitativeDataMap["restrict_quantitative_data"] = true
        CoursesApi.updateCourseSettings(course.id, restrictQuantitativeDataMap)

        Log.d(STEP_TAG, "Refresh the Dashboard page. Assert that the course grade is B-, as it is converted to letter grade because of the restriction.")
        retryWithIncreasingDelay(times = 10, maxDelay = 4000) {
            dashboardPage.refresh()
            dashboardPage.assertCourseGrade(course.name, "B-")
        }

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for ${course.name} course.")
        val percentageAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.PERCENT, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Grade submission: ${percentageAssignment.name} with 66% of the maximum points (aka. 10).")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, percentageAssignment.id, student.id, postedGrade = "10")

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for ${course.name} course.")
        val letterGradeAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.LETTER_GRADE, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Grade submission: ${letterGradeAssignment.name} with C.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, letterGradeAssignment.id, student.id, postedGrade = "C")

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for ${course.name} course.")
        val passFailAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.PASS_FAIL, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Grade submission: ${passFailAssignment.name} with 'Incomplete'.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, passFailAssignment.id, student.id, postedGrade = "Incomplete")

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for ${course.name} course.")
        val gpaScaleAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.GPA_SCALE, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Grade submission: ${gpaScaleAssignment.name} with 3.7.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, gpaScaleAssignment.id, student.id, postedGrade = "3.7")

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
        val data = seedData(teachers = 1, courses = 1, students = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for '${course.name}' course.")
        val pointsTextAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible =  15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(PREPARATION_TAG, "Grade submission: '${pointsTextAssignment.name}' with 12 points.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, pointsTextAssignment.id, student.id, postedGrade = "12")

        Log.d(PREPARATION_TAG, "Update '${course.name}' course's settings: Enable restriction for quantitative data.")
        val restrictQuantitativeDataMap = mutableMapOf<String, Boolean>()
        restrictQuantitativeDataMap["restrict_quantitative_data"] = true
        CoursesApi.updateCourseSettings(course.id, restrictQuantitativeDataMap)

        Log.d(STEP_TAG, "Refresh the Dashboard page. Assert that the course grade is B-, as it is converted to letter grade because of the restriction.")
        retryWithIncreasingDelay(times = 10, maxDelay = 4000) {
            dashboardPage.refresh()
            dashboardPage.assertCourseGrade(course.name, "B-")
        }

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for '${course.name}' course.")
        val percentageAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.PERCENT, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG, "Grade submission: '${percentageAssignment.name}' with 66% of the maximum points (aka. 10).")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, percentageAssignment.id, student.id, postedGrade = "10")

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for '${course.name}' course.")
        val letterGradeAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.LETTER_GRADE, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG, "Grade submission: '${letterGradeAssignment.name}' with C.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, letterGradeAssignment.id, student.id, postedGrade = "C")

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for '${course.name}' course.")
        val passFailAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.PASS_FAIL, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG, "Grade submission: '${passFailAssignment.name}' with 'Incomplete'.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, passFailAssignment.id, student.id, postedGrade = "Incomplete")

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for '${course.name}' course.")
        val gpaScaleAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.GPA_SCALE, pointsPossible = 15.0, dueAt =  1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG, "Grade submission: '${gpaScaleAssignment.name}' with 3.7.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, gpaScaleAssignment.id, student.id, postedGrade = "3.7")

        Log.d(STEP_TAG, "Refresh the Dashboard page to let the newly added submissions and their grades propagate.")
        dashboardPage.refresh()

        Log.d(STEP_TAG, "Select course: '${course.name}'. Select 'Grades' menu.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectGrades()

        Log.d(STEP_TAG, "Assert that the Total Grade is F and all of the assignment grades are displayed properly (so they have been converted to letter grade).")
        courseGradesPage.assertTotalGrade(ViewMatchers.withText("F"))
        courseGradesPage.assertAssignmentDisplayed(pointsTextAssignment.name, "B-")
        courseGradesPage.assertAssignmentDisplayed(percentageAssignment.name, "D")
        courseGradesPage.assertAssignmentDisplayed(letterGradeAssignment.name, "C")
        courseGradesPage.assertAssignmentDisplayed(passFailAssignment.name, "Incomplete")
        if(isLowResDevice()) courseGradesPage.swipeUp()
        courseGradesPage.assertAssignmentDisplayed(gpaScaleAssignment.name, "F")

        Log.d(PREPARATION_TAG, "Update '${course.name}' course's settings: Enable restriction for quantitative data.")
        restrictQuantitativeDataMap["restrict_quantitative_data"] = false
        CoursesApi.updateCourseSettings(course.id, restrictQuantitativeDataMap)

        Log.d(STEP_TAG, "Refresh the Course Grade Page.")
        courseGradesPage.refresh() //First go to the top of the recycler view
        courseGradesPage.refresh() //Actual refresh

        Log.d(STEP_TAG, "Assert that the Total Grade is 49.47% and all of the assignment grades are displayed properly. We now show numeric grades because restriction to quantitative data has been disabled.")
        courseGradesPage.assertTotalGrade(ViewMatchers.withText("49.47%"))
        courseGradesPage.assertAssignmentDisplayed(pointsTextAssignment.name, "12/15")
        courseGradesPage.assertAssignmentDisplayed(percentageAssignment.name, "66.67%")
        courseGradesPage.assertAssignmentDisplayed(letterGradeAssignment.name, "11.4/15 (C)")
        courseGradesPage.assertAssignmentDisplayed(passFailAssignment.name, "Incomplete")
        courseGradesPage.swipeUp()
        courseGradesPage.assertAssignmentDisplayed(gpaScaleAssignment.name, "3.7/15 (F)")
    }
}