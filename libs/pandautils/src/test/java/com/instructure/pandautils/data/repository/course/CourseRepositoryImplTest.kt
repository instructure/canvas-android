/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.instructure.pandautils.data.repository.course

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CourseRepositoryImplTest {

    private val localDataSource: CourseLocalDataSource = mockk(relaxed = true)
    private val networkDataSource: CourseNetworkDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val repository = CourseRepositoryImpl(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)

    @Before
    fun setup() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
    }

    @Test
    fun `getCourses uses network when online`() = runTest {
        val networkCourses = DataResult.Success(listOf(Course(id = 1), Course(id = 2)))
        val localCourses = DataResult.Success(listOf(Course(id = 3)))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getCourses(any()) } returns networkCourses
        coEvery { localDataSource.getCourses(any()) } returns localCourses

        val result = repository.getCourses(false)

        assertEquals(networkCourses, result)
        coVerify { networkDataSource.getCourses(false) }
    }

    @Test
    fun `getCourses uses local when offline`() = runTest {
        val networkCourses = DataResult.Success(listOf(Course(id = 1)))
        val localCourses = DataResult.Success(listOf(Course(id = 3)))

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getCourses(any()) } returns networkCourses
        coEvery { localDataSource.getCourses(any()) } returns localCourses

        val result = repository.getCourses(false)

        assertEquals(localCourses, result)
        coVerify { localDataSource.getCourses(false) }
    }

    @Test
    fun `getCourses uses network when offline but offline feature disabled`() = runTest {
        val networkCourses = DataResult.Success(listOf(Course(id = 1)))

        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns false
        coEvery { networkDataSource.getCourses(any()) } returns networkCourses

        val result = repository.getCourses(false)

        assertEquals(networkCourses, result)
        coVerify { networkDataSource.getCourses(false) }
    }

    @Test
    fun `getCourse uses network when online`() = runTest {
        val course = DataResult.Success(Course(id = 1))
        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getCourse(1, false) } returns course

        val result = repository.getCourse(1, false)

        assertEquals(course, result)
    }

    @Test
    fun `getCourse uses local when offline`() = runTest {
        val course = DataResult.Success(Course(id = 1))
        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getCourse(1, false) } returns course

        val result = repository.getCourse(1, false)

        assertEquals(course, result)
    }

    @Test
    fun `getDashboardCards uses network when online`() = runTest {
        val cards = DataResult.Success(listOf(DashboardCard(id = 1)))
        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getDashboardCards(any()) } returns cards

        val result = repository.getDashboardCards(false)

        assertEquals(cards, result)
    }

    @Test
    fun `getDashboardCards uses local when offline`() = runTest {
        val cards = DataResult.Success(listOf(DashboardCard(id = 1)))
        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getDashboardCards(any()) } returns cards

        val result = repository.getDashboardCards(false)

        assertEquals(cards, result)
    }

    @Test
    fun `getFavoriteCourses uses network when online`() = runTest {
        val courses = DataResult.Success(listOf(Course(id = 1, isFavorite = true)))
        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getFavoriteCourses(any()) } returns courses

        val result = repository.getFavoriteCourses(false)

        assertEquals(courses, result)
    }

    @Test
    fun `getFavoriteCourses uses local when offline`() = runTest {
        val courses = DataResult.Success(listOf(Course(id = 1, isFavorite = true)))
        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.getFavoriteCourses(any()) } returns courses

        val result = repository.getFavoriteCourses(false)

        assertEquals(courses, result)
    }
}