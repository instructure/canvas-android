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
package com.instructure.student.ui.e2e.k5

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.student.ui.pages.ElementaryDashboardPage
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.seedDataForK5
import com.instructure.student.ui.utils.tokenLoginElementary
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ResourcesE2ETest : StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.E2E, SecondaryFeatureCategory.RESOURCES)
    fun resourcesE2ETest() {

        Log.d(PREPARATION_TAG, "Seeding data for K5 sub-account.")
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

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLoginElementary(student)
        elementaryDashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to K5 Resources Page.")
        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.RESOURCES)

        Log.d(ASSERTION_TAG, "Assert that the K5 Resources Page is displayed correctly.")
        resourcesPage.assertPageObjects()

        Log.d(ASSERTION_TAG, "Assert that the important links, LTI tools and contacts are displayed.")
        assertElementaryResourcesPageInformation(teacher)

        Log.d(STEP_TAG, "Click on the compose message icon next to a contact ('${teacher.name}' teacher).")
        resourcesPage.openComposeMessage(teacher.shortName)

        Log.d(ASSERTION_TAG, "Assert that the new message page is displayed.")
        assertNewMessagePageDisplayed()

        Log.d(STEP_TAG, "Navigate back to K5 Resources Page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the K5 Resources Page is displayed correctly.")
        resourcesPage.assertPageObjects()

        Log.d(ASSERTION_TAG, "Assert that the important links, LTI tools and contacts are still displayed correctly, after the navigation.")
        assertElementaryResourcesPageInformation(teacher)

        Log.d(STEP_TAG, "Open an LTI tool (Google Drive).")
        resourcesPage.openLtiApp("Google Drive")

        Log.d(ASSERTION_TAG, "Assert if all the NON-homeroom courses are displayed within the 'Choose a Course' list.")
        nonHomeroomCourses.forEach {
            resourcesPage.assertCourseShown(it.name)
        }
    }

    private fun assertElementaryResourcesPageInformation(teacher: CanvasUserApiModel) {
        resourcesPage.assertImportantLinksHeaderDisplayed()
        resourcesPage.assertStudentApplicationsHeaderDisplayed()
        resourcesPage.assertStaffInfoHeaderDisplayed()
        resourcesPage.assertStaffDisplayed(teacher.shortName)
    }

    private fun assertNewMessagePageDisplayed() {
        inboxComposePage.assertTitle("New Message")
    }
}

