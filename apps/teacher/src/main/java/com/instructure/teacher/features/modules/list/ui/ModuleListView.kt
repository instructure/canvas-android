/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.teacher.features.modules.list.ui

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.pandarecycler.PaginatedScrollListener
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.toast
import com.instructure.teacher.R
import com.instructure.teacher.features.modules.list.ModulesListEvent
import com.instructure.teacher.mobius.common.ui.MobiusView
import com.instructure.teacher.utils.setupBackButton
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_module_list.*

class ModuleListView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    val course: CanvasContext
) : MobiusView<ModuleListViewState, ModulesListEvent>(R.layout.fragment_module_list, inflater, parent) {

    private var consumer: Consumer<ModulesListEvent>? = null

    private val scrollListener = PaginatedScrollListener {
        consumer?.accept(ModulesListEvent.NextPageRequested)
    }

    private val layoutManager= LinearLayoutManager(context)

    private val adapter = ModuleListRecyclerAdapter(object : ModuleListCallback {
        override fun retryNextPage() {
            consumer?.accept(ModulesListEvent.NextPageRequested)
        }

        override fun moduleItemClicked(moduleItemId: Long) {
            consumer?.accept(ModulesListEvent.ModuleItemClicked(moduleItemId))
        }

        override fun markModuleExpanded(moduleId: Long, isExpanded: Boolean) {
            consumer?.accept(ModulesListEvent.ModuleExpanded(moduleId, isExpanded))
        }

    })

    init {
        // Toolbar setup
        toolbar.subtitle = course.name
        toolbar.setupBackButton(context)
        ViewStyler.themeToolbar(context as Activity, toolbar, course)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(scrollListener)

        swipeRefreshLayout.setOnRefreshListener {
            consumer?.accept(ModulesListEvent.PullToRefresh)
        }
    }

    override fun onConnect(output: Consumer<ModulesListEvent>) {
        consumer = output
    }

    override fun render(state: ModuleListViewState) {
        swipeRefreshLayout.isRefreshing = state.showRefreshing
        adapter.setData(state.items, state.collapsedModuleIds)
        if (state.items.isEmpty()) scrollListener.resetScroll()
    }

    override fun onDispose() {
        consumer = null
    }

    fun routeToModuleItem(item: ModuleItem) {
        context.toast("Route to Module Item")
        // TODO
    }

    fun scrollToItem(itemId: Long) {
        val itemPosition = adapter.getItemVisualPosition(itemId)
        recyclerView?.scrollToPosition(itemPosition)
    }
}
