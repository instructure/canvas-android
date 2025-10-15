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
import com.instructure.journey.GetWidgetDataQuery
import com.instructure.journey.type.TimeSpanInput
import com.instructure.journey.type.TimeSpanType
import com.instructure.journey.type.WidgetDataFiltersInput
import javax.inject.Inject

interface GetWidgetsManager {
    suspend fun getTimeSpentWidgetData(courseId: Long?, forceNetwork: Boolean): GetWidgetDataQuery.WidgetData
}

class GetWidgetsManagerImpl @Inject constructor(
    private val journeyClient: ApolloClient,
) : GetWidgetsManager {
    override suspend fun getTimeSpentWidgetData(
        courseId: Long?,
        forceNetwork: Boolean,
    ): GetWidgetDataQuery.WidgetData {
        val widgetType = "time_spent_details"
        val timeSpanInput = TimeSpanInput(type = TimeSpanType.PAST_7_DAYS)
        val dataScope = "learner"
        val queryParams = if (courseId != null) {
            Optional.present(
                WidgetDataFiltersInput(courseId = Optional.present(courseId.toDouble()))
            )
        } else {
            Optional.absent()
        }

        val query = GetWidgetDataQuery(
            widgetType = widgetType,
            timeSpan = timeSpanInput,
            dataScope = dataScope,
            queryParams = queryParams
        )

        val result = journeyClient.enqueueQuery(query, forceNetwork)

        return result.dataAssertNoErrors.widgetData
    }
}