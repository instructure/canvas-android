package com.instructure.pandautils.features.inbox.list/*
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

import org.junit.Assert.*
import org.junit.Rule
import com.instructure.testutils.ViewModelTestRule
import org.junit.Test

class InboxSharedViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val viewModel = InboxSharedViewModel()

    @Test
    fun `Select context id sends correct event`() {
        viewModel.selectContextId(11)

        assertEquals(InboxFilterAction.FilterSelected(11), viewModel.events.value!!.peekContent())
    }

    @Test
    fun `Clear filter sends correct event`() {
        viewModel.clearFilter()

        assertEquals(InboxFilterAction.FilterCleared, viewModel.events.value!!.peekContent())
    }

    @Test
    fun `Filter dialog dismissed sends correct event`() {
        viewModel.filterDialogDismissed()

        assertEquals(InboxFilterAction.FilterDialogDismissed, viewModel.events.value!!.peekContent())
    }
}
