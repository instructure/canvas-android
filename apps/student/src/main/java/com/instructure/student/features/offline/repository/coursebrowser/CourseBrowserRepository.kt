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

package com.instructure.student.features.offline.repository.coursebrowser

import com.instructure.canvasapi2.apis.TabAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.room.daos.TabDao


class CourseBrowserRepository(
    private val tabApi: TabAPI.TabsInterface,
    private val tabDao: TabDao
) {

    suspend fun getTabs(canvasContext: CanvasContext, forceNetwork: Boolean): List<Tab> {
        val tabs = tabDao.findByCourseId(canvasContext.id).map {
            it.toApiModel()
        }
        return tabs.filter { !(it.isExternal && it.isHidden) }
    }

    private suspend fun fetchTabs(canvasContext: CanvasContext, forceNetwork: Boolean): List<Tab> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return tabApi.getTabs(canvasContext.id, params).dataOrThrow
    }
}