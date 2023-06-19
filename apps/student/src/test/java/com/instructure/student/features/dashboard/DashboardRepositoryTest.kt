/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.student.features.dashboard

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@ExperimentalCoroutinesApi
class DashboardRepositoryTest {

    private val networkDataSource: DashboardNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: DashboardLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val courseSyncSettingsDao: CourseSyncSettingsDao = mockk(relaxed = true)

    private val repository = DashboardRepository(localDataSource, networkDataSource, networkStateProvider, courseApi, courseSyncSettingsDao)

    @Test
    fun `Get courses from network if device is online`() = runTest {
        val onlineCourses = listOf(Course(1), Course(2))
        val offlineCourses = listOf(Course(3), Course(4))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getCourses(any()) } returns onlineCourses
        coEvery { localDataSource.getCourses(any()) } returns offlineCourses

        val courses = repository.getCourses(true)

        Assert.assertEquals(onlineCourses, courses)
    }

    @Test
    fun `Get courses from local database if device is offline`() = runTest {
        val onlineCourses = listOf(Course(1), Course(2))
        val offlineCourses = listOf(Course(3), Course(4))

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getCourses(any()) } returns onlineCourses
        coEvery { localDataSource.getCourses(any()) } returns offlineCourses

        val courses = repository.getCourses(true)

        Assert.assertEquals(offlineCourses, courses)
    }

    @Test
    fun `Get groups from network if device is online`() = runTest {
        val onlineGroups = listOf(Group(1), Group(2))
        val offlineGroups = listOf(Group(3), Group(4))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getGroups(any()) } returns onlineGroups
        coEvery { localDataSource.getGroups(any()) } returns offlineGroups

        val groups = repository.getGroups(true)

        Assert.assertEquals(onlineGroups, groups)
    }

    @Test
    fun `Get groups from local database if device is offline`() = runTest {
        val onlineGroups = listOf(Group(1), Group(2))
        val offlineGroups = listOf(Group(3), Group(4))

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getGroups(any()) } returns onlineGroups
        coEvery { localDataSource.getGroups(any()) } returns offlineGroups

        val groups = repository.getGroups(true)

        Assert.assertEquals(offlineGroups, groups)
    }

    @Test
    fun `Returns list of Dashboard cards from local database if device is offline`() = runTest {
        val onlineCards = listOf(DashboardCard(1), DashboardCard(2))
        val offlineCards = listOf(DashboardCard(3), DashboardCard(4))

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getDashboardCards(any()) } returns onlineCards
        coEvery { localDataSource.getDashboardCards(any()) } returns offlineCards

        val result = repository.getDashboardCourses(true)

        Assert.assertEquals(offlineCards, result)
    }

    @Test
    fun `Returns list of Dashboard cards from network if device is online`() = runTest {
        val onlineCards = listOf(DashboardCard(1), DashboardCard(2))
        val offlineCards = listOf(DashboardCard(3), DashboardCard(4))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getDashboardCards(any()) } returns onlineCards
        coEvery { localDataSource.getDashboardCards(any()) } returns offlineCards

        val result = repository.getDashboardCourses(true)

        Assert.assertEquals(onlineCards, result)
    }

    @Test
    fun `Returned dashboard courses are saved to the local store`() = runTest {
        val onlineCards = listOf(DashboardCard(1), DashboardCard(2))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getDashboardCards(any()) } returns onlineCards

        repository.getDashboardCourses(true)

        coVerify { localDataSource.saveDashboardCards(onlineCards) }
    }

    @Test
    fun `Sort dashboard cards by position`() = runTest {
        val dashboardCards = listOf(
            DashboardCard(id = 1, position = 1),
            DashboardCard(id = 2, position = 0),
            DashboardCard(id = 3),
            DashboardCard(id = 4, position = 2)
        )

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getDashboardCards(any()) } returns dashboardCards

        val result = repository.getDashboardCourses(true)

        val expectedResult = listOf(
            DashboardCard(id = 2, position = 0),
            DashboardCard(id = 1, position = 1),
            DashboardCard(id = 4, position = 2),
            DashboardCard(id = 3)
        )

        Assert.assertEquals(expectedResult, result)
    }

    @Test
    fun `Correctly filtered course ids are returned from getSyncedCourseIds`() = runTest {
        val entities = listOf(
            CourseSyncSettingsEntity(1, true, false, false, false, false, false),
            CourseSyncSettingsEntity(2, false, true, false, false, false, false),
            CourseSyncSettingsEntity(3, false, false, true, false, false, false),
            CourseSyncSettingsEntity(4, false, false, false, true, false, false),
            CourseSyncSettingsEntity(5, false, false, false, false, false, false),
        )
        coEvery { courseSyncSettingsDao.findAll() } returns entities

        val result = repository.getSyncedCourseIds()
        val expectedIds = setOf(1L, 2L, 3L, 4L)

        Assert.assertEquals(expectedIds, result)
    }
}