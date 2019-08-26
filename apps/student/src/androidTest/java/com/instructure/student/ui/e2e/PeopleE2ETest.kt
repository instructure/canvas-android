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

import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.Stub
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import org.junit.Test

class PeopleE2ETest: StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Stub
    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.PEOPLE, TestCategory.E2E, true)
    fun testPeopleE2E() {
        // Seed basic data
        val data = seedData(students = 2, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student1 = data.studentsList[0]
        val student2 = data.studentsList[1]

        // Sign in with student1
        tokenLogin(student1)

        // Navigate to the "People" page of our course
        dashboardPage.waitForRender()
        dashboardPage.selectCourse(course)
        courseBrowserPage.minimizeToolbar() // Topic scrolling doesn't seem to work without this
        courseBrowserPage.selectPeople()

        // Assert that all people are listed
        peopleListPage.assertPersonListed(teacher)
        peopleListPage.assertPersonListed(student1)
        peopleListPage.assertPersonListed(student2)

        // Let's send a message to student2
        peopleListPage.selectPerson(student2)
        personDetailsPage.assertPageObjects()
        personDetailsPage.clickCompose()
        newMessagePage.populateMessage(course,student2,"Yo!", "Hello from a fellow student", recipientPopulated = true)
        newMessagePage.hitSend()

        Espresso.pressBack() // Exit personDetailsPage
        Espresso.pressBack() // Exit peopleListPage
        Espresso.pressBack() // Exit courseBrowserPage

        // Sign out and back in as student2
        dashboardPage.signOut()
        tokenLogin(student2)
        dashboardPage.waitForRender()

        // Go to the inbox and make sure that student1's message is showing
        dashboardPage.clickInboxTab()
        inboxPage.assertConversationDisplayed("Yo!")

    }
}