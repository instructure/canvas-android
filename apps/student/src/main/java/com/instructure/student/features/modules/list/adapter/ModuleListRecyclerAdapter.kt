/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.features.modules.list.adapter

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.CountDownTimer
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.managers.graphql.ModuleItemCheckpoint
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.Failure
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandarecycler.interfaces.ViewHolderHeaderClicked
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandarecycler.util.Types
import com.instructure.pandautils.utils.Utils
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.orDefault
import com.instructure.student.R
import com.instructure.student.adapter.ExpandableRecyclerAdapter
import com.instructure.student.features.modules.list.CollapsedModulesStore
import com.instructure.student.features.modules.list.ModuleListRepository
import com.instructure.student.features.modules.util.ModuleUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

open class ModuleListRecyclerAdapter(
    private val courseContext: CanvasContext,
    context: Context,
    private var shouldExhaustPagination: Boolean,
    private val repository: ModuleListRepository,
    private val lifecycleScope: CoroutineScope,
    private val adapterToFragmentCallback: ModuleAdapterToFragmentCallback?
) : ExpandableRecyclerAdapter<ModuleObject, ModuleItem, RecyclerView.ViewHolder>(
    context,
    ModuleObject::class.java,
    ModuleItem::class.java
) {

    private var initialDataJob: Job? = null
    private var moduleObjectJob: Job? = null

    private val moduleFromNetworkOrDb = HashMap<Long, Boolean>()
    private var courseSettings: CourseSettings? = null
    private var moduleItemCheckpointsMap: Map<String, List<ModuleItemCheckpoint>> = emptyMap()

    /* For testing purposes only */
    protected constructor(
        context: Context,
        repository: ModuleListRepository,
        lifecycleScope: CoroutineScope
    ) : this(
        CanvasContext.defaultCanvasContext(),
        context,
        false,
        repository,
        lifecycleScope,
        null
    ) // Callback not needed for testing, cast to null

    init {
        viewHolderHeaderClicked = object : ViewHolderHeaderClicked<ModuleObject> {
            override fun viewClicked(view: View?, moduleObject: ModuleObject) {
                val isFromNetworkOrDb = moduleFromNetworkOrDb[moduleObject.id] ?: false
                if (!isFromNetworkOrDb && !isGroupExpanded(moduleObject)) {
                    lifecycleScope.launch {
                        val result = repository.getFirstPageModuleItems(courseContext, moduleObject.id, true)
                        handleModuleItemResponse(result, moduleObject, false)
                    }
                } else {
                    CollapsedModulesStore.markModuleCollapsed(courseContext, moduleObject.id, true)
                    expandCollapseGroup(moduleObject)
                }
            }

        }
        isExpandedByDefault = false
        isDisplayEmptyCell = true
        if (adapterToFragmentCallback != null) loadData() // Callback is null when testing
    }

    override fun createViewHolder(v: View, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Types.TYPE_HEADER -> ModuleHeaderViewHolder(v)
            Types.TYPE_SUB_HEADER -> ModuleSubHeaderViewHolder(v)
            Types.TYPE_EMPTY_CELL -> ModuleEmptyViewHolder(v)
            else -> ModuleViewHolder(v)
        }
    }

    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, moduleObject: ModuleObject, moduleItem: ModuleItem) {
        if (holder is ModuleSubHeaderViewHolder) {
            val groupItemCount = getGroupItemCount(moduleObject)
            val itemPosition = storedIndexOfItem(moduleObject, moduleItem)
            holder.bind(moduleItem, itemPosition == 0, itemPosition == groupItemCount - 1)
        } else {
            val courseColor = courseContext.color
            val groupItemCount = getGroupItemCount(moduleObject)
            val itemPosition = storedIndexOfItem(moduleObject, moduleItem)
            val checkpoints = moduleItemCheckpointsMap[moduleItem.id.toString()]

            (holder as ModuleViewHolder).bind(
                moduleObject, moduleItem, context, adapterToFragmentCallback, courseColor,
                itemPosition == 0, itemPosition == groupItemCount - 1, courseSettings?.restrictQuantitativeData.orDefault(), checkpoints
            )
        }
    }

    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, moduleObject: ModuleObject, isExpanded: Boolean) {
        (holder as ModuleHeaderViewHolder).bind(moduleObject, context, viewHolderHeaderClicked, isExpanded)
    }

    override fun onBindEmptyHolder(holder: RecyclerView.ViewHolder, moduleObject: ModuleObject) {
        val moduleEmptyViewHolder = holder as ModuleEmptyViewHolder
        // Keep displaying No connection as long as the result is not from network
        // Doing so will cause the user to toggle the expand to refresh the list, if they had expanded a module while offline
        if (moduleFromNetworkOrDb.containsKey(moduleObject.id) && moduleFromNetworkOrDb[moduleObject.id] == true) {
            moduleEmptyViewHolder.bind(getPrerequisiteString(moduleObject))
        } else {
            moduleEmptyViewHolder.bind(context.getString(R.string.noConnection))
        }
    }

    override fun itemLayoutResId(viewType: Int): Int {
        return when (viewType) {
            Types.TYPE_HEADER -> ModuleHeaderViewHolder.HOLDER_RES_ID
            Types.TYPE_SUB_HEADER -> ModuleSubHeaderViewHolder.HOLDER_RES_ID
            Types.TYPE_EMPTY_CELL -> ModuleEmptyViewHolder.HOLDER_RES_ID
            else -> ModuleViewHolder.HOLDER_RES_ID
        }
    }

    override fun refresh() {
        shouldExhaustPagination = false
        moduleFromNetworkOrDb.clear()
        initialDataJob?.cancel()
        collapseAll()
        super.refresh()
    }

    private suspend fun fetchModuleItemCheckpoints() {
        try {
            val checkpoints = repository.getModuleItemCheckpoints(courseContext.id.toString(), true)
            moduleItemCheckpointsMap = checkpoints.associate { it.moduleItemId to it.checkpoints }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            moduleItemCheckpointsMap = emptyMap()
        }
    }

    // region Expandable Callbacks
    override fun createGroupCallback(): GroupSortedList.GroupComparatorCallback<ModuleObject> {
        return object : GroupSortedList.GroupComparatorCallback<ModuleObject> {
            override fun compare(o1: ModuleObject, o2: ModuleObject): Int = o1.position - o2.position
            override fun areContentsTheSame(oldGroup: ModuleObject, newGroup: ModuleObject): Boolean {
                val isNewLocked = ModuleUtility.isGroupLocked(newGroup)
                val isOldLocked = ModuleUtility.isGroupLocked(oldGroup)
                return oldGroup.name == newGroup.name && isNewLocked == isOldLocked
            }

            override fun areItemsTheSame(group1: ModuleObject, group2: ModuleObject): Boolean = group1.id == group2.id
            override fun getGroupType(group: ModuleObject): Int = Types.TYPE_HEADER
            override fun getUniqueGroupId(group: ModuleObject): Long = group.id
        }
    }

    override fun createItemCallback(): GroupSortedList.ItemComparatorCallback<ModuleObject, ModuleItem> {
        return object : GroupSortedList.ItemComparatorCallback<ModuleObject, ModuleItem> {
            override fun compare(group: ModuleObject, o1: ModuleItem, o2: ModuleItem): Int = o1.position - o2.position
            override fun areContentsTheSame(oldItem: ModuleItem, newItem: ModuleItem): Boolean = oldItem.title == newItem.title
            override fun areItemsTheSame(item1: ModuleItem, item2: ModuleItem): Boolean = item1.id == item2.id

            override fun getChildType(group: ModuleObject, item: ModuleItem): Int {
                return if (item.type == ModuleItem.Type.SubHeader.toString()) {
                    Types.TYPE_SUB_HEADER
                } else Types.TYPE_ITEM
            }

            override fun getUniqueItemId(item: ModuleItem): Long = item.id
        }
    }
    // endregion


    private fun createProgressDialog(context: Context): ProgressDialog {
        val dialog = ProgressDialog(context)
        try {
            dialog.show()
        } catch (e: WindowManager.BadTokenException) {
        }

        dialog.setCancelable(false)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.progress_dialog)
        val currentColor = courseContext.color

        (dialog.findViewById<View>(R.id.progressBar) as ProgressBar).indeterminateDrawable.setColorFilter(
            currentColor,
            PorterDuff.Mode.SRC_ATOP
        )
        return dialog
    }

    fun updateWithoutResettingViews(moduleObject: ModuleObject) {
        moduleFromNetworkOrDb.clear()
        moduleObjectJob?.cancel()

        lifecycleScope.launch {
            val result = repository.getFirstPageModuleItems(courseContext, moduleObject.id, true)
            handleModuleItemResponse(result, moduleObject, false)
        }
    }

    fun updateMasteryPathItems() {

        val dialog = createProgressDialog(context)
        dialog.show()
        // Show for 3 seconds and then refresh the list
        // This 3 seconds is to allow the Canvas database to update so we can pull the module info down
        object : CountDownTimer(3000, 1000) {

            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                dialog.cancel()
                refresh()
            }
        }.start()
    }

    private fun handleModuleItemResponse(result: DataResult<List<ModuleItem>>, moduleObject: ModuleObject, isNotifyGroupChange: Boolean) {
        if (result is DataResult.Success) {
            val resultIsFromApiOrDb = result.apiType === ApiType.API || result.apiType == ApiType.DB
            val moduleItems = result.data

            var position = if (moduleItems.isNotEmpty()) moduleItems[0].position - 1 else 0
            for (item in moduleItems) {
                item.position = position++
                addOrUpdateItem(moduleObject, item)
                position = if (resultIsFromApiOrDb) checkMasteryPaths(position, item, moduleObject) else position
            }

            val nextItemsURL = result.linkHeaders.nextUrl
            if (nextItemsURL != null) {
                lifecycleScope.launch {
                    val nextPageResult = repository.getNextPageModuleItems(nextItemsURL, true)
                    handleModuleItemResponse(nextPageResult, moduleObject, isNotifyGroupChange)
                }
            }

            if (resultIsFromApiOrDb) {
                moduleFromNetworkOrDb.put(moduleObject.id, true)
                expandGroup(moduleObject, isNotifyGroupChange)
            } else if (result.apiType === ApiType.CACHE) {
                // Wait for the network to expand when there are no items
                if (moduleItems.isNotEmpty()) {
                    expandGroup(moduleObject, isNotifyGroupChange)
                }
            }
            CollapsedModulesStore.markModuleCollapsed(courseContext, moduleObject.id, false)
        } else {
            // Only expand if there was no cache result and no network. No connection empty cell will be displayed
            val failedResult = result as DataResult.Fail
            val errorCode = (failedResult.failure as? Failure.Network)?.errorCode
            if (failedResult.response != null
                && errorCode == 504
                && APIHelper.isCachedResponse(failedResult.response!!)
                && !Utils.isNetworkAvailable(context)
            ) {
                expandGroup(moduleObject, isNotifyGroupChange)
            }
        }
    }

    private fun checkMasteryPaths(initPosition: Int, item: ModuleItem, moduleObject: ModuleObject): Int {
        var position = initPosition
        if (item.masteryPaths != null && item.masteryPaths!!.isLocked) {
            // Add another module item that says it's locked
            val masteryPathsLocked = ModuleItem(
                // Set an id so that if there is more than one path we'll display all of them. otherwise addOrUpdateItem will overwrite it
                id = UUID.randomUUID().leastSignificantBits,
                title = String.format(Locale.getDefault(), context.getString(R.string.locked_mastery_paths), item.title),
                type = ModuleItem.Type.Locked.toString(),
                completionRequirement = null,
                position = position++
            )
            addOrUpdateItem(moduleObject, masteryPathsLocked)
        } else if (item.masteryPaths != null && !item.masteryPaths!!.isLocked && item.masteryPaths!!.selectedSetId == 0L) {
            // Add another module item that says select to choose assignment group
            // We only want to do this when we have a mastery paths object, it's unlocked, and the user hasn't already selected a set
            val masteryPathsSelect = ModuleItem(
                // Set an id so that if there is more than one path we'll display all of them. otherwise addOrUpdateItem will overwrite it
                id = UUID.randomUUID().leastSignificantBits,
                title = context.getString(R.string.choose_assignment_group),
                type = ModuleItem.Type.ChooseAssignmentGroup.toString(),
                completionRequirement = null,
                position = position++
            )

            // Sort the mastery paths by position
            item.masteryPaths!!.assignmentSets!!.sortBy { it?.position }
            masteryPathsSelect.masteryPathsItemId = item.id
            masteryPathsSelect.masteryPaths = item.masteryPaths
            addOrUpdateItem(moduleObject, masteryPathsSelect)
            notifyDataSetChanged()
        }
        return position
    }

    // region Pagination
    override val isPaginated get() = true

    private fun handleModuleObjectsResponse(result: DataResult<List<ModuleObject>>) {
        if (result is DataResult.Success) {
            val moduleObjects = result.data
            setNextUrl(result.linkHeaders.nextUrl)
            val collapsedItems = CollapsedModulesStore.getCollapsedModuleIds(courseContext)
            moduleObjects.toTypedArray().forEach {
                addOrUpdateGroup(it)
                if (!collapsedItems.contains(it.id)) {
                    lifecycleScope.launch {
                        val itemsResult = repository.getFirstPageModuleItems(courseContext, it.id, true)
                        handleModuleItemResponse(itemsResult, it, true)
                    }
                }
            }
            if (!shouldExhaustPagination || result.linkHeaders.nextUrl == null) {
                // If we should exhaust pagination wait until we are done exhausting pagination
                adapterToFragmentCallback?.onRefreshFinished()
            }
            this@ModuleListRecyclerAdapter.onCallbackFinished(result.apiType)
        } else {
            this@ModuleListRecyclerAdapter.onCallbackFinished(ApiType.API) // We can only get failed data result when it comes from the API
        }
    }

    override fun loadFirstPage() {
        initialDataJob = lifecycleScope.tryLaunch {
            val tabs = repository.getTabs(courseContext, isRefresh)
            courseSettings = repository.loadCourseSettings(courseContext.id, isRefresh)
            fetchModuleItemCheckpoints()

            // We only want to show modules if its a course nav option OR set to as the homepage
            if (tabs.find { it.tabId == "modules" } != null || (courseContext as Course).homePage?.apiString == "modules") {
                moduleObjectJob = lifecycleScope.launch {
                    val result = if (shouldExhaustPagination) {
                        repository.getAllModuleObjects(courseContext, true)
                    } else {
                        repository.getFirstPageModuleObjects(courseContext, true)
                    }
                    handleModuleObjectsResponse(result)
                }
            } else {
                adapterToFragmentCallback?.onRefreshFinished(true)
            }
        } catch {
            adapterToFragmentCallback?.onRefreshFinished(true)
        }
    }

    override fun loadNextPage(nextURL: String) {
        moduleObjectJob = lifecycleScope.launch {
            val result = repository.getNextPageModuleObjects(nextURL, true)
            handleModuleObjectsResponse(result)
        }
    }

    // endregion

    // region Module binder Helper
    // never actually shows prereqs because grayed out module items show instead.
    private fun getPrerequisiteString(moduleObject: ModuleObject): String {
        var prereqString = context.getString(R.string.noItemsToDisplayShort)

        if (ModuleUtility.isGroupLocked(moduleObject)) {
            prereqString = context.getString(R.string.locked)
        }

        if (moduleObject.state != null &&
            moduleObject.state == ModuleObject.State.Locked.apiString &&
            getGroupItemCount(moduleObject) > 0 &&
            getItem(moduleObject, 0)?.type == ModuleObject.State.UnlockRequirements.apiString
        ) {

            val reqs = StringBuilder()
            val ids = moduleObject.prerequisiteIds
            //check to see if they need to finish other modules first
            if (ids != null) {
                for (i in ids.indices) {
                    val prereqModuleObject = getGroup(ids[i])
                    if (prereqModuleObject != null) {
                        if (i == 0) { //if it's the first one, add the "Prerequisite:" label
                            reqs.append(context.getString(R.string.prerequisites) + " " + prereqModuleObject.name)
                        } else {
                            reqs.append(", " + prereqModuleObject.name!!)
                        }
                    }
                }
            }

            if (moduleObject.unlockAt != null) {
                //only want a newline if there are prerequisite ids
                if (ids!!.size > 0 && ids[0] != 0L) {
                    reqs.append("\n")
                }
                reqs.append(DateHelper.createPrefixedDateTimeString(context, R.string.unlocked, moduleObject.unlockDate))
            }

            prereqString = reqs.toString()
        }
        return prereqString
    }
    // endregion
}
