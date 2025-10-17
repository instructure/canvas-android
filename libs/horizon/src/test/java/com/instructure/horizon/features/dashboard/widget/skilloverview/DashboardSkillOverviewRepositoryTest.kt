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

import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetSkillsManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Skill
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Date

class DashboardSkillOverviewRepositoryTest {
    private val getSkillsManager: GetSkillsManager = mockk(relaxed = true)

    @Test
    fun `Test successful skills retrieval with completedOnly true`() = runTest {
        val skills = listOf(
            Skill("1", "Completed Skill 1", "expert", Date(), Date()),
            Skill("2", "Completed Skill 2", "advanced", Date(), Date()),
            Skill("3", "Completed Skill 3", "proficient", Date(), Date())
        )
        coEvery { getSkillsManager.getSkills(true, false) } returns skills

        val result = getRepository().getSkills(completedOnly = true, forceNetwork = false)

        assertEquals(3, result.size)
        assertEquals(skills, result)
        coVerify { getSkillsManager.getSkills(true, false) }
    }

    @Test
    fun `Test skills retrieval with forceNetwork true`() = runTest {
        val skills = listOf(
            Skill("1", "Network Skill", "advanced", Date(), Date())
        )
        coEvery { getSkillsManager.getSkills(true, true) } returns skills

        val result = getRepository().getSkills(completedOnly = true, forceNetwork = true)

        assertEquals(1, result.size)
        coVerify { getSkillsManager.getSkills(true, true) }
    }

    @Test
    fun `Test empty skills list is returned correctly`() = runTest {
        coEvery { getSkillsManager.getSkills(true, false) } returns emptyList()

        val result = getRepository().getSkills(completedOnly = true, forceNetwork = false)

        assertEquals(0, result.size)
    }

    @Test
    fun `Test skills with null proficiency level`() = runTest {
        val skills = listOf(
            Skill("1", "Skill Without Level", null, Date(), Date())
        )
        coEvery { getSkillsManager.getSkills(true, false) } returns skills

        val result = getRepository().getSkills(completedOnly = true, forceNetwork = false)

        assertEquals(1, result.size)
        assertEquals(null, result[0].proficiencyLevel)
    }

    private fun getRepository(): DashboardSkillOverviewRepository {
        return DashboardSkillOverviewRepository(getSkillsManager)
    }
}
