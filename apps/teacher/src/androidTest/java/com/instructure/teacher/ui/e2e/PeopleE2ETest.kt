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
package com.instructure.teacher.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.ViewUtils
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.pages.PersonContextPage
import com.instructure.teacher.ui.utils.*
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class PeopleE2ETest: TeacherTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.PEOPLE, TestCategory.E2E)
    fun testPeopleE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(teachers = 2, students = 2, parents = 1, courses = 1)
        val teacher = data.teachersList[0]
        val notGradedStudent = data.studentsList[0]
        val gradedStudent = data.studentsList[1]
        val course = data.coursesList[0]
        val parent = data.parentsList[0]

        Log.d(PREPARATION_TAG,"Seed a 'Text Entry' assignment for course: ${course.name}.")
        val assignments = seedAssignments(
                courseId = course.id,
                dueAt = 1.days.fromNow.iso8601,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                pointsPossible = 10.0
        )

        Log.d(PREPARATION_TAG,"Seed a submission for ${assignments[0].name} assignment.")
        seedAssignmentSubmission(
                submissionSeeds = listOf(SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )),
                assignmentId = assignments[0].id,
                courseId = course.id,
                studentToken = gradedStudent.token
        )

        Log.d(PREPARATION_TAG,"Grade the previously seeded submission for ${assignments[0].name} assignment.")
        gradeSubmission(teacher, course, assignments, gradedStudent)

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId}.")
        tokenLogin(teacher)

        Log.d(STEP_TAG,"Open ${course.name} course and navigate to People Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openPeopleTab()

        Log.d(STEP_TAG,"Click on '${teacher.name}', the teacher person and assert the that the teacher course info and the corresponding section name is displayed on Context Page.")
        peopleListPage.clickPerson(teacher)
        personContextPage.assertDisplaysCourseInfo(course)
        personContextPage.assertSectionNameView(PersonContextPage.UserRole.TEACHER)

        Log.d(STEP_TAG,"Navigate back and click on '${parent.name}', the parent (observer) person and assert the that the observer course info and the corresponding section name are displayed on Context Page.")
        Espresso.pressBack()
        peopleListPage.clickPerson(parent)
        personContextPage.assertDisplaysCourseInfo(course)
        personContextPage.assertSectionNameView(PersonContextPage.UserRole.OBSERVER)

        Log.d(STEP_TAG,"Navigate back and click on ${notGradedStudent.name} student and assert that the NOT GRADED student course info and the corresponding section name is displayed are displayed properly on Context Page.")
        Espresso.pressBack()
        peopleListPage.clickPerson(notGradedStudent)
        studentContextPage.assertDisplaysStudentInfo(notGradedStudent)
        studentContextPage.assertSectionNameView(PersonContextPage.UserRole.STUDENT)
        studentContextPage.assertDisplaysCourseInfo(course)
        studentContextPage.assertStudentGrade("--")
        studentContextPage.assertStudentSubmission("--")

        Log.d(STEP_TAG,"Navigate back and click on ${gradedStudent.name} student." +
                "Assert that '${gradedStudent.name}' graded student's info," +
                "and the '${course.name}' course's info are displayed properly on the Context Page.")
        Espresso.pressBack()
        peopleListPage.clickPerson(gradedStudent)
        studentContextPage.assertDisplaysStudentInfo(gradedStudent)
        studentContextPage.assertDisplaysCourseInfo(course)
        studentContextPage.assertSectionNameView(PersonContextPage.UserRole.STUDENT)
        studentContextPage.assertStudentGrade("100.0")
        studentContextPage.assertStudentSubmission("1")
        studentContextPage.assertAssignmentListed(assignments[0].name)

        Log.d(STEP_TAG, "Click on the '+' (aka. Compose new message) button.")
        studentContextPage.clickOnNewMessageButton()

        val subject = "Test Subject"
        Log.d(STEP_TAG,"Fill in the 'Subject' field with the value: $subject.")
        addMessagePage.addSubject(subject)

        Log.d(STEP_TAG,"Add some message text and click on 'Send' (aka. 'Arrow') button.")
        addMessagePage.addMessage("This a test message from student context page.")
        addMessagePage.clickSendButton()

        Log.d(STEP_TAG, "Navigate back to People List Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on 'Search' (magnifying glass) icon and type '${gradedStudent.name}', the graded student's name to the search input field.")
        peopleListPage.clickSearchButton()
        peopleListPage.typeSearchInput(gradedStudent.name)

        Log.d(STEP_TAG, "Assert that only 1 person matches for the search text, and it is '${gradedStudent.name}', the graded student.")
        peopleListPage.assertSearchResultCount(1)
        peopleListPage.assertPersonListed(gradedStudent)

        Log.d(STEP_TAG, "Click on 'Reset' search (cross) icon and assert that the empty view is displayed.")
        peopleListPage.clickResetSearchText()
        peopleListPage.assertEmptyViewIsDisplayed()

        Log.d(STEP_TAG, "Navigate back to Dashboard Page. Click on the Inbox bottom menu. Assert that the 'All' section is empty.")
        ViewUtils.pressBackButton(4)
        dashboardPage.openInbox()
        inboxPage.assertInboxEmpty()

        Log.d(STEP_TAG,"Filter the Inbox by selecting 'Sent' category from the spinner on Inbox Page.")
        inboxPage.filterInbox("Sent")

        Log.d(STEP_TAG,"Assert that the previously sent conversation is displayed.")
        inboxPage.assertHasConversation()
     }

    private fun gradeSubmission(
        teacher: CanvasUserApiModel,
        course: CourseApiModel,
        assignments: List<AssignmentApiModel>,
        gradedStudent: CanvasUserApiModel
    ) {
        SubmissionsApi.gradeSubmission(
            teacherToken = teacher.token,
            courseId = course.id,
            assignmentId = assignments[0].id,
            studentId = gradedStudent.id,
            postedGrade = "10",
            excused = false
        )
    }
}