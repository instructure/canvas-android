package com.instructure.pandautils.features.inbox.list.filter/*
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

import android.content.res.Resources
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.R
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import com.instructure.testutils.ViewModelTestRule
import org.junit.Test

class ContextFilterViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val resources = mockk<Resources>(relaxed = true)

    private val viewModel = ContextFilterViewModel(resources)

    @Before
    fun setUp() {
        every { resources.getString(R.string.courses) } returns "Courses"
        every { resources.getString(R.string.groups) } returns "Groups"
    }

    @Test
    fun `Create list with both courses and groups`() {
        val canvasContext = listOf<CanvasContext>(Course(id = 1, name = "Course"), Group(id = 2, name = "Group"))
        viewModel.setFilterItems(canvasContext)

        val listItems = viewModel.itemViewModels.value ?: emptyList()
        assertEquals(4, listItems.size)
        assertEquals("Courses", (listItems[0] as ContextFilterHeaderItemViewModel).title)
        assertEquals("Course", (listItems[1] as ContextFilterItemViewModel).title)
        assertEquals("Groups", (listItems[2] as ContextFilterHeaderItemViewModel).title)
        assertEquals("Group", (listItems[3] as ContextFilterItemViewModel).title)
    }

    @Test
    fun `Create list with only courses when there is no group`() {
        val canvasContext = listOf<CanvasContext>(Course(id = 1, name = "Course"))
        viewModel.setFilterItems(canvasContext)

        val listItems = viewModel.itemViewModels.value ?: emptyList()
        assertEquals(2, listItems.size)
        assertEquals("Courses", (listItems[0] as ContextFilterHeaderItemViewModel).title)
        assertEquals("Course", (listItems[1] as ContextFilterItemViewModel).title)
    }

    @Test
    fun `Create list with only groups when there is no course`() {
        val canvasContext = listOf<CanvasContext>(Group(id = 1, name = "Group"))
        viewModel.setFilterItems(canvasContext)

        val listItems = viewModel.itemViewModels.value ?: emptyList()
        assertEquals(2, listItems.size)
        assertEquals("Groups", (listItems[0] as ContextFilterHeaderItemViewModel).title)
        assertEquals("Group", (listItems[1] as ContextFilterItemViewModel).title)
    }

    @Test
    fun `Clicking items sends event with id`() {
        val canvasContext = listOf<CanvasContext>(Course(id = 1L, name = "Course"))
        viewModel.setFilterItems(canvasContext)

        val listItems = viewModel.itemViewModels.value ?: emptyList()
        (listItems[1] as ContextFilterItemViewModel).onClicked()

        assertEquals(1L, viewModel.events.value!!.peekContent())
    }
}
