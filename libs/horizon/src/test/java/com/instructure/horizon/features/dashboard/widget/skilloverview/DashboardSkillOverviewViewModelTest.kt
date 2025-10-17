/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.dashboard.widget.skilloverview

import com.instructure.canvasapi2.managers.graphql.horizon.journey.Skill
import com.instructure.horizon.features.dashboard.DashboardItemState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardSkillOverviewViewModelTest {
    private val repository: DashboardSkillOverviewRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test initialization loads skills data`() = runTest {
        val skills = listOf(
            Skill("1", "Completed Skill 1", "advanced", Date(), Date()),
            Skill("2", "Completed Skill 2", "beginner", Date(), Date()),
            Skill("3", "Completed Skill 3", "proficient", Date(), Date())
        )
        coEvery { repository.getSkills(true, false) } returns skills

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.SUCCESS, state.state)
        assertEquals(3, state.cardState.completedSkillCount)
        coVerify { repository.getSkills(true, false) }
    }

    @Test
    fun `Test completed skill count is correct`() = runTest {
        val skills = listOf(
            Skill("1", "Skill 1", "expert", Date(), Date()),
            Skill("2", "Skill 2", "advanced", Date(), Date()),
            Skill("3", "Skill 3", "proficient", Date(), Date()),
            Skill("4", "Skill 4", "beginner", Date(), Date()),
            Skill("5", "Skill 5", "advanced", Date(), Date())
        )
        coEvery { repository.getSkills(true, false) } returns skills

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(5, state.cardState.completedSkillCount)
    }

    @Test
    fun `Test zero completed skills`() = runTest {
        coEvery { repository.getSkills(true, false) } returns emptyList()

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.SUCCESS, state.state)
        assertEquals(0, state.cardState.completedSkillCount)
    }

    @Test
    fun `Test error state when repository throws exception`() = runTest {
        coEvery { repository.getSkills(true, false) } throws Exception("Network error")

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.ERROR, state.state)
    }

    @Test
    fun `Test refresh calls repository with forceNetwork true`() = runTest {
        val skills = listOf(
            Skill("1", "Skill 1", "advanced", Date(), Date()),
            Skill("2", "Skill 2", "proficient", Date(), Date())
        )
        coEvery { repository.getSkills(true, false) } returns skills
        coEvery { repository.getSkills(true, true) } returns skills

        val viewModel = getViewModel()
        advanceUntilIdle()

        var completed = false
        viewModel.uiState.value.onRefresh { completed = true }
        advanceUntilIdle()

        assertTrue(completed)
        coVerify { repository.getSkills(true, true) }
    }

    @Test
    fun `Test refresh updates state to loading then success`() = runTest {
        val skills = listOf(
            Skill("1", "Skill 1", "expert", Date(), Date()),
            Skill("2", "Skill 2", "advanced", Date(), Date())
        )
        coEvery { repository.getSkills(true, any()) } returns skills

        val viewModel = getViewModel()
        advanceUntilIdle()

        var completed = false
        viewModel.uiState.value.onRefresh { completed = true }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.SUCCESS, state.state)
        assertTrue(completed)
    }

    @Test
    fun `Test refresh with error sets error state`() = runTest {
        val skills = listOf(
            Skill("1", "Skill 1", "expert", Date(), Date())
        )
        coEvery { repository.getSkills(true, false) } returns skills
        coEvery { repository.getSkills(true, true) } throws Exception("Refresh failed")

        val viewModel = getViewModel()
        advanceUntilIdle()

        var completed = false
        viewModel.uiState.value.onRefresh { completed = true }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.ERROR, state.state)
        assertTrue(completed)
    }

    @Test
    fun `Test large number of completed skills`() = runTest {
        val skills = (1..100).map {
            Skill(it.toString(), "Skill $it", "advanced", Date(), Date())
        }
        coEvery { repository.getSkills(true, false) } returns skills

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(100, state.cardState.completedSkillCount)
    }

    private fun getViewModel(): DashboardSkillOverviewViewModel {
        return DashboardSkillOverviewViewModel(repository)
    }
}
