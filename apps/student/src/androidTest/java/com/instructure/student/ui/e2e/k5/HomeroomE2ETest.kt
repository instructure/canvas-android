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
import com.instructure.canvas.espresso.FlakyE2E
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.espresso.page.getStringFromResource
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.R
import com.instructure.student.ui.pages.ElementaryDashboardPage
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedDataForK5
import com.instructure.student.ui.utils.tokenLoginElementary
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.util.*

@HiltAndroidTest
class HomeroomE2ETest : StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @FlakyE2E("Need to investigate why is it breaking when asserting todo text. Timezone shouldn't be a problem anymore since we run these tests at 8 PM.")
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.K5_DASHBOARD, TestCategory.E2E)
    fun homeroomE2ETest() {

        Log.d(PREPARATION_TAG,"Seeding data for K5 sub-account.")
        val data = seedDataForK5(
            teachers = 1,
            students = 1,
            courses = 4,
            homeroomCourses = 1,
            announcements = 3
        )

        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val homeroomCourse = data.coursesList[0]
        val homeroomAnnouncement = data.announcementsList[0]
        val nonHomeroomCourses = data.coursesList.filter { !it.homeroomCourse }

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 55)
        calendar.set(Calendar.SECOND, 59)

        val missingCalendar = Calendar.getInstance()

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for ${nonHomeroomCourses[2].name} course.")
        val testAssignment = AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
                courseId = nonHomeroomCourses[2].id,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                gradingType = GradingType.LETTER_GRADE,
                teacherToken = teacher.token,
                pointsPossible = 100.0,
                dueAt = calendar.time.toApiString()
            )
        )

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' MISSING assignment for ${nonHomeroomCourses[2].name} course.")
        val testAssignmentMissing = AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
                courseId = nonHomeroomCourses[2].id,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                gradingType = GradingType.PERCENT,
                teacherToken = teacher.token,
                pointsPossible = 100.0,
                dueAt = missingCalendar.time.toApiString()
            ))

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLoginElementary(student)
        elementaryDashboardPage.assertPageObjects()

        Log.d(STEP_TAG, "Navigate to K5 Important Dates Page and assert it is loaded.")
        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.HOMEROOM)
        //TODO: Maybe at this point is why this test is flaky. Suggestion: Check if homeroomPage's page object has been loaded, and check if homeroomPage and elementaryDashboardPage is different at all.

        Log.d(STEP_TAG, "Assert that there is a welcome text with the student's shortname (${student.shortName}).")
        homeroomPage.assertWelcomeText(student.shortName)

        Log.d(STEP_TAG, "Assert that the ${homeroomAnnouncement.title} announcement (which belongs to ${homeroomCourse.name} homeroom course) is displayed.")
        homeroomPage.assertAnnouncementDisplayed(
            homeroomCourse.name,
            homeroomAnnouncement.title,
            homeroomAnnouncement.message
        )

        Log.d(STEP_TAG, "Assert that under the 'My Subject' section there are 3 items.")
        homeroomPage.assertCourseItemsCount(3) //gives back the number of courses under 'My Subject' list

        Log.d(STEP_TAG, "Click on 'View Previous Announcements'." +
                "Assert that the Announcement List Page is displayed" +
                "and the ${homeroomAnnouncement.title} announcement is displayed as well within the announcement list..")
        homeroomPage.clickOnViewPreviousAnnouncements()
        announcementListPage.assertToolbarTitle()
        announcementListPage.assertAnnouncementTitleVisible(homeroomAnnouncement.title)

        Log.d(STEP_TAG, "Navigate back to Homeroom Page and assert it is displayed well.")
        Espresso.pressBack()
        homeroomPage.assertPageObjects()
        elementaryDashboardPage.waitForRender() //TODO: might not need this here.

        for (i in 0 until nonHomeroomCourses.size - 1) {
            Log.d(STEP_TAG, "Assert that the ${nonHomeroomCourses[i].name} course is displayed with the announcements which belongs to it.")
            homeroomPage.assertCourseDisplayed(
                nonHomeroomCourses[i].name,
                homeroomPage.getStringFromResource(R.string.nothingDueToday),
                data.announcementsList[i + 1].title
            )
        }

        Log.d(STEP_TAG, "Assert To Do text that it count the one missing, and the other not missing assignment as well.")
        homeroomPage.assertPageObjects()
        homeroomPage.assertToDoText("1 due today | 1 missing")

        Log.d(STEP_TAG, "Open ${nonHomeroomCourses[0].name} course." +
                "Assert that the Course Details Page is displayed and the title is '${nonHomeroomCourses[0].name}' (the course's name).")
        homeroomPage.openCourse(nonHomeroomCourses[0].name)
        elementaryCoursePage.assertPageObjects()
        elementaryCoursePage.assertTitleCorrect(nonHomeroomCourses[0].name)

        Log.d(STEP_TAG, "Navigate back to Homeroom Page. Open ${data.announcementsList[1].title} announcement by clicking on it.")
        Espresso.pressBack()
        homeroomPage.assertPageObjects()
        homeroomPage.openCourseAnnouncement(data.announcementsList[1].title)

        Log.d(STEP_TAG, "Assert that the ${data.announcementsList[1].title} announcement's details page is displayed.")
        discussionDetailsPage.assertTitleText(data.announcementsList[1].title)

        Log.d(STEP_TAG, "Navigate back to Homeroom Page. Open the Assignment List Page of ${nonHomeroomCourses[2].name} course.")
        Espresso.pressBack()
        homeroomPage.assertPageObjects()
        homeroomPage.openAssignments("1 due today | 1 missing")

        Log.d(STEP_TAG, "Assert that the Assignment list page of ${nonHomeroomCourses[2].name} course is loaded well" +
                "and the corresponding assignments (Not missing: ${testAssignment.name}, missing: ${testAssignmentMissing.name}) are displayed.")
        assignmentListPage.assertPageObjects()
        assignmentListPage.assertHasAssignment(testAssignment)
        assignmentListPage.assertHasAssignment(testAssignmentMissing)
    }
}

