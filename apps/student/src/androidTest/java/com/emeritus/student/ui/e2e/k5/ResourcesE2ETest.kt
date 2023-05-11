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
package com.emeritus.student.ui.e2e.k5

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.emeritus.student.ui.pages.ElementaryDashboardPage
import com.emeritus.student.ui.utils.StudentTest
import com.emeritus.student.ui.utils.seedDataForK5
import com.emeritus.student.ui.utils.tokenLoginElementary
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ResourcesE2ETest : StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.K5_DASHBOARD, TestCategory.E2E)
    fun resourcesE2ETest() {

        Log.d(PREPARATION_TAG,"Seeding data for K5 sub-account.")
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

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLoginElementary(student)
        elementaryDashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to K5 Resources Page and assert it is loaded.")
        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.RESOURCES)
        resourcesPage.assertPageObjects()

        Log.d(STEP_TAG, "Assert that the important links, LTI tools and contacts are displayed.")
        assertElementaryResourcesPageInformations(teacher)

        Log.d(STEP_TAG, "Click on the compose message icon next to a contact (${teacher.name}), and verify if the new message page is displayed.")
        resourcesPage.openComposeMessage(teacher.shortName)
        assertNewMessagePageDisplayed()

        Log.d(STEP_TAG, "Navigate back to K5 Resources Page and assert that is displayed.")
        Espresso.pressBack()
        resourcesPage.assertPageObjects()

        Log.d(STEP_TAG, "Assert that the important links, LTI tools and contacts are still displayed correctly, after the navigation.")
        assertElementaryResourcesPageInformations(teacher)

        Log.d(STEP_TAG, "Open an LTI tool (Google Drive), and verify if all the NON-homeroom courses are displayed within the 'Choose a Course' list.")
        resourcesPage.openLtiApp("Google Drive")
        nonHomeroomCourses.forEach {
            resourcesPage.assertCourseShown(it.name)
        }
    }

    private fun assertElementaryResourcesPageInformations(
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

