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

package com.instructure.pandautils.domain.usecase.conference

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.data.repository.conference.ConferenceRepository
import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.data.repository.group.GroupRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.features.dashboard.widget.conferences.ConferenceItem
import com.instructure.pandautils.models.ConferenceDashboardBlacklist
import java.util.Locale
import javax.inject.Inject

class LoadLiveConferencesUseCase @Inject constructor(
    private val conferenceRepository: ConferenceRepository,
    private val courseRepository: CourseRepository,
    private val groupRepository: GroupRepository,
    private val conferenceDashboardBlacklist: ConferenceDashboardBlacklist
) : BaseUseCase<LoadLiveConferencesUseCase.Params, List<ConferenceItem>>() {

    data class Params(
        val forceRefresh: Boolean = false
    )

    override suspend fun execute(params: Params): List<ConferenceItem> {
        val blacklist = conferenceDashboardBlacklist.conferenceDashboardBlacklist
        val conferences = conferenceRepository.getLiveConferences(params.forceRefresh).dataOrThrow
            .filter { !blacklist.contains(it.id.toString()) }

        if (conferences.isEmpty()) {
            return emptyList()
        }

        val coursesMap = courseRepository.getCourses(params.forceRefresh).dataOrThrow.associateBy { it.id }
        val groupsMap = groupRepository.getGroups(params.forceRefresh).dataOrThrow.associateBy { it.id }

        return conferences.map { conference ->
            val canvasContext = resolveCanvasContext(conference, coursesMap, groupsMap)
            ConferenceItem(
                id = conference.id,
                subtitle = canvasContext.name ?: conference.title.orEmpty(),
                joinUrl = conference.joinUrl,
                canvasContext = canvasContext
            )
        }
    }

    private fun resolveCanvasContext(
        conference: Conference,
        coursesMap: Map<Long, Course>,
        groupsMap: Map<Long, Group>
    ): CanvasContext {
        val contextType = conference.contextType.lowercase(Locale.US)
        val contextId = conference.contextId
        val genericContext = CanvasContext.fromContextCode("${contextType}_${contextId}")
            ?: CanvasContext.defaultCanvasContext()

        return when (genericContext) {
            is Course -> coursesMap[contextId] ?: genericContext
            is Group -> groupsMap[contextId] ?: genericContext
            else -> genericContext
        }
    }
}