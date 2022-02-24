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
import java.text.SimpleDateFormat
import java.util.*

@HiltAndroidTest
class HomeroomE2ETest : StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enableAndConfigureAccessibilityChecks() {
        //We dont want to see accessibility errors on E2E tests
    }

    @E2E
    @FlakyE2E("Need to investigate why is it breaking when asserting todo text. Timezone shouldn't be a problem anymore since we run these tests at 8 PM.")
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.E2E)
    fun homeroomE2ETest() {

        // Seed data for K5 sub-account
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

        val testAssignmentMissing = AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
                courseId = nonHomeroomCourses[2].id,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                gradingType = GradingType.PERCENT,
                teacherToken = teacher.token,
                pointsPossible = 100.0,
                dueAt = missingCalendar.time.toApiString()
            ))

        // Sign in with elementary (K5) student
        tokenLoginElementary(student)
        elementaryDashboardPage.assertPageObjects()
        elementaryDashboardPage.waitForRender()
        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.HOMEROOM)

        homeroomPage.assertWelcomeText(student.shortName)
        homeroomPage.assertAnnouncementDisplayed(
            homeroomCourse.name,
            homeroomAnnouncement.title,
            homeroomAnnouncement.message
        )

        homeroomPage.assertCourseItemsCount(3) //gives back the number of courses under 'My Subject' list
        homeroomPage.clickOnViewPreviousAnnouncements()
        announcementListPage.assertToolbarTitle()
        announcementListPage.assertAnnouncementTitleVisible(homeroomAnnouncement.title)
        Espresso.pressBack()
        homeroomPage.assertPageObjects()
        elementaryDashboardPage.waitForRender()

        for (i in 0 until nonHomeroomCourses.size - 1) {
            homeroomPage.assertCourseDisplayed(
                nonHomeroomCourses[i].name,
                homeroomPage.getStringFromResource(R.string.nothingDueToday),
                data.announcementsList[i + 1].title
            )
        }
        homeroomPage.assertPageObjects()
        homeroomPage.assertToDoText("1 due today | 1 missing")
        homeroomPage.openCourse(nonHomeroomCourses[0].name)

        elementaryCoursePage.assertPageObjects()
        elementaryCoursePage.assertTitleCorrect(nonHomeroomCourses[0].name)
        Espresso.pressBack()
        homeroomPage.assertPageObjects()
        homeroomPage.openCourseAnnouncement(data.announcementsList[1].title)

        discussionDetailsPage.assertTitleText(data.announcementsList[1].title)
        Espresso.pressBack()
        homeroomPage.assertPageObjects()
        homeroomPage.openAssignments("1 due today | 1 missing")

        assignmentListPage.assertPageObjects()
        assignmentListPage.assertHasAssignment(testAssignment)
        assignmentListPage.assertHasAssignment(testAssignmentMissing)
    }
}

