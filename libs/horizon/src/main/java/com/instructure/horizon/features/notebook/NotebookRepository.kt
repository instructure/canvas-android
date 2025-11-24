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
package com.instructure.horizon.features.notebook

import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.RedwoodApiManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.redwood.QueryNotesQuery
import com.instructure.redwood.type.LearningObjectFilter
import com.instructure.redwood.type.NoteFilterInput
import com.instructure.redwood.type.OrderByInput
import com.instructure.redwood.type.OrderDirection
import javax.inject.Inject

class NotebookRepository @Inject constructor(
    private val redwoodApiManager: RedwoodApiManager,
    private val horizonGetCoursesManager: HorizonGetCoursesManager,
    private val apiPrefs: ApiPrefs,
) {
    suspend fun getNotes(
        after: String? = null,
        before: String? = null,
        itemCount: Int = 10,
        filterType: NotebookType? = null,
        courseId: Long? = null,
        objectTypeAndId: Pair<String, String>? = null,
        orderDirection: OrderDirection? = null,
        forceNetwork: Boolean = false
    ): QueryNotesQuery.Notes {
        val filterInput = NoteFilterInput(
            reactions = if (filterType != null) {
                Optional.present(
                    listOf(filterType.name)
                )
            } else {
                Optional.absent()
            },
            courseId = Optional.presentIfNotNull(courseId?.toString()),
            learningObject = if (objectTypeAndId != null) {
                Optional.present(
                    LearningObjectFilter(
                        type = objectTypeAndId.first,
                        id = objectTypeAndId.second
                    )
                )
            } else {
                Optional.absent()
            }
        )
        val orderByInput = OrderByInput(
            direction = Optional.presentIfNotNull(orderDirection)
        )

        return if (before != null) {
            redwoodApiManager.getNotes(
                lastN = itemCount,
                before = before,
                filter = filterInput,
                orderBy = orderByInput,
                forceNetwork = forceNetwork
            )
        } else {
            redwoodApiManager.getNotes(
                firstN = itemCount,
                after = after,
                filter = filterInput,
                orderBy = orderByInput,
                forceNetwork = forceNetwork
            )
        }

    }

    suspend fun getCourses(forceNetwork: Boolean = false): DataResult<List<CourseWithProgress>> {
        return horizonGetCoursesManager.getCoursesWithProgress(
            userId = apiPrefs.user?.id ?: 0L,
            forceNetwork = forceNetwork
        )
    }
}