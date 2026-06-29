/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.horizon.data.repository

import com.instructure.horizon.data.datasource.WidgetsNetworkDataSource
import com.instructure.horizon.model.MyProgressWidgetData
import com.instructure.horizon.model.TimeSpentDataEntry
import com.instructure.horizon.model.TimeSpentWidgetData
import com.instructure.horizon.util.deserializeDynamicList
import com.instructure.horizon.util.deserializeDynamicObject
import javax.inject.Inject

class WidgetsRepository @Inject constructor(
    private val networkDataSource: WidgetsNetworkDataSource,
) {
    suspend fun getTimeSpentData(courseId: Long?, forceNetwork: Boolean): TimeSpentWidgetData {
        val widgetData = networkDataSource.getTimeSpentWidgetData(courseId, forceNetwork)
        return TimeSpentWidgetData(
            lastModifiedDate = widgetData.lastModifiedDate,
            data = widgetData.data.deserializeDynamicList<TimeSpentDataEntry>(),
        )
    }

    suspend fun getLearningStatusData(courseId: Long?, forceNetwork: Boolean): MyProgressWidgetData? {
        return networkDataSource.getLearningStatusWidgetData(courseId, forceNetwork)
            .deserializeDynamicObject<MyProgressWidgetData>()
    }
}
