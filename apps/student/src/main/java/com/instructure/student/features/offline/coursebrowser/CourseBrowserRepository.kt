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

package com.instructure.student.features.offline.coursebrowser

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.utils.NetworkStateProvider
import java.lang.IllegalStateException


class CourseBrowserRepository(
    private val networkDataSource: CourseBrowserNetworkDataSource,
    private val localDataSource: CourseBrowserLocalDataSource,
    private val networkStateProvider: NetworkStateProvider
) {

    suspend fun getTabs(canvasContext: CanvasContext, forceNetwork: Boolean): List<Tab> {
        val tabs = if (networkStateProvider.isOnline()) {
            networkDataSource.getTabs(canvasContext, forceNetwork)
        } else {
            localDataSource.getTabs(canvasContext)
        }
        return tabs.filter { !(it.isExternal && it.isHidden) }
    }

    suspend fun getFrontPage(canvasContext: CanvasContext, forceNetwork: Boolean): Page? {
        return if (networkStateProvider.isOnline()) {
            networkDataSource.getFrontPage(canvasContext, forceNetwork)
        } else {
            localDataSource.getFrontPage(canvasContext)
        }
    }
}