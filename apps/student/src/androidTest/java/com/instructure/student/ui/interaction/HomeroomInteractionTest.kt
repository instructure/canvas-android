/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.StubLandscape
import com.instructure.canvas.espresso.mockCanvas.*
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigPrefs
import com.instructure.espresso.page.getStringFromResource
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.R
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLoginElementary
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class HomeroomInteractionTest : StudentTest() {

    override fun displaysPageObjects() = Unit

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testAnnouncementsAndCoursesShowUpOnHomeroom() {
        val data = createMockDataWithHomeroomCourse(courseCount = 3)
        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val user = data.users.values.first()

        val homeroomAnnouncement = data.addDiscussionTopicToCourse(homeroomCourse, user, isAnnouncement = true)

        goToHomeroomPage(data)

        homeroomPage.assertPageObjects()

        val student = data.students[0]
        homeroomPage.assertWelcomeText(student.shortName!!)
        homeroomPage.assertAnnouncementDisplayed(homeroomCourse.name, homeroomAnnouncement.title!!, homeroomAnnouncement.message!!)

        homeroomPage.assertCourseItemsCount(3)
        data.courses.values
            .filter { !it.homeroomCourse }
            .forEach {
                homeroomPage.assertCourseDisplayed(it.name, homeroomPage.getStringFromResource(R.string.nothingDueToday), "")
            }
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testOnlyCoursesShowUpOnHomeroomIfNoHomeroomAnnouncement() {
        val data = createMockDataWithHomeroomCourse(courseCount = 3)

        goToHomeroomPage(data)

        homeroomPage.assertPageObjects()

        val student = data.students[0]
        homeroomPage.assertWelcomeText(student.shortName!!)
        homeroomPage.assertAnnouncementNotDisplayed()

        homeroomPage.assertCourseItemsCount(3)
        data.courses.values
            .filter { !it.homeroomCourse }
            .forEach {
                homeroomPage.assertCourseDisplayed(it.name, homeroomPage.getStringFromResource(R.string.nothingDueToday), "")
            }
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testOnlyAnnouncementShowsUpOnHomeroomIfNoCourses() {
        val data = createMockDataWithHomeroomCourse()
        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val user = data.users.values.first()

        val homeroomAnnouncement = data.addDiscussionTopicToCourse(homeroomCourse, user, isAnnouncement = true)

        goToHomeroomPage(data)

        val student = data.students[0]
        homeroomPage.assertWelcomeText(student.shortName!!)
        homeroomPage.assertAnnouncementDisplayed(homeroomCourse.name, homeroomAnnouncement.title!!, homeroomAnnouncement.message!!)

        homeroomPage.assertCourseItemsCount(0)
        homeroomPage.assertNoSubjectsTextDisplayed()
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testOpenCourse() {
        val data = createMockDataWithHomeroomCourse(courseCount = 3)
        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val user = data.users.values.first()

        data.addDiscussionTopicToCourse(homeroomCourse, user, isAnnouncement = true)

        val courses = data.courses.values.filter { !it.homeroomCourse }

        goToHomeroomPage(data)

        homeroomPage.assertPageObjects()

        homeroomPage.openCourse(courses[0].name)

        elementaryCoursePage.assertPageObjects()
        elementaryCoursePage.assertTitleCorrect(courses[0].originalName!!)
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testRefreshAfterEnrolledToCourses() {
        val data = createMockDataWithHomeroomCourse()

        goToHomeroomPage(data)

        homeroomPage.assertHomeroomContentNotDisplayed()
        homeroomPage.assertCourseItemsCount(0)
        homeroomPage.assertEmptyViewDisplayed()

        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val user = data.users.values.first()

        val homeroomAnnouncement = data.addDiscussionTopicToCourse(homeroomCourse, user, isAnnouncement = true)

        val student = data.students[0]
        data.addCourseWithEnrollment(student, Enrollment.EnrollmentType.Student)
        data.addCourseWithEnrollment(student, Enrollment.EnrollmentType.Student)

        homeroomPage.refresh()

        homeroomPage.assertWelcomeText(student.shortName!!)
        homeroomPage.assertAnnouncementDisplayed(homeroomCourse.name, homeroomAnnouncement.title!!, homeroomAnnouncement.message!!)

        homeroomPage.assertCourseItemsCount(2)
        data.courses.values
            .filter { !it.homeroomCourse }
            .forEach {
                homeroomPage.assertCourseDisplayed(it.name, homeroomPage.getStringFromResource(R.string.nothingDueToday), "")
            }
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testOpenHomeroomCourseAnnouncements() {
        val data = createMockDataWithHomeroomCourse(courseCount = 3, homeroomCourseCount = 2)
        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val user = data.users.values.first()


        val homeroomAnnouncement = data.addDiscussionTopicToCourse(homeroomCourse, user, isAnnouncement = true)

        goToHomeroomPage(data)

        homeroomPage.assertPageObjects()

        val student = data.students[0]
        homeroomPage.assertWelcomeText(student.shortName!!)
        homeroomPage.assertAnnouncementDisplayed(homeroomCourse.name, homeroomAnnouncement.title!!, homeroomAnnouncement.message!!)

        homeroomPage.clickOnViewPreviousAnnouncements()

        announcementListPage.assertToolbarTitle()
        announcementListPage.assertAnnouncementTitleVisible(homeroomAnnouncement.title!!)
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testOpenCourseAnnouncements() {
        val data = createMockDataWithHomeroomCourse(courseCount = 1)

        val user = data.users.values.first()
        val courses = data.courses.values.filter { !it.homeroomCourse }
        val courseAnnouncement = data.addDiscussionTopicToCourse(courses[0], user, isAnnouncement = true)

        goToHomeroomPage(data)

        homeroomPage.assertPageObjects()

        homeroomPage.openCourseAnnouncement(courseAnnouncement.title!!)

        discussionDetailsPage.assertPageObjects()
        discussionDetailsPage.assertTitleText(courseAnnouncement.title!!)
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testShowCourseCardWithAnnouncement() {
        val data = createMockDataWithHomeroomCourse(courseCount = 3)
        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val user = data.users.values.first()

        data.addDiscussionTopicToCourse(homeroomCourse, user, isAnnouncement = true)

        val courses = data.courses.values.filter { !it.homeroomCourse }
        val courseAnnouncement = data.addDiscussionTopicToCourse(courses[0], user, isAnnouncement = true)

        goToHomeroomPage(data)

        homeroomPage.assertPageObjects()

        homeroomPage.assertCourseDisplayed(courses[0].name, homeroomPage.getStringFromResource(R.string.nothingDueToday), courseAnnouncement.title!!)
        homeroomPage.assertCourseDisplayed(courses[1].name, homeroomPage.getStringFromResource(R.string.nothingDueToday), "")
        homeroomPage.assertCourseDisplayed(courses[2].name, homeroomPage.getStringFromResource(R.string.nothingDueToday), "")
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testDueTodayAndMissingAssignments() {
        val data = createMockDataWithHomeroomCourse(courseCount = 1)
        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val user = data.users.values.first()

        data.addDiscussionTopicToCourse(homeroomCourse, user, isAnnouncement = true)

        val courses = data.courses.values.filter { !it.homeroomCourse }

        data.addAssignment(courses[0].id, submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        data.addAssignment(courses[0].id, submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY)

        goToHomeroomPage(data)

        homeroomPage.assertPageObjects()

        // With the current implementation of MockCanvas, all the assignments will show up as due today and missing, because both Mock endpoints will return all the assignments.
        // This cannot happen in normal circumstances, but for testing the UI it's fine.
        // We can add a more sophisticated approach when other tests will need it.
        // Verifying the logic that one can be due today or missing only is covered by unit tests.
        homeroomPage.assertToDoText("2 due today | 2 missing")
    }

    @StubLandscape
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testOpenAssignments() {
        val data = createMockDataWithHomeroomCourse(courseCount = 1)
        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val user = data.users.values.first()

        data.addDiscussionTopicToCourse(homeroomCourse, user, isAnnouncement = true)

        val courses = data.courses.values.filter { !it.homeroomCourse }

        val assignment1 = data.addAssignment(courses[0].id, submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        data.addAssignment(courses[0].id, submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY)

        goToHomeroomPage(data)

        homeroomPage.assertPageObjects()
        homeroomPage.openAssignments("2 due today | 2 missing")

        assignmentListPage.assertPageObjects()
        assignmentListPage.assertHasAssignment(assignment1)
    }

    @Test
    @TestMetaData(Priority.P2, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testEmptyState() {
        val data = createMockDataWithHomeroomCourse()

        goToHomeroomPage(data)

        homeroomPage.assertHomeroomContentNotDisplayed()
        homeroomPage.assertCourseItemsCount(0)
        homeroomPage.assertEmptyViewDisplayed()
    }

    private fun createMockDataWithHomeroomCourse(
        courseCount: Int = 0,
        pastCourseCount: Int = 0,
        favoriteCourseCount: Int = 0,
        announcementCount: Int = 0,
        homeroomCourseCount: Int = 1): MockCanvas {

        // We have to add this delay to be sure that the remote config is already fetched before we want to override remote config values.
        Thread.sleep(3000)
        RemoteConfigPrefs.putString(RemoteConfigParam.K5_DESIGN.rc_name, "true")

        val data = MockCanvas.init(
            studentCount = 1,
            courseCount = courseCount,
            pastCourseCount = pastCourseCount,
            favoriteCourseCount = favoriteCourseCount,
            accountNotificationCount = announcementCount,
            homeroomCourseCount = homeroomCourseCount)

        return data
    }

    private fun goToHomeroomPage(data: MockCanvas) {
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLoginElementary(data.domain, token, student)
        elementaryDashboardPage.waitForRender()
    }
}