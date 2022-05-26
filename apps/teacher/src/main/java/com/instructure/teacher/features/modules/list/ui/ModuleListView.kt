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
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.License
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.utils.tryOrNull
import com.instructure.interactions.router.Route
import com.instructure.pandarecycler.PaginatedScrollListener
import com.instructure.pandautils.models.EditableFile
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.color
import com.instructure.teacher.R
import com.instructure.teacher.features.modules.list.ModuleListEvent
import com.instructure.teacher.fragments.*
import com.instructure.teacher.mobius.common.ui.MobiusView
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.utils.viewMedia
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_module_list.*

class ModuleListView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    val course: CanvasContext
) : MobiusView<ModuleListViewState, ModuleListEvent>(R.layout.fragment_module_list, inflater, parent) {

    private var consumer: Consumer<ModuleListEvent>? = null

    private val scrollListener = PaginatedScrollListener {
        consumer?.accept(ModuleListEvent.NextPageRequested)
    }

    private val layoutManager = LinearLayoutManager(context)

    private val adapter = ModuleListRecyclerAdapter(context, object : ModuleListCallback {
        override fun retryNextPage() {
            consumer?.accept(ModuleListEvent.NextPageRequested)
        }

        override fun moduleItemClicked(moduleItemId: Long) {
            consumer?.accept(ModuleListEvent.ModuleItemClicked(moduleItemId))
        }

        override fun markModuleExpanded(moduleId: Long, isExpanded: Boolean) {
            consumer?.accept(ModuleListEvent.ModuleExpanded(moduleId, isExpanded))
        }

    })

    init {
        // Toolbar setup
        toolbar.subtitle = course.name
        toolbar.setupBackButton(context)
        ViewStyler.themeToolbarColored(context as Activity, toolbar, course)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(scrollListener)

        swipeRefreshLayout.setOnRefreshListener {
            consumer?.accept(ModuleListEvent.PullToRefresh)
        }
    }

    override fun onConnect(output: Consumer<ModuleListEvent>) {
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

    fun routeToModuleItem(item: ModuleItem, canvasContext: CanvasContext) {
        val route = when (tryOrNull { ModuleItem.Type.valueOf(item.type!!) }) {
            ModuleItem.Type.Assignment -> {
                val args = AssignmentDetailsFragment.makeBundle(item.contentId)
                Route(null, AssignmentDetailsFragment::class.java, canvasContext, args)
            }
            ModuleItem.Type.Discussion -> {
                val args = DiscussionsDetailsFragment.makeBundle(item.contentId)
                Route(null, DiscussionsDetailsFragment::class.java, canvasContext, args)
            }
            ModuleItem.Type.Page -> {
                val args = PageDetailsFragment.makeBundle(item.pageUrl!!)
                Route(null, PageDetailsFragment::class.java, canvasContext, args)
            }
            ModuleItem.Type.Quiz -> {
                val args = QuizDetailsFragment.makeBundle(item.contentId)
                Route(null, QuizDetailsFragment::class.java, canvasContext, args)
            }
            ModuleItem.Type.ExternalUrl -> {
                val args = InternalWebViewFragment.makeBundle(
                    item.externalUrl.orEmpty(),
                    item.title.orEmpty()
                )
                Route(null, InternalWebViewFragment::class.java, canvasContext, args)
            }
            ModuleItem.Type.ExternalTool -> {
                val args = LtiLaunchFragment.makeBundle(
                    canvasContext = canvasContext,
                    url = item.url.orEmpty(),
                    title = item.title.orEmpty(),
                    sessionLessLaunch = true
                )
                Route(null, LtiLaunchFragment::class.java, canvasContext, args)
            }
            else -> null
        }
        RouteMatcher.route(context, route)
    }

    fun routeToFile(
        canvasContext: CanvasContext,
        file: FileFolder,
        requiresUsageRights: Boolean,
        licenses: List<License>
    ) {
        val editableFile = EditableFile(
            file = file,
            usageRights = requiresUsageRights,
            licenses = licenses,
            courseColor = canvasContext.color,
            canvasContext = canvasContext,
            iconRes = R.drawable.ic_document
        )
        viewMedia(
            context = context,
            filename = file.displayName.orEmpty(),
            contentType = file.contentType.orEmpty(),
            url = file.url,
            thumbnailUrl = file.thumbnailUrl,
            displayName = file.displayName,
            iconRes = R.drawable.ic_document,
            toolbarColor = canvasContext.color,
            editableFile = editableFile
        )
    }

    fun scrollToItem(itemId: Long) {
        val itemPosition = adapter.getItemVisualPosition(itemId)
        recyclerView?.scrollToPosition(itemPosition)
    }
}
