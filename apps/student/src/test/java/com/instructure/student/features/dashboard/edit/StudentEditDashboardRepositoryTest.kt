/*
 * Copyright (C) 2022 - present Instructure, Inc.
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

package com.instructure.student.features.dashboard.edit

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.room.offline.daos.CourseDao
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.entities.CourseEntity
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.dashboard.edit.datasource.StudentEditDashboardLocalDataSource
import com.instructure.student.features.dashboard.edit.datasource.StudentEditDashboardNetworkDataSource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StudentEditDashboardRepositoryTest {

    private val networkDataSource: StudentEditDashboardNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: StudentEditDashboardLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val courseSyncSettingsDao: CourseSyncSettingsDao = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val courseDao: CourseDao = mockk(relaxed = true)

    private val repository = StudentEditDashboardRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider, courseSyncSettingsDao, courseDao)

    @Before
    fun setup() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
    }

    @Test
    fun `Returns isOpenable true when course is not deleted and is published`() {
        // Given
        val course = Course(id = 1L, name = "Course", workflowState = Course.WorkflowState.AVAILABLE)

        // When
        val result = repository.isOpenable(course)

        // Then
        assertTrue(result)
    }

    @Test
    fun `Returns isOpenable false when course is deleted`() {
        // Given
        val course = Course(id = 1L, name = "Course", workflowState = Course.WorkflowState.DELETED)

        // When
        val result = repository.isOpenable(course)

        // Then
        assertFalse(result)
    }

    @Test
    fun `Returns isOpenable false when course is not published`() {
        // Given
        val course = Course(id = 1L, name = "Course", workflowState = Course.WorkflowState.UNPUBLISHED)

        // When
        val result = repository.isOpenable(course)

        // Then
        assertFalse(result)
    }

    @Test
    fun `Returns isFavoriteable true when course is validTerm and not deleted and published and has active enrollment`() {
        // Given
        val course = Course(
            id = 1L, name = "Course",
            workflowState = Course.WorkflowState.AVAILABLE,
            enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))
        )

        // When
        val result = repository.isFavoriteable(course)

        // Then
        assertTrue(result)
    }

    @Test
    fun `Returns isFavoriteable false when course is deleted`() {
        // Given
        val course = Course(
            id = 1L, name = "Course",
            workflowState = Course.WorkflowState.DELETED,
            enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))
        )

        // When
        val result = repository.isFavoriteable(course)

        // Then
        assertFalse(result)
    }

    @Test
    fun `Returns isFavoriteable false when course is not published`() {
        // Given
        val course = Course(
            id = 1L, name = "Course",
            workflowState = Course.WorkflowState.UNPUBLISHED,
            enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))
        )

        // When
        val result = repository.isFavoriteable(course)

        // Then
        assertFalse(result)
    }

    @Test
    fun `Returns isFavoriteable false when course doesn't have active enrollment`() {
        // Given
        val course = Course(
            id = 1L, name = "Course",
            workflowState = Course.WorkflowState.AVAILABLE,
            enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_DELETED))
        )

        // When
        val result = repository.isFavoriteable(course)

        // Then
        assertFalse(result)
    }

    @Test
    fun `Correctly filtered course ids are returned from getSyncedCourseIds`() = runTest {
        val entities = listOf(
            CourseSyncSettingsEntity(1, "Course 1",true),
            CourseSyncSettingsEntity(2, "Course 2",false),
            CourseSyncSettingsEntity(3, "Course 3",false, fullFileSync = true),
            CourseSyncSettingsEntity(4, "Course 4",false, CourseSyncSettingsEntity.TABS.associateWith { it == Tab.ANNOUNCEMENTS_ID }),
            CourseSyncSettingsEntity(5, "Course 5",false, CourseSyncSettingsEntity.TABS.associateWith { it == Tab.DISCUSSIONS_ID }),
            CourseSyncSettingsEntity(6, "Course 6",false, CourseSyncSettingsEntity.TABS.associateWith { it == Tab.PAGES_ID }),
        )
        coEvery { courseSyncSettingsDao.findAll() } returns entities
        coEvery { courseDao.findByIds(any()) } returns entities.map { CourseEntity(Course(it.courseId)) }.filter { it.id != 2L }

        val result = repository.getSyncedCourseIds()
        val expectedIds = setOf(1L, 3L, 4L, 5L, 6L)

        assertEquals(expectedIds, result)
    }

    @Test
    fun `Do not return course ids that are not synced yet`() = runTest {
        val entities = listOf(
            CourseSyncSettingsEntity(1, "Course 1",true),
            CourseSyncSettingsEntity(2, "Course 2",false),
            CourseSyncSettingsEntity(3, "Course 3",false, fullFileSync = true),
            CourseSyncSettingsEntity(4, "Course 4",false, CourseSyncSettingsEntity.TABS.associateWith { it == Tab.ANNOUNCEMENTS_ID }),
            CourseSyncSettingsEntity(5, "Course 5",false, CourseSyncSettingsEntity.TABS.associateWith { it == Tab.DISCUSSIONS_ID }),
            CourseSyncSettingsEntity(6, "Course 6",false, CourseSyncSettingsEntity.TABS.associateWith { it == Tab.PAGES_ID }),
        )
        coEvery { courseSyncSettingsDao.findAll() } returns entities
        coEvery { courseDao.findByIds(any()) } returns listOf(CourseEntity(Course(1)), CourseEntity(Course(3)))

        val result = repository.getSyncedCourseIds()
        val expectedIds = setOf(1L, 3L)

        assertEquals(expectedIds, result)
    }

    @Test
    fun `Get courses from network if device is online`() = runTest {
        val onlineCourses = listOf(listOf(Course(1), Course(2)), emptyList(), emptyList())
        val offlineCourses = listOf(listOf(Course(3), Course(4)), emptyList(), emptyList())

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getCourses() } returns onlineCourses
        coEvery { localDataSource.getCourses() } returns offlineCourses

        val courses = repository.getCourses()

        Assert.assertEquals(onlineCourses, courses)
    }

    @Test
    fun `Get courses from local database if device is offline`() = runTest {
        val onlineCourses = listOf(listOf(Course(1), Course(2)), emptyList(), emptyList())
        val offlineCourses = listOf(listOf(Course(3), Course(4)), emptyList(), emptyList())

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getCourses() } returns onlineCourses
        coEvery { localDataSource.getCourses() } returns offlineCourses

        val courses = repository.getCourses()

        Assert.assertEquals(offlineCourses, courses)
    }

    @Test
    fun `Get groups from network if device is online`() = runTest {
        val onlineGroups = listOf(Group(1), Group(2))
        val offlineGroups = listOf(Group(3), Group(4))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getGroups() } returns onlineGroups
        coEvery { localDataSource.getGroups() } returns offlineGroups

        val groups = repository.getGroups()

        Assert.assertEquals(onlineGroups, groups)
    }

    @Test
    fun `Get groups from local database if device is offline`() = runTest {
        val onlineGroups = listOf(Group(1), Group(2))
        val offlineGroups = listOf(Group(3), Group(4))

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getGroups() } returns onlineGroups
        coEvery { localDataSource.getGroups() } returns offlineGroups

        val groups = repository.getGroups()

        Assert.assertEquals(offlineGroups, groups)
    }
}