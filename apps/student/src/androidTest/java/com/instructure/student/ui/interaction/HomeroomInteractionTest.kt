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

import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addCourseWithEnrollment
import com.instructure.canvas.espresso.mockCanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.di.graphql.CustomGradeStatusModule
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.espresso.page.getStringFromResource
import com.instructure.student.R
import com.instructure.student.ui.pages.ElementaryDashboardPage
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.tokenLoginElementary
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Test

@HiltAndroidTest
@UninstallModules(CustomGradeStatusModule::class)
class HomeroomInteractionTest : StudentComposeTest() {

    @BindValue
    @JvmField
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

    override fun displaysPageObjects() = Unit

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testAnnouncementsAndCoursesShowUpOnHomeroom() {
        val data = createMockDataWithHomeroomCourse(courseCount = 3)
        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val user = data.users.values.first()

        val homeroomAnnouncement = data.addDiscussionTopicToCourse(homeroomCourse, user, isAnnouncement = true)

        goToHomeroomTab(data)

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
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testOnlyCoursesShowUpOnHomeroomIfNoHomeroomAnnouncement() {
        val data = createMockDataWithHomeroomCourse(courseCount = 3)

        goToHomeroomTab(data)

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
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testOnlyAnnouncementShowsUpOnHomeroomIfNoCourses() {
        val data = createMockDataWithHomeroomCourse()
        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val user = data.users.values.first()

        val homeroomAnnouncement = data.addDiscussionTopicToCourse(homeroomCourse, user, isAnnouncement = true)

        goToHomeroomTab(data)

        val student = data.students[0]
        homeroomPage.assertWelcomeText(student.shortName!!)
        homeroomPage.assertAnnouncementDisplayed(homeroomCourse.name, homeroomAnnouncement.title!!, homeroomAnnouncement.message!!)

        homeroomPage.assertCourseItemsCount(0)
        homeroomPage.assertNoSubjectsTextDisplayed()
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testOpenCourse() {
        val data = createMockDataWithHomeroomCourse(courseCount = 3)
        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val user = data.users.values.first()

        data.addDiscussionTopicToCourse(homeroomCourse, user, isAnnouncement = true)

        val courses = data.courses.values.filter { !it.homeroomCourse }

        goToHomeroomTab(data)

        homeroomPage.assertPageObjects()

        homeroomPage.openCourse(courses[0].name)

        elementaryCoursePage.assertPageObjects()
        elementaryCoursePage.assertTitleCorrect(courses[0].originalName!!)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testRefreshAfterEnrolledToCourses() {
        val data = createMockDataWithHomeroomCourse()

        goToHomeroomTab(data)

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
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testOpenHomeroomCourseAnnouncements() {
        val data = createMockDataWithHomeroomCourse(courseCount = 3, homeroomCourseCount = 2)
        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val user = data.users.values.first()


        val homeroomAnnouncement = data.addDiscussionTopicToCourse(homeroomCourse, user, isAnnouncement = true)

        goToHomeroomTab(data)

        homeroomPage.assertPageObjects()

        val student = data.students[0]
        homeroomPage.assertWelcomeText(student.shortName!!)
        homeroomPage.assertAnnouncementDisplayed(homeroomCourse.name, homeroomAnnouncement.title!!, homeroomAnnouncement.message!!)

        homeroomPage.clickOnViewPreviousAnnouncements()

        announcementListPage.assertToolbarTitle()
        announcementListPage.assertAnnouncementTitleVisible(homeroomAnnouncement.title!!)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testOpenCourseAnnouncements() {
        val data = createMockDataWithHomeroomCourse(courseCount = 1)

        val user = data.users.values.first()
        val courses = data.courses.values.filter { !it.homeroomCourse }
        val courseAnnouncement = data.addDiscussionTopicToCourse(courses[0], user, isAnnouncement = true)

        goToHomeroomTab(data)

        homeroomPage.assertPageObjects()

        homeroomPage.openCourseAnnouncement(courseAnnouncement.title!!)

        discussionDetailsPage.assertToolbarDiscussionTitle(courseAnnouncement.title!!)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testShowCourseCardWithAnnouncement() {
        val data = createMockDataWithHomeroomCourse(courseCount = 3)
        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val user = data.users.values.first()

        data.addDiscussionTopicToCourse(homeroomCourse, user, isAnnouncement = true)

        val courses = data.courses.values.filter { !it.homeroomCourse }
        val courseAnnouncement = data.addDiscussionTopicToCourse(courses[0], user, isAnnouncement = true)

        goToHomeroomTab(data)

        homeroomPage.assertPageObjects()

        homeroomPage.assertCourseDisplayed(courses[0].name, homeroomPage.getStringFromResource(R.string.nothingDueToday), courseAnnouncement.title!!)
        homeroomPage.assertCourseDisplayed(courses[1].name, homeroomPage.getStringFromResource(R.string.nothingDueToday), "")
        homeroomPage.assertCourseDisplayed(courses[2].name, homeroomPage.getStringFromResource(R.string.nothingDueToday), "")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testDueTodayAndMissingAssignments() {
        val data = createMockDataWithHomeroomCourse(courseCount = 1)
        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val user = data.users.values.first()

        data.addDiscussionTopicToCourse(homeroomCourse, user, isAnnouncement = true)

        val courses = data.courses.values.filter { !it.homeroomCourse }

        data.addAssignment(courses[0].id, submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY))
        data.addAssignment(courses[0].id, submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY))

        goToHomeroomTab(data)

        homeroomPage.assertPageObjects()

        // With the current implementation of MockCanvas, all the assignments will show up as due today and missing, because both Mock endpoints will return all the assignments.
        // This cannot happen in normal circumstances, but for testing the UI it's fine.
        // We can add a more sophisticated approach when other tests will need it.
        // Verifying the logic that one can be due today or missing only is covered by unit tests.
        homeroomPage.assertToDoText("2 due today | 2 missing")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testOpenAssignments() {
        val data = createMockDataWithHomeroomCourse(courseCount = 1)
        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val user = data.users.values.first()

        data.addDiscussionTopicToCourse(homeroomCourse, user, isAnnouncement = true)

        val courses = data.courses.values.filter { !it.homeroomCourse }

        val assignment1 = data.addAssignment(courses[0].id, submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY))
        data.addAssignment(courses[0].id, submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY))

        goToHomeroomTab(data)

        homeroomPage.assertPageObjects()
        homeroomPage.openAssignments("2 due today | 2 missing")

        assignmentListPage.assertHasAssignment(assignment1)
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testEmptyState() {
        val data = createMockDataWithHomeroomCourse()

        goToHomeroomTab(data)

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

        val data = MockCanvas.init(
            studentCount = 1,
            courseCount = courseCount,
            pastCourseCount = pastCourseCount,
            favoriteCourseCount = favoriteCourseCount,
            accountNotificationCount = announcementCount,
            homeroomCourseCount = homeroomCourseCount)

        data.elementarySubjectPages = true

        return data
    }

    private fun goToHomeroomTab(data: MockCanvas) {
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLoginElementary(data.domain, token, student)
        elementaryDashboardPage.waitForRender()
        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.HOMEROOM)
    }
}