/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.presenters

import com.instructure.canvasapi2.managers.PageManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.filterWithQuery
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.teacher.viewinterface.PageListView
import com.instructure.pandautils.blueprint.SyncPresenter
import kotlinx.coroutines.Job

class PageListPresenter(private val mCanvasContext: CanvasContext) :
    SyncPresenter<Page, PageListView>(Page::class.java) {

    private var pages = emptyList<Page>()

    private var apiCalls: Job? = null

    var searchQuery = ""
        set(value) {
            field = value
            clearData()
            populateData()
        }

    override fun loadData(forceNetwork: Boolean) {
        if (data.size() > 0 && !forceNetwork) {
            viewCallback?.onRefreshFinished()
            viewCallback?.checkIfEmpty()
            return
        }
        if (forceNetwork) clearData()
        apiCalls = tryWeave {
            onRefreshStarted()
            pages = awaitApi { PageManager.getAllPages(mCanvasContext, forceNetwork, it) }
            populateData()
        } catch {
        }
    }

    override fun compare(page1: Page, page2: Page) = page1.compareTo(page2)

    private fun populateData() {
        pages.filterWithQuery(searchQuery, Page::title).forEach { data.addOrUpdate(it) }
        viewCallback?.onRefreshFinished()
        viewCallback?.checkIfEmpty()
    }

    override fun refresh(forceNetwork: Boolean) {
        apiCalls?.cancel()
        clearData()
        loadData(forceNetwork)
    }

    override fun onDestroyed() {
        super.onDestroyed()
        apiCalls?.cancel()
    }
}
