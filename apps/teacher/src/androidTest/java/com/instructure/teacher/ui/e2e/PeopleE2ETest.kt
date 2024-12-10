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
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.api.GroupsApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.ViewUtils
import com.instructure.teacher.ui.pages.PeopleListPage
import com.instructure.teacher.ui.pages.PersonContextPage
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.seedAssignmentSubmission
import com.instructure.teacher.ui.utils.seedAssignments
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.lang.Thread.sleep

@HiltAndroidTest
class PeopleE2ETest: TeacherComposeTest() {

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

        Log.d(PREPARATION_TAG,"Seed some group info.")
        val groupCategory = GroupsApi.createCourseGroupCategory(data.coursesList[0].id, teacher.token)
        val groupCategory2 = GroupsApi.createCourseGroupCategory(data.coursesList[0].id, teacher.token)
        val group = GroupsApi.createGroup(groupCategory.id, teacher.token)
        val group2 = GroupsApi.createGroup(groupCategory2.id, teacher.token)

        Log.d(PREPARATION_TAG,"Create group membership for '${gradedStudent.name}' student to '${group.name}' group.")
        GroupsApi.createGroupMembership(group.id, gradedStudent.id, teacher.token)

        Log.d(PREPARATION_TAG,"Create group membership for '${notGradedStudent.name}' student to '${group2.name}' group.")
        GroupsApi.createGroupMembership(group2.id, notGradedStudent.id, teacher.token)

        Log.d(PREPARATION_TAG,"Seed a 'Text Entry' assignment for course: '${course.name}'.")
        val assignments = seedAssignments(
                courseId = course.id,
                dueAt = 1.days.fromNow.iso8601,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                pointsPossible = 10.0
        )

        Log.d(PREPARATION_TAG,"Seed a submission for '${assignments[0].name}' assignment.")
        seedAssignmentSubmission(
                submissionSeeds = listOf(SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )),
                assignmentId = assignments[0].id,
                courseId = course.id,
                studentToken = gradedStudent.token
        )

        Log.d(PREPARATION_TAG,"Grade the previously seeded submission for '${assignments[0].name}' assignment.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, assignments[0].id, gradedStudent.id, postedGrade = "10")

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)

        Log.d(STEP_TAG,"Open '${course.name}' course and navigate to People Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openPeopleTab()

        Log.d(STEP_TAG,"Click on '${teacher.name}', the teacher person and assert the that the teacher course info and the corresponding section name is displayed on Context Page.")
        peopleListPage.assertPersonRole(teacher.name, PeopleListPage.UserRole.TEACHER)
        peopleListPage.clickPerson(teacher)
        personContextPage.assertDisplaysCourseInfo(course)
        personContextPage.assertSectionNameView(PersonContextPage.UserRole.TEACHER)

        Log.d(STEP_TAG,"Navigate back and click on '${parent.name}', the parent (observer) person and assert the that the observer course info and the corresponding section name are displayed on Context Page.")
        Espresso.pressBack()
        peopleListPage.assertPersonRole(parent.name, PeopleListPage.UserRole.OBSERVER)
        peopleListPage.clickPerson(parent)
        personContextPage.assertDisplaysCourseInfo(course)
        personContextPage.assertSectionNameView(PersonContextPage.UserRole.OBSERVER)

        Log.d(STEP_TAG,"Navigate back and click on '${notGradedStudent.name}' student and assert that the NOT GRADED student course info and the corresponding section name is displayed are displayed properly on Context Page.")
        Espresso.pressBack()
        peopleListPage.assertPersonRole(notGradedStudent.name, PeopleListPage.UserRole.STUDENT)
        peopleListPage.clickPerson(notGradedStudent)
        studentContextPage.assertDisplaysStudentInfo(notGradedStudent)
        studentContextPage.assertSectionNameView(PersonContextPage.UserRole.STUDENT)
        studentContextPage.assertDisplaysCourseInfo(course)
        studentContextPage.assertStudentGrade("--")
        studentContextPage.assertStudentSubmission("--")

        Log.d(STEP_TAG,"Navigate back and click on '${gradedStudent.name}' student." +
                "Assert that '${gradedStudent.name}' graded student's info," +
                "and the '${course.name}' course's info are displayed properly on the Context Page.")
        Espresso.pressBack()
        peopleListPage.assertPersonRole(gradedStudent.name, PeopleListPage.UserRole.STUDENT)
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
        val body = "This a test message from student context page."
        Log.d(STEP_TAG,"Fill in the 'Subject' field with the value: '$subject'. Add some message text and click on 'Send' (aka. 'Arrow') button.")

        inboxComposePage.typeSubject(subject)
        inboxComposePage.typeBody(body)
        inboxComposePage.pressSendButton()

        Log.d(STEP_TAG, "Navigate back to People List Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on 'Search' (magnifying glass) icon and type '${gradedStudent.name}', the graded student's name to the search input field.")
        peopleListPage.searchable.clickOnSearchButton()
        peopleListPage.searchable.typeToSearchBar(gradedStudent.name)

        Log.d(STEP_TAG, "Assert that only 1 person matches for the search text, and it is '${gradedStudent.name}', the graded student.")
        peopleListPage.assertSearchResultCount(1)
        peopleListPage.assertPersonListed(gradedStudent)

        Log.d(STEP_TAG, "Click on 'Reset' search (X) icon and assert that all the people are displayed (5).")
        peopleListPage.searchable.clickOnClearSearchButton()
        peopleListPage.assertSearchResultCount(5)

        Log.d(STEP_TAG, "Quit from searching and navigate to People List page.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG, "Click on the 'Filter' icon on the top-right corner and select '${group.name}' group as a filter.")
        peopleListPage.clickOnPeopleFilterMenu()
        peopleListPage.selectFilter(listOf(group.name))

        Log.d(STEP_TAG, "Assert that the filter title is the previously selected, '${group.name}' group.")
        peopleListPage.assertFilterTitle(group.name)

        Log.d(STEP_TAG, "Assert that only 1 person matches for the filter, and it is '${gradedStudent.name}', the graded student.")
        peopleListPage.assertSearchResultCount(1)
        peopleListPage.assertPersonListed(gradedStudent)

        Log.d(STEP_TAG, "Clear the filter and assert that the list title became 'All People' and all the people are displayed again.")
        peopleListPage.clickOnClearFilter()
        sleep(1000) //Allow the clear filter process to propagate.
        peopleListPage.assertFilterTitle("All People")
        peopleListPage.assertSearchResultCount(5)

        Log.d(STEP_TAG, "Click on the 'Filter' icon on the top-right corner and select '${group.name}' and '${group2.name}' groups as a filters.")
        peopleListPage.clickOnPeopleFilterMenu()
        peopleListPage.selectFilter(listOf(group.name, group2.name))

        Log.d(STEP_TAG, "Assert that the filter title is the previously selected TWO groups: '${group.name}' and '${group2.name}'.")
        //The order of how the filter title is generated is inconsistent, so we check both way if group1 is the leading and if group2.
        try { peopleListPage.assertFilterTitle(group.name + ", " + group2.name) }
        catch(e: AssertionError) { peopleListPage.assertFilterTitle(group2.name + ", " + group.name) }

        Log.d(STEP_TAG, "Assert that only that 2 people matches for the filter, and they are '${gradedStudent.name}' and '${notGradedStudent.name}'.")
        peopleListPage.assertSearchResultCount(2)
        peopleListPage.assertPersonListed(gradedStudent)
        peopleListPage.assertPersonListed(notGradedStudent)

        Log.d(STEP_TAG, "Clear the filter and assert that the list title became 'All People' and all the people are displayed again.")
        peopleListPage.clickOnClearFilter()
        sleep(1000) //Allow the clear filter process to propagate.
        peopleListPage.assertFilterTitle("All People")
        peopleListPage.assertSearchResultCount(5)

        Log.d(STEP_TAG, "Navigate back to Dashboard Page. Click on the Inbox bottom menu. Assert that the 'All' section is empty.")
        ViewUtils.pressBackButton(2)
        dashboardPage.openInbox()
        inboxPage.assertInboxEmpty()

        Log.d(STEP_TAG,"Filter the Inbox by selecting 'Sent' category from the spinner on Inbox Page.")
        inboxPage.filterInbox("Sent")

        Log.d(STEP_TAG,"Assert that the previously sent conversation is displayed.")
        inboxPage.assertHasConversation()
     }

}