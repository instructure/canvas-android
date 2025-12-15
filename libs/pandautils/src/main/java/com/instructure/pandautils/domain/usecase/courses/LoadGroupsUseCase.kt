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

package com.instructure.pandautils.domain.usecase.courses

import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.data.repository.group.GroupRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

data class LoadGroupsParams(
    val forceRefresh: Boolean = false
)

class LoadGroupsUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) : BaseUseCase<LoadGroupsParams, List<Group>>() {

    override suspend fun execute(params: LoadGroupsParams): List<Group> {
        val groups = groupRepository.getGroups(params.forceRefresh).dataOrThrow

        return groups.filter { it.isFavorite }
    }
}