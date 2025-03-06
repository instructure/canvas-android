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
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.pandarecycler.PaginatedScrollListener
import com.instructure.pandautils.features.progress.ProgressDialogFragment
import com.instructure.pandautils.room.appdatabase.entities.ModuleBulkProgressEntity
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.showThemed
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentModuleListBinding
import com.instructure.teacher.features.modules.list.BulkModuleUpdateAction
import com.instructure.teacher.features.modules.list.ModuleListEvent
import com.instructure.teacher.features.modules.list.ui.file.UpdateFileDialogFragment
import com.instructure.teacher.features.modules.progression.ModuleProgressionFragment
import com.instructure.teacher.mobius.common.ui.MobiusView
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.setupBackButton
import com.spotify.mobius.functions.Consumer

class ModuleListView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    val course: CanvasContext
) : MobiusView<ModuleListViewState, ModuleListEvent, FragmentModuleListBinding>(
    inflater,
    FragmentModuleListBinding::inflate,
    parent
) {

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
            showConfirmationDialog(
                R.string.publishDialogTitle,
                R.string.publishModuleDialogMessage,
                R.string.publishDialogPositiveButton,
                R.string.cancel
            ) {
                consumer?.accept(ModuleListEvent.BulkUpdateModule(moduleId, BulkModuleUpdateAction.PUBLISH, true))
            }
        }

        override fun publishModuleAndItems(moduleId: Long) {
            showConfirmationDialog(
                R.string.publishDialogTitle,
                R.string.publishModuleAndItemsDialogMessage,
                R.string.publishDialogPositiveButton,
                R.string.cancel
            ) {
                consumer?.accept(ModuleListEvent.BulkUpdateModule(moduleId, BulkModuleUpdateAction.PUBLISH, false))
            }
        }

        override fun unpublishModuleAndItems(moduleId: Long) {
            showConfirmationDialog(
                R.string.unpublishDialogTitle,
                R.string.unpublishModuleAndItemsDialogMessage,
                R.string.unpublishDialogPositiveButton,
                R.string.cancel
            ) {
                consumer?.accept(ModuleListEvent.BulkUpdateModule(moduleId, BulkModuleUpdateAction.UNPUBLISH, false))
            }
        }

        override fun updateFileModuleItem(fileId: Long, contentDetails: ModuleContentDetails) {
            consumer?.accept(
                ModuleListEvent.UpdateFileModuleItem(
                    fileId,
                    contentDetails
                )
            )
        }

        override fun showSnackbar(@StringRes message: Int, params: Array<Any>) {
            consumer?.accept(ModuleListEvent.ShowSnackbar(message, params))
        }

        override fun updateModuleItem(itemId: Long, isPublished: Boolean) {
            val title = if (isPublished) R.string.publishDialogTitle else R.string.unpublishDialogTitle
            val message =
                if (isPublished) R.string.publishModuleItemDialogMessage else R.string.unpublishModuleItemDialogMessage
            val positiveButton = if (isPublished) R.string.publishDialogPositiveButton else R.string.unpublishDialogPositiveButton

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
                        showConfirmationDialog(
                            R.string.publishDialogTitle,
                            R.string.publishModulesAndItemsDialogMessage,
                            R.string.publishDialogPositiveButton,
                            R.string.cancel
                        ) {
                            consumer?.accept(
                                ModuleListEvent.BulkUpdateAllModules(
                                    BulkModuleUpdateAction.PUBLISH,
                                    false
                                )
                            )
                        }
                        true
                    }

                    R.id.actionPublishModules -> {
                        showConfirmationDialog(
                            R.string.publishDialogTitle,
                            R.string.publishModulesDialogMessage,
                            R.string.publishDialogPositiveButton,
                            R.string.cancel
                        ) {
                            consumer?.accept(ModuleListEvent.BulkUpdateAllModules(BulkModuleUpdateAction.PUBLISH, true))
                        }
                        true
                    }

                    R.id.actionUnpublishModulesItems -> {
                        showConfirmationDialog(
                            R.string.unpublishDialogTitle,
                            R.string.unpublishModulesAndItemsDialogMessage,
                            R.string.unpublishDialogPositiveButton,
                            R.string.cancel
                        ) {
                            consumer?.accept(
                                ModuleListEvent.BulkUpdateAllModules(
                                    BulkModuleUpdateAction.UNPUBLISH,
                                    false
                                )
                            )
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

    fun showConfirmationDialog(
        title: Int,
        message: Int,
        positiveButton: Int,
        negativeButton: Int,
        onConfirmed: () -> Unit
    ) {
        AlertDialog.Builder(context, R.style.AccessibleAlertDialog)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton) { _, _ ->
                onConfirmed()
            }
            .setNegativeButton(negativeButton) { _, _ -> }
            .showThemed()
    }

    fun showSnackbar(@StringRes message: Int, params: Array<Any> = emptyArray()) {
        val messageText = context.getString(message, *params)
        Snackbar.make(binding.root, messageText, Snackbar.LENGTH_SHORT).show()
        binding.root.announceForAccessibility(messageText)
    }

    fun showUpdateFileDialog(fileId: Long, contentDetails: ModuleContentDetails) {
        val fragment = UpdateFileDialogFragment.newInstance(fileId, contentDetails, course)
        fragment.show((activity as FragmentActivity).supportFragmentManager, "editFileDialog")
    }

    fun showProgressDialog(
        progressId: Long,
        @StringRes title: Int,
        @StringRes progressTitle: Int,
        @StringRes note: Int? = null
    ) {
        val fragment = ProgressDialogFragment.newInstance(
            progressId,
            context.getString(title),
            context.getString(progressTitle),
            note?.let { context.getString(it) })
        fragment.show((activity as FragmentActivity).supportFragmentManager, "progressDialog")
    }

    fun bulkUpdateInProgress(progresses: List<ModuleBulkProgressEntity>) {
        progresses.forEach {
            consumer?.accept(ModuleListEvent.BulkUpdateStarted(course, it.progressId, it.allModules, it.skipContentTags, it.affectedIds, BulkModuleUpdateAction.valueOf(it.action)))
        }
    }
}
