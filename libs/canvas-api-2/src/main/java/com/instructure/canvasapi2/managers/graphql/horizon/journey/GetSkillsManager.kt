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
package com.instructure.canvasapi2.managers.graphql.horizon.journey

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.enqueueQuery
import com.instructure.journey.GetSkillsQuery
import java.util.Date
import javax.inject.Inject

data class Skill(
    val id: String,
    val name: String,
    val proficiencyLevel: String?,
    val createdAt: Date?,
    val updatedAt: Date?
)

interface GetSkillsManager {
    suspend fun getSkills(completedOnly: Boolean?, forceNetwork: Boolean): List<Skill>
}

class GetSkillsManagerImpl @Inject constructor(
    private val journeyClient: ApolloClient
) : GetSkillsManager {
    override suspend fun getSkills(
        completedOnly: Boolean?,
        forceNetwork: Boolean
    ): List<Skill> {
        val query = GetSkillsQuery(
            completedOnly = Optional.presentIfNotNull(completedOnly)
        )

        val result = journeyClient.enqueueQuery(query, forceNetwork)
        val skills = result.dataAssertNoErrors.skills

        return skills.map { skill ->
            Skill(
                id = skill.id,
                name = skill.name,
                proficiencyLevel = skill.proficiencyLevel,
                createdAt = skill.createdAt,
                updatedAt = skill.updatedAt
            )
        }
    }
}
