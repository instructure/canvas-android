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

package com.instructure.student.features.pages.list.datasource

import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.depaginate

class PageListNetworkDataSource(
    private val api: PageAPI.PagesInterface
) : PageListDataSource {

    override suspend fun loadPages(canvasContext: CanvasContext, forceNetwork: Boolean): List<Page> {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        val pages = api.getFirstPagePages(canvasContext.id, canvasContext.apiContext(), restParams)
            .depaginate { api.getNextPagePagesList(it, restParams) }.dataOrThrow

        return if (pages.any { it.frontPage }) pages else pages.withFrontPage(canvasContext, forceNetwork)
    }

    /**
     * The pages index can come back without the course's front page - missing the entry entirely, or
     * with a stale front_page flag - while GET /front_page still resolves it correctly (MBL-20279).
     * When that happens the list has no front page to render a card for, so ask the dedicated
     * endpoint instead. A course with no front page simply fails this call and keeps the list as-is.
     */
    private suspend fun List<Page>.withFrontPage(canvasContext: CanvasContext, forceNetwork: Boolean): List<Page> {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        val frontPage = api.getFrontPage(canvasContext.apiContext(), canvasContext.id, restParams).dataOrNull ?: return this

        return if (any { it.id == frontPage.id }) {
            map { if (it.id == frontPage.id) it.copy(frontPage = true) else it }
        } else {
            // The index never returns bodies; dropping it here keeps the page out of the details
            // screen's argument Bundle, which a long page body could otherwise overflow.
            this + frontPage.copy(frontPage = true, body = null)
        }
    }
}
