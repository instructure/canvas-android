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
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandarecycler.interfaces.ViewHolderHeaderClicked
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandarecycler.util.Types
import com.instructure.pandautils.utils.Utils
import com.instructure.pandautils.utils.textAndIconColor
import com.instructure.student.R
import com.instructure.student.adapter.ExpandableRecyclerAdapter
import com.instructure.student.features.modules.list.CollapsedModulesStore
import com.instructure.student.features.modules.list.ModuleListRepository
import com.instructure.student.features.modules.util.ModuleUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import retrofit2.Call
import retrofit2.Response
import java.util.Locale
import java.util.UUID

open class ModuleListRecyclerAdapter(
    private val courseContext: CanvasContext,
    context: Context,
    private var shouldExhaustPagination: Boolean,
    private val repository: ModuleListRepository,
    private val lifecycleScope: CoroutineScope,
    private val adapterToFragmentCallback: ModuleAdapterToFragmentCallback?
) : ExpandableRecyclerAdapter<ModuleObject, ModuleItem, RecyclerView.ViewHolder>(context, ModuleObject::class.java, ModuleItem::class.java) {

    private val mModuleItemCallbacks = HashMap<Long, ModuleItemCallback>()
    private var mModuleObjectCallback: StatusCallback<List<ModuleObject>>? = null
    private var checkCourseTabsJob: Job? = null

    /* For testing purposes only */
    protected constructor(context: Context, repository: ModuleListRepository, lifecycleScope: CoroutineScope) : this(CanvasContext.defaultCanvasContext(), context, false, repository, lifecycleScope, null) // Callback not needed for testing, cast to null

    init {
        viewHolderHeaderClicked = object : ViewHolderHeaderClicked<ModuleObject> {
            override fun viewClicked(view: View?, moduleObject: ModuleObject) {
                val moduleItemsCallback = getModuleItemsCallback(moduleObject, false)
                if (!moduleItemsCallback.isFromNetwork && !isGroupExpanded(moduleObject)) {
                    repository.getFirstPageModuleItems(courseContext, moduleObject.id, true, moduleItemsCallback)
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
            val courseColor = courseContext.textAndIconColor
            val groupItemCount = getGroupItemCount(moduleObject)
            val itemPosition = storedIndexOfItem(moduleObject, moduleItem)

            (holder as ModuleViewHolder).bind(moduleObject, moduleItem, context, adapterToFragmentCallback, courseColor,
                    itemPosition == 0, itemPosition == groupItemCount - 1)
        }
    }

    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, moduleObject: ModuleObject, isExpanded: Boolean) {
        (holder as ModuleHeaderViewHolder).bind(moduleObject, context, viewHolderHeaderClicked, isExpanded)
    }

    override fun onBindEmptyHolder(holder: RecyclerView.ViewHolder, moduleObject: ModuleObject) {
        val moduleEmptyViewHolder = holder as ModuleEmptyViewHolder
        // Keep displaying No connection as long as the result is not from network
        // Doing so will cause the user to toggle the expand to refresh the list, if they had expanded a module while offline
        if (mModuleItemCallbacks.containsKey(moduleObject.id) && mModuleItemCallbacks[moduleObject.id]!!.isFromNetwork) {
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
        mModuleItemCallbacks.clear()
        checkCourseTabsJob?.cancel()
        collapseAll()
        super.refresh()
    }

    override fun cancel() {
        mModuleItemCallbacks.values.forEach { it.cancel() }
        mModuleObjectCallback?.cancel()
        checkCourseTabsJob?.cancel()
    }

    override fun contextReady() {

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

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.progress_dialog)
        val currentColor = courseContext.textAndIconColor

        (dialog.findViewById<View>(R.id.progressBar) as ProgressBar).indeterminateDrawable.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
        return dialog
    }

    fun updateWithoutResettingViews(moduleObject: ModuleObject) {
        mModuleItemCallbacks.clear()
        mModuleObjectCallback!!.cancel()

        repository.getFirstPageModuleItems(courseContext, moduleObject.id, true, getModuleItemsCallback(moduleObject, false))
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

    private fun getModuleItemsCallback(moduleObject: ModuleObject, isNotifyGroupChange: Boolean): ModuleItemCallback {
        if (mModuleItemCallbacks.containsKey(moduleObject.id)) {
            return mModuleItemCallbacks[moduleObject.id]!!
        } else {
            val moduleItemCallback = object : ModuleItemCallback(moduleObject) {

                private fun checkMasteryPaths(position: Int, item: ModuleItem): Int {
                    var position = position
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
                        addOrUpdateItem(this.moduleObject, masteryPathsLocked)
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
                        addOrUpdateItem(this.moduleObject, masteryPathsSelect)
                        notifyDataSetChanged()
                    }
                    return position
                }

                override fun onResponse(response: Response<List<ModuleItem>>, linkHeaders: LinkHeaders, type: ApiType) {
                    val moduleItems = response.body()
                    if (type === ApiType.API) {
                        var position = if (moduleItems!!.isNotEmpty() && moduleItems[0] != null) moduleItems[0].position - 1 else 0
                        for (item in moduleItems) {
                            item.position = position++
                            addOrUpdateItem(this.moduleObject, item)
                            position = checkMasteryPaths(position, item)
                        }

                        val nextItemsURL = linkHeaders.nextUrl
                        if (nextItemsURL != null) {
                            repository.getNextPageModuleItems(nextItemsURL, true, this)
                        }

                        this.isFromNetwork = true
                        expandGroup(this.moduleObject, isNotifyGroupChange)
                    } else if (type === ApiType.CACHE) {
                        var position = if (moduleItems!!.isNotEmpty() && moduleItems[0] != null) moduleItems[0].position - 1 else 0
                        for (item in moduleItems) {
                            item.position = position++
                            addOrUpdateItem(this.moduleObject, item)
                        }

                        val nextItemsURL = linkHeaders.nextUrl
                        if (nextItemsURL != null) {
                            repository.getNextPageModuleItems(nextItemsURL, true, this)
                        }

                        // Wait for the network to expand when there are no items
                        if (moduleItems.isNotEmpty()) {
                            expandGroup(this.moduleObject, isNotifyGroupChange)
                        }
                    }
                    CollapsedModulesStore.markModuleCollapsed(courseContext, moduleObject.id, false)
                }

                override fun onFail(call: Call<List<ModuleItem>>?, error: Throwable, response: Response<*>?) {

                    // Only expand if there was no cache result and no network. No connection empty cell will be displayed
                    if (response != null
                            && response.code() == 504
                            && APIHelper.isCachedResponse(response)
                            && context != null
                            && !Utils.isNetworkAvailable(context)) {
                        expandGroup(this.moduleObject, isNotifyGroupChange)
                    }
                }
            }

            mModuleItemCallbacks[moduleObject.id] = moduleItemCallback
            return moduleItemCallback
        }
    }

    // region Pagination
    override val isPaginated get() = true

    override fun setupCallbacks() {
        mModuleObjectCallback = object : StatusCallback<List<ModuleObject>>() {

            override fun onResponse(response: Response<List<ModuleObject>>, linkHeaders: LinkHeaders, type: ApiType) {
                val moduleObjects = response.body()
                setNextUrl(linkHeaders.nextUrl)
                val collapsedItems = CollapsedModulesStore.getCollapsedModuleIds(courseContext)
                moduleObjects?.toTypedArray()?.forEach {
                    addOrUpdateGroup(it)
                    if (!collapsedItems.contains(it.id)) {
                        repository.getFirstPageModuleItems(courseContext, it.id, true, getModuleItemsCallback(it, true))
                    }
                }
                if(!shouldExhaustPagination || !this.moreCallsExist()) {
                    // If we should exhaust pagination wait until we are done exhausting pagination
                    adapterToFragmentCallback?.onRefreshFinished()
                }
            }

            override fun onFinished(type: ApiType) {
                this@ModuleListRecyclerAdapter.onCallbackFinished(type)
            }
        }
    }

    override fun loadFirstPage() {
        checkCourseTabsJob = lifecycleScope.tryLaunch {
            val tabs = repository.getTabs(courseContext, isRefresh)

            // We only want to show modules if its a course nav option OR set to as the homepage
            if (tabs.find { it.tabId == "modules" } != null || (courseContext as Course).homePage?.apiString == "modules") {
                if (shouldExhaustPagination) {
                    repository.getAllModuleObjects(courseContext, true, mModuleObjectCallback!!)
                } else {
                    repository.getFirstPageModuleObjects(courseContext, true, mModuleObjectCallback!!)
                }
            } else {
                adapterToFragmentCallback?.onRefreshFinished(true)
            }
        } catch {
            adapterToFragmentCallback?.onRefreshFinished(true)
        }
    }

    override fun loadNextPage(nextURL: String) {
        repository.getNextPageModuleObjects(nextURL, true, mModuleObjectCallback!!)
    }

    // endregion

    // region Module binder Helpers
    private fun isSequentiallyEnabled(moduleObject: ModuleObject, moduleItem: ModuleItem): Boolean {
        // If it's sequential progress and the group is unlocked, the first incomplete one can be viewed
        // if this moduleItem is locked, it should be greyed out unless it is the first one (position == 1 -> it is 1 based, not
        // 0 based) or the previous item is unlocked
        if ((courseContext as Course).isTeacher || courseContext.isTA) {
            return true
        }

        if (moduleObject.sequentialProgress && moduleObject.state != null && (moduleObject.state == ModuleObject.State.Unlocked.apiString || moduleObject.state == ModuleObject.State.Started.apiString)) {

            //group is sequential, need to figure out which ones to grey out
            val indexOfCurrentModuleItem = storedIndexOfItem(moduleObject, moduleItem)
            if (indexOfCurrentModuleItem != -1) {
                // getItem performs invalid index checks
                val previousModuleItem = getItem(moduleObject, indexOfCurrentModuleItem - 1)

                return when {
                    isComplete(moduleItem) -> true
                    previousModuleItem == null -> true // Its the first one in the sequence
                    !isComplete(previousModuleItem) -> false // previous item is not complete
                    else -> isComplete(previousModuleItem) && !isComplete(moduleItem) // Previous complete, so show current as next in sequence
                }
            }
        }
        return true
    }

    private fun isComplete(moduleItem: ModuleItem?): Boolean {
        return moduleItem != null && moduleItem.completionRequirement != null && moduleItem.completionRequirement!!.completed
    }

    // never actually shows prereqs because grayed out module items show instead.
    private fun getPrerequisiteString(moduleObject: ModuleObject): String {
        var prereqString = context.getString(R.string.noItemsToDisplayShort)

        if (ModuleUtility.isGroupLocked(moduleObject)) {
            prereqString = context.getString(R.string.locked)
        }

        if (moduleObject.state != null &&
                moduleObject.state == ModuleObject.State.Locked.apiString &&
                getGroupItemCount(moduleObject) > 0 &&
                getItem(moduleObject, 0)?.type == ModuleObject.State.UnlockRequirements.apiString) {

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

    private abstract class ModuleItemCallback internal constructor(internal val moduleObject: ModuleObject) : StatusCallback<List<ModuleItem>>() {
        internal var isFromNetwork = false // When true, there is no need to fetch objects from the network again.
    }
}
