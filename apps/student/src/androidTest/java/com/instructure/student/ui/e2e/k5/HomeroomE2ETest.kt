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
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.page.getStringFromResource
import com.instructure.student.R
import com.instructure.student.ui.pages.ElementaryDashboardPage
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedDataForK5
import com.instructure.student.ui.utils.tokenLoginElementary
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

@HiltAndroidTest
class HomeroomE2ETest : StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @Stub // TODO: Investigate flaky test
    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.E2E, SecondaryFeatureCategory.HOMEROOM)
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

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for '${nonHomeroomCourses[2].name}' course.")
        val testAssignment = AssignmentsApi.createAssignment(nonHomeroomCourses[2].id, teacher.token, gradingType = GradingType.LETTER_GRADE, pointsPossible = 100.0, dueAt = OffsetDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_DATE_TIME), submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' MISSING assignment for ${nonHomeroomCourses[2].name} course.")
        val testAssignmentMissing = AssignmentsApi.createAssignment(nonHomeroomCourses[2].id, teacher.token, gradingType = GradingType.PERCENT, pointsPossible = 100.0, dueAt =  3.days.ago.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLoginElementary(student)
        elementaryDashboardPage.assertPageObjects()

        Log.d(STEP_TAG, "Navigate to K5 Important Dates Page and assert it is loaded.")
        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.HOMEROOM)

        Log.d(STEP_TAG, "Assert that there is a welcome text with the student's shortname: '${student.shortName}'.")
        homeroomPage.assertWelcomeText(student.shortName)

        Log.d(STEP_TAG, "Assert that the '${homeroomAnnouncement.title}' announcement (which belongs to '${homeroomCourse.name}' homeroom course) is displayed.")
        homeroomPage.assertAnnouncementDisplayed(homeroomCourse.name, homeroomAnnouncement.title, homeroomAnnouncement.message)

        Log.d(STEP_TAG, "Assert that under the 'My Subject' section there are 3 items.")
        homeroomPage.assertCourseItemsCount(3)

        Log.d(STEP_TAG, "Click on 'View Previous Announcements'." +
                "Assert that the Announcement List Page is displayed" +
                "and the '${homeroomAnnouncement.title}' announcement is displayed as well within the announcement list..")
        homeroomPage.clickOnViewPreviousAnnouncements()
        announcementListPage.assertToolbarTitle()
        announcementListPage.assertAnnouncementTitleVisible(homeroomAnnouncement.title)

        Log.d(STEP_TAG, "Navigate back to Homeroom Page and assert it is displayed well.")
        Espresso.pressBack()
        homeroomPage.assertPageObjects()
        elementaryDashboardPage.waitForRender()

        for (i in 0 until nonHomeroomCourses.size - 1) {
            Log.d(STEP_TAG, "Assert that the '${nonHomeroomCourses[i].name}' course is displayed with the announcements which belongs to it.")
            homeroomPage.assertCourseDisplayed(
                nonHomeroomCourses[i].name,
                homeroomPage.getStringFromResource(R.string.nothingDueToday),
                data.announcementsList[i + 1].title
            )
        }

        Log.d(STEP_TAG, "Assert To Do text that it count the one missing, and the other not missing assignment as well.")
        homeroomPage.assertPageObjects()
        homeroomPage.assertToDoText("1 due today | 1 missing")

        Log.d(STEP_TAG, "Open '${nonHomeroomCourses[0].name}' course." +
                "Assert that the Course Details Page is displayed and the title is '${nonHomeroomCourses[0].name}' (the course's name).")
        homeroomPage.openCourse(nonHomeroomCourses[0].name)
        elementaryCoursePage.assertPageObjects()
        elementaryCoursePage.assertTitleCorrect(nonHomeroomCourses[0].name)

        Log.d(STEP_TAG, "Navigate back to Homeroom Page. Open '${data.announcementsList[1].title}' announcement by clicking on it.")
        Espresso.pressBack()
        homeroomPage.assertPageObjects()
        homeroomPage.openCourseAnnouncement(data.announcementsList[1].title)

        Log.d(STEP_TAG, "Assert that the '${data.announcementsList[1].title}' discussion's details page is displayed.")
        discussionDetailsPage.assertHomeroomToolbarDiscussionTitle(data.announcementsList[1].title)

        Log.d(STEP_TAG, "Navigate back to Homeroom Page. Open the Assignment List Page of '${nonHomeroomCourses[2].name}' course.")
        Espresso.pressBack()
        homeroomPage.assertPageObjects()
        homeroomPage.openAssignments("1 due today | 1 missing")

        Log.d(STEP_TAG, "Assert that the Assignment list page of '${nonHomeroomCourses[2].name}' course is loaded well" +
                "and the corresponding assignments (Not missing: '${testAssignment.name}', missing: '${testAssignmentMissing.name}') are displayed.")
        assignmentListPage.assertPageObjects()
        assignmentListPage.assertHasAssignment(testAssignment)
        assignmentListPage.assertHasAssignment(testAssignmentMissing)
    }
}

