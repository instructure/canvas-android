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
import com.instructure.canvasapi2.di.JourneyApolloClient
import com.instructure.canvasapi2.enqueueQuery
import com.instructure.journey.GetTimeSpentWidgetDataQuery
import com.instructure.journey.type.TimeSpanInput
import com.instructure.journey.type.TimeSpanType
import java.util.Date
import javax.inject.Inject

data class TimeSpentWidgetData(
    val lastModifiedDate: Date?,
    val data: List<Any>
)

interface GetWidgetsManager {
    suspend fun getTimeSpentWidgetData(forceNetwork: Boolean): TimeSpentWidgetData
}

class GetWidgetsManagerImpl @Inject constructor(
    @JourneyApolloClient private val journeyClient: ApolloClient
) : GetWidgetsManager {
    override suspend fun getTimeSpentWidgetData(forceNetwork: Boolean): TimeSpentWidgetData {
        val widgetType = "time_spent_overview"
        val timeSpanInput = TimeSpanInput(type = TimeSpanType.PAST_30_DAYS)
        val dataScope = "learner"

        val query = GetTimeSpentWidgetDataQuery(
            widgetId = "widgetId",
            timeSpan = timeSpanInput,
            dataScope = dataScope,
        )

        val result = journeyClient.enqueueQuery(query, forceNetwork)
        val widgetData = result.dataAssertNoErrors.widgetDataWithLastModifiedDate

        return TimeSpentWidgetData(
            lastModifiedDate = widgetData.lastModifiedDate,
            data = widgetData.data
        )
    }
}