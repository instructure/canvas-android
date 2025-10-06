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
package com.instructure.horizon.features.dashboard.timespent

import com.instructure.canvasapi2.managers.graphql.JourneyApiManager
import com.instructure.canvasapi2.managers.graphql.TimeSpentWidgetData
import com.instructure.journey.type.TimeSpanType
import javax.inject.Inject

interface DashboardTimeSpentRepository {
    suspend fun getTimeSpentData(
        widgetId: String = "time-spent-widget",
        timeSpanType: TimeSpanType = TimeSpanType.PAST_30_DAYS,
        dataScope: String = "learner",
        forceNetwork: Boolean = false
    ): TimeSpentWidgetData
}

class DashboardTimeSpentRepositoryImpl @Inject constructor(
    private val journeyApiManager: JourneyApiManager
) : DashboardTimeSpentRepository {

    override suspend fun getTimeSpentData(
        widgetId: String,
        timeSpanType: TimeSpanType,
        dataScope: String,
        forceNetwork: Boolean
    ): TimeSpentWidgetData {
        return journeyApiManager.getTimeSpentWidgetData(forceNetwork)
    }
}
