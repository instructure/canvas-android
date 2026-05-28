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

import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.models.UnreadNotificationCount
import com.instructure.canvasapi2.models.User
import com.instructure.horizon.data.datasource.DashboardNetworkDataSource
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    private val networkDataSource: DashboardNetworkDataSource,
) {
    suspend fun getUnreadCounts(forceNetwork: Boolean): List<UnreadNotificationCount> {
        return networkDataSource.getUnreadCounts(forceNetwork)
    }

    suspend fun getTheme(): CanvasTheme? {
        return networkDataSource.getTheme()
    }

    suspend fun getSelf(): User? {
        return networkDataSource.getSelf()
    }
}
