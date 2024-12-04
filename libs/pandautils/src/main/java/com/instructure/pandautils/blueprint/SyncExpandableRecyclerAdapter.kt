/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.pandautils.blueprint

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.instructure.canvasapi2.models.CanvasComparable
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandarecycler.util.Types
import java.lang.ref.WeakReference

abstract class SyncExpandableRecyclerAdapter<
        GROUP,
        MODEL : CanvasComparable<*>,
        HOLDER : RecyclerView.ViewHolder,
        VIEW : SyncExpandableManager<GROUP, MODEL>>(
    context: Context,
    presenter: SyncExpandablePresenter<GROUP, MODEL, VIEW>
) : RecyclerView.Adapter<HOLDER>() {

    abstract fun createViewHolder(binding: ViewBinding, viewType: Int): HOLDER

    abstract fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, group: GROUP, isExpanded: Boolean)

    abstract fun onBindChildHolder(holder: RecyclerView.ViewHolder, group: GROUP, item: MODEL)

    private fun onBindEmptyHolder(holder: RecyclerView.ViewHolder, group: GROUP) {}

    private val contextReference: WeakReference<Context>?

    private val presenter: SyncExpandablePresenter<GROUP, MODEL, VIEW>

    init {
        contextReference = WeakReference(context)
        this.presenter = presenter
        setExpandedByDefault(expandByDefault())
        this.presenter.setListChangeCallback(object : ListChangeCallback {
            override fun onInserted(position: Int, count: Int) {
                notifyItemRangeInserted(position, count)
            }

            override fun onRemoved(position: Int, count: Int) {
                notifyItemRangeRemoved(position, count)
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                notifyItemMoved(fromPosition, toPosition)
            }

            override fun onChanged(position: Int, count: Int) {
                notifyItemRangeChanged(position, count)
            }
        })
        list.isDisplayEmptyCell = showEmptyCells()

        // Workaround for a11y bug that causes TalkBack to skip items after expand/collapse
        val a11yManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val isTalkBackEnabled =
            !a11yManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN)
                .isEmpty()
        if (isTalkBackEnabled) list.disallowCollapse = true
        notifyDataSetChanged()
    }

    abstract fun bindingInflater(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> ViewBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HOLDER {
        val binding = bindingInflater(viewType)(LayoutInflater.from(context), parent, false)
        return createViewHolder(binding, viewType)
    }

    override fun onBindViewHolder(baseHolder: HOLDER, position: Int) {
        val group = list.getGroup(position)
        val list = list
        if (list.isVisualEmptyItemPosition(position)) {
            onBindEmptyHolder(baseHolder, group)
        } else if (list.isVisualGroupPosition(position)) {
            onBindHeaderHolder(baseHolder, group, list.isGroupExpanded(group))
        } else {
            onBindChildHolder(baseHolder, group, list.getItem(position)!!)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (list.isVisualEmptyItemPosition(position)) {
            Types.TYPE_EMPTY_CELL
        } else list.getItemViewType(position)
    }

    fun size(): Int = presenter.data.size()

    override fun getItemCount(): Int = size()

    // region GROUP, MODEL Helpers
    val list: GroupSortedList<GROUP, MODEL>
        get() = presenter.data

    fun addOrUpdateAllItems(group: GROUP, items: List<MODEL>) {
        list.addOrUpdateAllItems(group, items)
    }

    fun addOrUpdateAllItems(group: GROUP, items: Array<MODEL>) {
        list.addOrUpdateAllItems(group, items)
    }

    fun addOrUpdateItem(group: GROUP, item: MODEL) {
        list.addOrUpdateItem(group, item)
    }

    /**
     * Uses as last resort, if you have the group you will save a lot of looping by using [addOrUpdateItem(GROUP, MODEL)]
     *
     * @param item A model item
     */
    fun addOrUpdateItem(item: MODEL) {
        val groups = groups
        for (group in groups!!) {
            for (model in getItems(group)!!) {
                if (model.id == item.id) {
                    addOrUpdateItem(group, item)
                    return
                }
            }
        }
    }

    fun removeItem(item: MODEL): Boolean {
        return list.removeItem(item)
    }

    fun removeItem(item: MODEL, removeGroupIfEmpty: Boolean): Boolean {
        return list.removeItem(item, removeGroupIfEmpty)
    }

    fun getItem(group: GROUP, storedPosition: Int): MODEL? {
        return list.getItem(group, storedPosition)
    }

    fun getItem(visualPosition: Int): MODEL? {
        return list.getItem(visualPosition)
    }

    /**
     * Uses as last resort, if you have the group you will save a lot of looping
     *
     * @param itemId A model itemId
     */
    fun getItem(itemId: Long): MODEL? {
        val groups = groups
        for (group in groups!!) {
            for (model in getItems(group)!!) {
                if (model.id == itemId) {
                    return model
                }
            }
        }
        return null
    }

    fun getChildItemId(position: Int): Long {
        return list.getItemId(list.getItem(position)!!)
    }

    override fun getItemId(position: Int): Long {
        throw UnsupportedOperationException("Method getItemId() is unimplemented in BaseExpandableRecyclerAdapter. Use getChildItemId instead.")
    }

    private fun getItems(group: GROUP): ArrayList<MODEL>? {
        return list.getItems(group)
    }

    fun storedIndexOfItem(group: GROUP, item: MODEL): Int {
        return list.storedIndexOfItem(group, item)
    }

    fun addOrUpdateAllGroups(groups: Array<GROUP>) {
        list.addOrUpdateAllGroups(groups)
    }

    fun addOrUpdateGroup(group: GROUP) {
        list.addOrUpdateGroup(group)
    }

    fun getGroup(groupId: Long): GROUP? {
        return list.getGroup(groupId)
    }

    fun getGroup(position: Int): GROUP {
        return list.getGroup(position)
    }

    val groups: ArrayList<GROUP>?
        get() = list.groups

    val groupCount: Int
        get() = list.groupCount

    fun getGroupItemCount(group: GROUP): Int {
        return list.getGroupItemCount(group)
    }

    fun expandCollapseGroup(group: GROUP) {
        list.expandCollapseGroup(group)
    }

    fun collapseAll() {
        list.collapseAll()
    }

    fun expandAll() {
        list.expandAll()
    }

    fun expandGroup(group: GROUP) {
        list.expandGroup(group)
    }

    fun expandGroup(group: GROUP, isNotifyGroupChange: Boolean) {
        list.expandGroup(group, isNotifyGroupChange)
    }

    fun collapseGroup(group: GROUP) {
        list.collapseGroup(group)
    }

    fun collapseGroup(group: GROUP, isNotifyGroupChange: Boolean) {
        list.collapseGroup(group, isNotifyGroupChange)
    }

    fun isGroupExpanded(group: GROUP): Boolean {
        return list.isGroupExpanded(group)
    }

    fun getGroupVisualPosition(position: Int): Int {
        return list.getGroupVisualPosition(position)
    }

    fun isPositionGroupHeader(position: Int): Boolean {
        return list.isVisualGroupPosition(position)
    }

    fun clear() {
        list.clearAll()
        notifyDataSetChanged()
    }

    //endregion
    protected val context: Context?
        get() = contextReference?.get()

    protected fun setExpandedByDefault(isExpandedByDefault: Boolean) {
        list.isExpandedByDefault = isExpandedByDefault
    }

    private fun expandByDefault(): Boolean = true

    /*
     * Override to allow empty cells (views) to be shown within groups
     */
    private fun showEmptyCells(): Boolean = false
}
