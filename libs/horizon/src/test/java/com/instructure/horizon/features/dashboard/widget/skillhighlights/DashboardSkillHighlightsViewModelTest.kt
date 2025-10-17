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
package com.instructure.horizon.features.dashboard.widget.skillhighlights

import com.instructure.canvasapi2.managers.graphql.horizon.journey.Skill
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.skillhighlights.card.SkillHighlightProficiencyLevel
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
class DashboardSkillHighlightsViewModelTest {
    private val repository: DashboardSkillHighlightsRepository = mockk(relaxed = true)
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
            Skill("1", "Advanced Skill", "advanced", Date(), Date()),
            Skill("2", "Beginner Skill", "beginner", Date(), Date()),
            Skill("3", "Proficient Skill", "proficient", Date(), Date())
        )
        coEvery { repository.getSkills(null, false) } returns skills

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.SUCCESS, state.state)
        assertEquals(3, state.cardState.skills.size)
        coVerify { repository.getSkills(null, false) }
    }

    @Test
    fun `Test skills are sorted by proficiency level then alphabetically`() = runTest {
        val skills = listOf(
            Skill("1", "Zebra Skill", "beginner", Date(), Date()),
            Skill("2", "Apple Skill", "advanced", Date(), Date()),
            Skill("3", "Banana Skill", "expert", Date(), Date()),
            Skill("4", "Cherry Skill", "advanced", Date(), Date())
        )
        coEvery { repository.getSkills(null, false) } returns skills

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.cardState.skills.size)
        // Should be: Banana (expert), Apple (advanced), Cherry (advanced)
        assertEquals("Banana Skill", state.cardState.skills[0].name)
        assertEquals(SkillHighlightProficiencyLevel.EXPERT, state.cardState.skills[0].proficiencyLevel)
        assertEquals("Apple Skill", state.cardState.skills[1].name)
        assertEquals(SkillHighlightProficiencyLevel.ADVANCED, state.cardState.skills[1].proficiencyLevel)
        assertEquals("Cherry Skill", state.cardState.skills[2].name)
        assertEquals(SkillHighlightProficiencyLevel.ADVANCED, state.cardState.skills[2].proficiencyLevel)
    }

    @Test
    fun `Test only top 3 skills are displayed`() = runTest {
        val skills = listOf(
            Skill("1", "Skill 1", "expert", Date(), Date()),
            Skill("2", "Skill 2", "expert", Date(), Date()),
            Skill("3", "Skill 3", "advanced", Date(), Date()),
            Skill("4", "Skill 4", "advanced", Date(), Date()),
            Skill("5", "Skill 5", "beginner", Date(), Date())
        )
        coEvery { repository.getSkills(null, false) } returns skills

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.cardState.skills.size)
    }

    @Test
    fun `Test no data state when fewer than 3 skills`() = runTest {
        val skills = listOf(
            Skill("1", "Skill 1", "expert", Date(), Date()),
            Skill("2", "Skill 2", "advanced", Date(), Date())
        )
        coEvery { repository.getSkills(null, false) } returns skills

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.SUCCESS, state.state)
        assertTrue(state.cardState.skills.isEmpty())
    }

    @Test
    fun `Test no data state when no skills`() = runTest {
        coEvery { repository.getSkills(null, false) } returns emptyList()

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.SUCCESS, state.state)
        assertTrue(state.cardState.skills.isEmpty())
    }

    @Test
    fun `Test error state when repository throws exception`() = runTest {
        coEvery { repository.getSkills(null, false) } throws Exception("Network error")

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.ERROR, state.state)
    }

    @Test
    fun `Test refresh calls repository with forceNetwork true`() = runTest {
        val skills = listOf(
            Skill("1", "Skill 1", "advanced", Date(), Date()),
            Skill("2", "Skill 2", "proficient", Date(), Date()),
            Skill("3", "Skill 3", "beginner", Date(), Date())
        )
        coEvery { repository.getSkills(null, false) } returns skills
        coEvery { repository.getSkills(null, true) } returns skills

        val viewModel = getViewModel()
        advanceUntilIdle()

        var completed = false
        viewModel.uiState.value.onRefresh { completed = true }
        advanceUntilIdle()

        assertTrue(completed)
        coVerify { repository.getSkills(null, true) }
    }

    @Test
    fun `Test refresh updates state to loading then success`() = runTest {
        val skills = listOf(
            Skill("1", "Skill 1", "expert", Date(), Date()),
            Skill("2", "Skill 2", "advanced", Date(), Date()),
            Skill("3", "Skill 3", "proficient", Date(), Date())
        )
        coEvery { repository.getSkills(null, any()) } returns skills

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
            Skill("1", "Skill 1", "expert", Date(), Date()),
            Skill("2", "Skill 2", "advanced", Date(), Date()),
            Skill("3", "Skill 3", "proficient", Date(), Date())
        )
        coEvery { repository.getSkills(null, false) } returns skills
        coEvery { repository.getSkills(null, true) } throws Exception("Refresh failed")

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
    fun `Test null proficiency level defaults to beginner`() = runTest {
        val skills = listOf(
            Skill("1", "Skill Without Level", null, Date(), Date()),
            Skill("2", "Advanced Skill", "advanced", Date(), Date()),
            Skill("3", "Proficient Skill", "proficient", Date(), Date())
        )
        coEvery { repository.getSkills(null, false) } returns skills

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val skillWithoutLevel = state.cardState.skills.find { it.name == "Skill Without Level" }
        assertEquals(SkillHighlightProficiencyLevel.BEGINNER, skillWithoutLevel?.proficiencyLevel)
    }

    @Test
    fun `Test unknown proficiency level defaults to beginner`() = runTest {
        val skills = listOf(
            Skill("1", "Unknown Level Skill", "unknown", Date(), Date()),
            Skill("2", "Advanced Skill", "advanced", Date(), Date()),
            Skill("3", "Proficient Skill", "proficient", Date(), Date())
        )
        coEvery { repository.getSkills(null, false) } returns skills

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val unknownSkill = state.cardState.skills.find { it.name == "Unknown Level Skill" }
        assertEquals(SkillHighlightProficiencyLevel.BEGINNER, unknownSkill?.proficiencyLevel)
    }

    @Test
    fun `Test proficiency level parsing is case insensitive`() = runTest {
        val skills = listOf(
            Skill("1", "Skill 1", "EXPERT", Date(), Date()),
            Skill("2", "Skill 2", "Advanced", Date(), Date()),
            Skill("3", "Skill 3", "ProFicIent", Date(), Date())
        )
        coEvery { repository.getSkills(null, false) } returns skills

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(SkillHighlightProficiencyLevel.EXPERT, state.cardState.skills[0].proficiencyLevel)
        assertEquals(SkillHighlightProficiencyLevel.ADVANCED, state.cardState.skills[1].proficiencyLevel)
        assertEquals(SkillHighlightProficiencyLevel.PROFICIENT, state.cardState.skills[2].proficiencyLevel)
    }

    private fun getViewModel(): DashboardSkillHighlightsViewModel {
        return DashboardSkillHighlightsViewModel(repository)
    }
}
