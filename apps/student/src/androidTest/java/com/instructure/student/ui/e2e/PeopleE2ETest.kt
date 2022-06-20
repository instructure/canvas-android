/*
 * Copyright (C) 2019 - present Instructure, Inc.
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

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.ViewUtils
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

private const val STEP_TAG = "PeopleE2ETest #STEP# "
private const val PREPARATION_TAG = "PeopleE2ETest #PREPARATION# "

@HiltAndroidTest
class PeopleE2ETest : StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enableAndConfigureAccessibilityChecks() {
        //We don't want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.PEOPLE, TestCategory.E2E)
    fun testPeopleE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student1 = data.studentsList[0]
        val student2 = data.studentsList[1]

        Log.d(STEP_TAG, "Login with user: ${student1.name}, login id: ${student1.loginId} , password: ${student1.password}")
        tokenLogin(student1)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Navigate to ${course.name} course's People Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectPeople()

        Log.d(STEP_TAG,"Assert that the teacher user and both of the student users has been displayed.")
        peopleListPage.assertPersonListed(teacher)
        peopleListPage.assertPersonListed(student1)
        peopleListPage.assertPersonListed(student2)
        peopleListPage.assertPeopleCount(5) //2 for Teachers and Students sections, 1 for teacher user and 2 for student users.

        Log.d(STEP_TAG,"Collapse student list and assert that the students are not displayed, but the teacher user is displayed.")
        peopleListPage.clickOnStudentsExpandCollapseButton()
        peopleListPage.assertPersonListed(teacher)
        peopleListPage.assertPeopleCount(3) //2 for Teachers and Students sections, and 3rd for the teacher user.
        peopleListPage.clickOnStudentsExpandCollapseButton()
        peopleListPage.assertPersonListed(student1)
        peopleListPage.assertPersonListed(student2)
        peopleListPage.assertPeopleCount(5) //2 for Teachers and Students sections, 1 for teacher user and 2 for student users.

        Log.d(STEP_TAG,"Select ${student2.name} student and assert if we are landing on the Person Details Page.")
        peopleListPage.selectPerson(student2)
        personDetailsPage.assertPageObjects()

        Log.d(STEP_TAG,"Compose a new message for ${student2.name} student.")
        personDetailsPage.clickCompose()
        newMessagePage.populateMessage(course,student2,"Yo!", "Hello from a fellow student", recipientPopulated = true)

        Log.d(STEP_TAG,"Send the message and assert if we are landing on the Person Details Page.")
        newMessagePage.clickSend()
        personDetailsPage.assertPageObjects()

        Log.d(STEP_TAG,"Navigate back to the Dashboard (Course List) Page.")
        ViewUtils.pressBackButton(3)

        Log.d(STEP_TAG,"Sign out with ${student1.name} student.")
        dashboardPage.logOut()

        Log.d(STEP_TAG, "Login with user: ${student2.name}, login id: ${student2.loginId} , password: ${student2.password}")
        tokenLogin(student2)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Click on the 'Inbox' bottom menu and assert that ${student1.name}'s message is displayed.")
        dashboardPage.clickInboxTab()
        inboxPage.assertConversationDisplayed("Yo!")
    }

}