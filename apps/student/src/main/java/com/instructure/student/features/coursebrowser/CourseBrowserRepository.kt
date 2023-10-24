/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 */

package com.instructure.student.features.coursebrowser

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.coursebrowser.datasource.CourseBrowserDataSource
import com.instructure.student.features.coursebrowser.datasource.CourseBrowserLocalDataSource
import com.instructure.student.features.coursebrowser.datasource.CourseBrowserNetworkDataSource

class CourseBrowserRepository(
    networkDataSource: CourseBrowserNetworkDataSource,
    localDataSource: CourseBrowserLocalDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider
) : Repository<CourseBrowserDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider) {

    suspend fun getTabs(canvasContext: CanvasContext, forceNetwork: Boolean): List<Tab> {
        val tabs = dataSource().getTabs(canvasContext, forceNetwork)
        return tabs.filter { !(it.isExternal && it.isHidden) }
    }

    suspend fun getFrontPage(canvasContext: CanvasContext, forceNetwork: Boolean): Page? {
        return dataSource().getFrontPage(canvasContext, forceNetwork)
    }
}