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
package com.instructure.student.ui.e2e.k5

import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.pages.ElementaryDashboardPage
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedDataForK5
import com.instructure.student.ui.utils.tokenLoginElementary
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ResourcesE2ETest : StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enableAndConfigureAccessibilityChecks() {
        //We dont want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.K5_DASHBOARD, TestCategory.E2E)
    fun resourcesE2ETest() {

        // Seed data for K5 sub-account
        val syllabusBodyString = "this is the syllabus body..."
        val data = seedDataForK5(
            teachers = 1,
            students = 1,
            courses = 4,
            homeroomCourses = 1,
            syllabusBody = syllabusBodyString
        )

        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val nonHomeroomCourses = data.coursesList.filter { !it.homeroomCourse }

        // Sign in with elementary (K5) student
        tokenLoginElementary(student)
        elementaryDashboardPage.waitForRender()
        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.RESOURCES)
        resourcesPage.assertPageObjects()

        //Verify if important links, LTI tools and contacts are displayed
        verifyResourcesPageAssertions(teacher)

        //Compose message to a contact, and verify if the new message page is displayed
        resourcesPage.openComposeMessage(teacher.shortName)
        assertNewMessagePageDisplayed()
        Espresso.pressBack()
        resourcesPage.assertPageObjects()

        //Refresh the resources page and assert if important links, LTI tools and contact are displayed
        resourcesPage.refresh()
        resourcesPage.assertPageObjects()
        verifyResourcesPageAssertions(teacher)

        //Open an LTI tool, and verify if all the NON-homeroom courses are displayed within the 'Choose a Course' list.
        resourcesPage.openLtiApp("Google Drive")
        nonHomeroomCourses.forEach {
            resourcesPage.assertCourseShown(it.name)
        }
    }

    private fun verifyResourcesPageAssertions(
        teacher: CanvasUserApiModel
    ) {
        resourcesPage.assertImportantLinksHeaderDisplayed()
        resourcesPage.assertStudentApplicationsHeaderDisplayed()
        resourcesPage.assertStaffInfoHeaderDisplayed()
        resourcesPage.assertStaffDisplayed(teacher.shortName)
    }

    private fun assertNewMessagePageDisplayed() {
        newMessagePage.assertToolbarTitleNewMessage()
        newMessagePage.assertCourseSelectorNotShown()
        newMessagePage.assertRecipientsNotShown()
        newMessagePage.assertSendIndividualMessagesNotShown()
        newMessagePage.assertSubjectViewShown()
        newMessagePage.assertMessageViewShown()
    }
}

