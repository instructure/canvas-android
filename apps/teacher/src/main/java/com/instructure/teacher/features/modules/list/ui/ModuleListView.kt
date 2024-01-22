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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.pandarecycler.PaginatedScrollListener
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.showThemed
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentModuleListBinding
import com.instructure.teacher.features.modules.list.BulkModuleUpdateAction
import com.instructure.teacher.features.modules.list.ModuleListEvent
import com.instructure.teacher.features.modules.progression.ModuleProgressionFragment
import com.instructure.teacher.mobius.common.ui.MobiusView
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.setupBackButton
import com.spotify.mobius.functions.Consumer

class ModuleListView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    val course: CanvasContext
) : MobiusView<ModuleListViewState, ModuleListEvent, FragmentModuleListBinding>(inflater, FragmentModuleListBinding::inflate, parent) {

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

        override fun publishModule(moduleId: Long) {
            showConfirmationDialog(R.string.publishDialogTitle, R.string.publishModuleDialogMessage, R.string.publish, R.string.cancel) {
                consumer?.accept(ModuleListEvent.BulkUpdateModule(moduleId, BulkModuleUpdateAction.PUBLISH, true))
            }
        }

        override fun publishModuleAndItems(moduleId: Long) {
            showConfirmationDialog(R.string.publishDialogTitle, R.string.publishModuleAndItemsDialogMessage, R.string.publish, R.string.cancel) {
                consumer?.accept(ModuleListEvent.BulkUpdateModule(moduleId, BulkModuleUpdateAction.PUBLISH, false))
            }
        }

        override fun unpublishModuleAndItems(moduleId: Long) {
            showConfirmationDialog(R.string.unpublishDialogTitle, R.string.unpublishModuleAndItemsDialogMessage, R.string.unpublish, R.string.cancel) {
                consumer?.accept(ModuleListEvent.BulkUpdateModule(moduleId, BulkModuleUpdateAction.UNPUBLISH, false))
            }
        }

        override fun updateModuleItem(itemId: Long, isPublished: Boolean) {
            val title = if (isPublished) R.string.publishDialogTitle else R.string.unpublishDialogTitle
            val message = if (isPublished) R.string.publishModuleItemDialogMessage else R.string.unpublishModuleItemDialogMessage
            val positiveButton = if (isPublished) R.string.publish else R.string.unpublish

            showConfirmationDialog(title, message, positiveButton, R.string.cancel) {
                consumer?.accept(ModuleListEvent.UpdateModuleItem(itemId, isPublished))
            }
        }
    })

    init {
        // Toolbar setup
        binding.toolbar.apply {
            subtitle = course.name
            setupBackButton(activity)
            ViewStyler.themeToolbarColored(activity, this, course)
            inflateMenu(R.menu.menu_module_list)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.actionPublishModulesItems -> {
                        showConfirmationDialog(R.string.publishDialogTitle, R.string.publishModulesAndItemsDialogMessage, R.string.publish, R.string.cancel) {
                            consumer?.accept(ModuleListEvent.BulkUpdateAllModules(BulkModuleUpdateAction.PUBLISH, false))
                        }
                        true
                    }
                    R.id.actionPublishModules -> {
                        showConfirmationDialog(R.string.publishDialogTitle, R.string.publishModulesDialogMessage, R.string.publish, R.string.cancel) {
                            consumer?.accept(ModuleListEvent.BulkUpdateAllModules(BulkModuleUpdateAction.PUBLISH, true))
                        }
                        true
                    }
                    R.id.actionUnpublishModulesItems -> {
                        showConfirmationDialog(R.string.unpublishDialogTitle, R.string.unpublishModulesAndItemsDialogMessage, R.string.unpublish, R.string.cancel) {
                            consumer?.accept(ModuleListEvent.BulkUpdateAllModules(BulkModuleUpdateAction.UNPUBLISH, false))
                        }
                        true
                    }
                    else -> false
                }
            }
        }

        binding.recyclerView.apply {
            layoutManager = this@ModuleListView.layoutManager
            adapter = this@ModuleListView.adapter
            addOnScrollListener(scrollListener)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            consumer?.accept(ModuleListEvent.PullToRefresh)
        }
    }

    override fun onConnect(output: Consumer<ModuleListEvent>) {
        consumer = output
    }

    override fun render(state: ModuleListViewState) {
        binding.swipeRefreshLayout.isRefreshing = state.showRefreshing
        adapter.setData(state.items, state.collapsedModuleIds)
        if (state.items.isEmpty()) scrollListener.resetScroll()
    }

    override fun onDispose() {
        consumer = null
    }

    fun routeToModuleItem(item: ModuleItem, canvasContext: CanvasContext) {
        val route = ModuleProgressionFragment.makeRoute(canvasContext, item.id)
        RouteMatcher.route(activity as FragmentActivity, route)
    }

    fun scrollToItem(itemId: Long) {
        val itemPosition = adapter.getItemVisualPosition(itemId)
        binding.recyclerView.scrollToPosition(itemPosition)
    }

    fun showConfirmationDialog(title: Int, message: Int, positiveButton: Int, negativeButton: Int, onConfirmed: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton) { _, _ ->
                onConfirmed()
            }
            .setNegativeButton(negativeButton) { _, _ -> }
            .showThemed()
    }

    fun showSnackbar(@StringRes message: Int) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
