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

import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.data.repository.group.GroupRepository
import com.instructure.pandautils.domain.models.courses.GroupCardItem
import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.utils.ColorKeeper
import javax.inject.Inject

data class LoadGroupsParams(
    val forceRefresh: Boolean = false
)

class LoadGroupsUseCase @Inject constructor(
    private val groupRepository: GroupRepository,
    private val courseRepository: CourseRepository
) : BaseUseCase<LoadGroupsParams, List<GroupCardItem>>() {

    override suspend fun execute(params: LoadGroupsParams): List<GroupCardItem> {
        val groups = groupRepository.getGroups(params.forceRefresh).dataOrThrow

        return groups
            .filter { it.isFavorite }
            .map { group ->
                val parentCourse = if (group.courseId != 0L) {
                    courseRepository.getCourse(group.courseId, params.forceRefresh).dataOrNull
                } else {
                    null
                }

                val themedColor = parentCourse?.let { ColorKeeper.getOrGenerateColor(it) }
                    ?: ColorKeeper.getOrGenerateColor(group)

                GroupCardItem(
                    id = group.id,
                    name = group.name.orEmpty(),
                    parentCourseName = parentCourse?.name,
                    parentCourseId = group.courseId,
                    color = themedColor.light,
                    memberCount = group.membersCount
                )
            }
    }
}