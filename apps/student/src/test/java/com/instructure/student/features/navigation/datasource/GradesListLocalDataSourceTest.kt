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

package com.instructure.student.features.navigation.datasource

import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.room.offline.facade.CourseFacade
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationLocalDataSourceTest {

    private val courseFacade: CourseFacade = mockk(relaxed = true)

    private val dataSource = NavigationLocalDataSource(courseFacade)

    @Test
    fun `Get course successfully returns api model`() = runTest {
        val expected = Course(1L)

        coEvery { courseFacade.getCourseById(any()) } returns expected

        val result = dataSource.getCourse(1, true)

        assertEquals(expected, result)
    }
}
