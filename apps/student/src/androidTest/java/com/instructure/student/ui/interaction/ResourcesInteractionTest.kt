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

import com.instructure.canvas.espresso.mockCanvas.*
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigPrefs
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLoginElementary
import com.instructure.student.util.FeatureFlagPrefs
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ResourcesInteractionTest : StudentTest() {

    override fun displaysPageObjects() = Unit

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testImportantLinksAndActionItemsShowUpInResourcesScreen() {
        val data = createMockDataWithHomeroomCourse(courseCount = 2)

        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val courseWithSyllabus = homeroomCourse.copy(syllabusBody = "Important links content")
        data.courses[homeroomCourse.id] = courseWithSyllabus

        val nonHomeroomCourses = data.courses.values.filter { !it.homeroomCourse }
        nonHomeroomCourses.forEach {
            data.addLTITool("Google Drive", "http://google.com", it, 1234L)
            data.addLTITool("Media Gallery", "http://instructure.com", it, 12345L)
        }

        goToResources(data)

        resourcesPage.assertPageObjects()
        resourcesPage.assertImportantLinksDisplayed(courseWithSyllabus.syllabusBody!!)

        resourcesPage.assertStudentApplicationsHeaderDisplayed()
        resourcesPage.assertLtiToolDisplayed("Google Drive")
        resourcesPage.assertLtiToolDisplayed("Media Gallery")

        val teacher = data.teachers[0]
        resourcesPage.assertStaffInfoHeaderDisplayed()
        resourcesPage.assertStaffDisplayed(teacher.shortName!!)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testImportantLinksForTwoCourses() {
        val data = createMockDataWithHomeroomCourse(courseCount = 2)

        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val courseWithSyllabus = homeroomCourse.copy(syllabusBody = "Important links content")
        data.courses[homeroomCourse.id] = courseWithSyllabus

        val homeroomCourse2 = data.addCourseWithEnrollment(data.students[0], Enrollment.EnrollmentType.Student, isHomeroom = true)
        data.addEnrollment(data.teachers[0], homeroomCourse, Enrollment.EnrollmentType.Teacher)

        val courseWithSyllabus2 = homeroomCourse2.copy(syllabusBody = "Important links 2")
        data.courses[homeroomCourse2.id] = courseWithSyllabus2

        goToResources(data)

        resourcesPage.assertPageObjects()

        // We only assert the course names, because can't differentiate between the two WebViews.
        resourcesPage.assertCourseNameDisplayed(courseWithSyllabus.name)
        resourcesPage.assertCourseNameDisplayed(courseWithSyllabus2.name)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testOnlyActionItemsShowIfSyllabusIsEmpty() {
        val data = createMockDataWithHomeroomCourse(courseCount = 2)

        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val courseWithSyllabus = homeroomCourse.copy(syllabusBody = "")
        data.courses[homeroomCourse.id] = courseWithSyllabus

        val nonHomeroomCourses = data.courses.values.filter { !it.homeroomCourse }
        nonHomeroomCourses.forEach {
            data.addLTITool("Google Drive", "http://google.com", it, 1234L)
            data.addLTITool("Media Gallery", "http://instructure.com", it, 12345L)
        }

        goToResources(data)

        resourcesPage.assertImportantLinksNotDisplayed()

        resourcesPage.assertStudentApplicationsHeaderDisplayed()
        resourcesPage.assertLtiToolDisplayed("Google Drive")
        resourcesPage.assertLtiToolDisplayed("Media Gallery")

        val teacher = data.teachers[0]
        resourcesPage.assertStaffInfoHeaderDisplayed()
        resourcesPage.assertStaffDisplayed(teacher.shortName!!)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testOnlyLtiToolsShowIfNoHomeroomCourse() {
        val data = createMockDataWithHomeroomCourse(courseCount = 2, homeroomCourseCount = 0)

        val nonHomeroomCourses = data.courses.values.filter { !it.homeroomCourse }
        nonHomeroomCourses.forEach {
            data.addLTITool("Google Drive", "http://google.com", it, 1234L)
            data.addLTITool("Media Gallery", "http://instructure.com", it, 12345L)
        }

        goToResources(data)

        resourcesPage.assertImportantLinksNotDisplayed()

        resourcesPage.assertStudentApplicationsHeaderDisplayed()
        resourcesPage.assertLtiToolDisplayed("Google Drive")
        resourcesPage.assertLtiToolDisplayed("Media Gallery")

        resourcesPage.assertStaffInfoNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.P2, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testEmptyState() {
        val data = createMockDataWithHomeroomCourse(courseCount = 2, homeroomCourseCount = 0)

        goToResources(data)

        resourcesPage.assertImportantLinksNotDisplayed()
        resourcesPage.assertStudentApplicationsNotDisplayed()
        resourcesPage.assertStaffInfoNotDisplayed()
        resourcesPage.assertEmptyViewDisplayed()
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testRefresh() {
        val data = createMockDataWithHomeroomCourse(courseCount = 2, homeroomCourseCount = 0)

        goToResources(data)

        resourcesPage.assertEmptyViewDisplayed()

        val homeroomCourse = data.addCourseWithEnrollment(data.students[0], Enrollment.EnrollmentType.Student, isHomeroom = true)
        data.addEnrollment(data.teachers[0], homeroomCourse, Enrollment.EnrollmentType.Teacher)

        val courseWithSyllabus = homeroomCourse.copy(syllabusBody = "Important links content")
        data.courses[homeroomCourse.id] = courseWithSyllabus

        val nonHomeroomCourses = data.courses.values.filter { !it.homeroomCourse }
        nonHomeroomCourses.forEach {
            data.addLTITool("Google Drive", "http://google.com", it, 1234L)
            data.addLTITool("Media Gallery", "http://instructure.com", it, 12345L)
        }

        resourcesPage.refresh()

        resourcesPage.assertPageObjects()
        resourcesPage.assertImportantLinksDisplayed(courseWithSyllabus.syllabusBody!!)

        resourcesPage.assertStudentApplicationsHeaderDisplayed()
        resourcesPage.assertLtiToolDisplayed("Google Drive")
        resourcesPage.assertLtiToolDisplayed("Media Gallery")

        val teacher = data.teachers[0]
        resourcesPage.assertStaffInfoHeaderDisplayed()
        resourcesPage.assertStaffDisplayed(teacher.shortName!!)
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testOpenLtiToolShowsCourseSelector() {
        val data = createMockDataWithHomeroomCourse(courseCount = 2)

        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val courseWithSyllabus = homeroomCourse.copy(syllabusBody = "Important links content")
        data.courses[homeroomCourse.id] = courseWithSyllabus

        val nonHomeroomCourses = data.courses.values.filter { !it.homeroomCourse }
        nonHomeroomCourses.forEach {
            data.addLTITool("Google Drive", "http://google.com", it, 1234L)
            data.addLTITool("Media Gallery", "http://instructure.com", it, 12345L)
        }

        goToResources(data)

        resourcesPage.openLtiApp("Google Drive")
        nonHomeroomCourses.forEach {
            resourcesPage.assertCourseShown(it.name)
        }
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testOpenComposeMessageScreen() {
        val data = createMockDataWithHomeroomCourse(courseCount = 2)

        val homeroomCourse = data.courses.values.first { it.homeroomCourse }
        val courseWithSyllabus = homeroomCourse.copy(syllabusBody = "Important links content")
        data.courses[homeroomCourse.id] = courseWithSyllabus

        val nonHomeroomCourses = data.courses.values.filter { !it.homeroomCourse }
        nonHomeroomCourses.forEach {
            data.addLTITool("Google Drive", "http://google.com", it, 1234L)
            data.addLTITool("Media Gallery", "http://instructure.com", it, 12345L)
        }

        goToResources(data)
        resourcesPage.openComposeMessage(data.teachers[0].shortName!!)

        newMessagePage.assertToolbarTitleNewMessage()
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
        FeatureFlagPrefs.showInProgressK5Tabs = true

        return MockCanvas.init(
            studentCount = 1,
            teacherCount = 1,
            courseCount = courseCount,
            pastCourseCount = pastCourseCount,
            favoriteCourseCount = favoriteCourseCount,
            accountNotificationCount = announcementCount,
            homeroomCourseCount = homeroomCourseCount)
    }

    private fun goToResources(data: MockCanvas) {
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLoginElementary(data.domain, token, student)
        elementaryDashboardPage.waitForRender()
        elementaryDashboardPage.selectResourcesTab()
    }
}