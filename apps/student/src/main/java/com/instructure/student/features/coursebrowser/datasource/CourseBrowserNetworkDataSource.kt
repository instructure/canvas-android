/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.student.features.coursebrowser.datasource

import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.apis.TabAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Tab

class CourseBrowserNetworkDataSource(
    private val tabApi: TabAPI.TabsInterface,
    private val pageApi: PageAPI.PagesInterface
) : CourseBrowserDataSource {
    override suspend fun getTabs(canvasContext: CanvasContext, forceNetwork: Boolean): List<Tab> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return tabApi.getTabs(canvasContext.id, canvasContext.type.apiString, params).dataOrThrow
    }

    override suspend fun getFrontPage(canvasContext: CanvasContext, forceNetwork: Boolean): Page? {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return pageApi.getFrontPage(canvasContext.apiContext(), canvasContext.id, params).dataOrNull
    }
}