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
@file:Suppress("unused")

package com.instructure.teacher.adapters

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.instructure.pandarecycler.util.GroupSortedList
import java.util.*

abstract class GroupedRecyclerAdapter<T : Any, C : ListItemCallback>(
    context: Context,
    klazz: Class<T>,
    val callback: C
) : RecyclerView.Adapter<ViewHolder>() {

    val registeredBinders: MutableMap<Class<out T>, ListItemBinder<out T, C>> = mutableMapOf()

    inline fun <reified I : T, reified B : ListItemBinder<I, C>> register(binder: B) {
        binder.viewType = registeredBinders[I::class.java]?.viewType ?: registeredBinders.size
        registeredBinders[I::class.java] = binder
    }

    abstract fun registerBinders()

    /**
     * The comparator used to compare items of different subtypes
     */
    open val baseComparator: Comparator<T> = Comparator { _, _ -> -1 }

    private val sortedList: GroupSortedList<T, T> // Manages all the objects in the list

    /**
     * See [GroupSortedList.getGroups]
     */
    val groups: ArrayList<T>?
        get() = sortedList.groups

    /**
     * See [GroupSortedList.getGroupCount]
     */
    val groupCount: Int
        get() = sortedList.groupCount

    /**
     * See [GroupSortedList.setExpandedByDefault]
     */
    var isExpandedByDefault: Boolean
        get() = sortedList.isExpandedByDefault
        set(isExpandedByDefault) {
            sortedList.isExpandedByDefault = isExpandedByDefault
        }

    init {
        @Suppress("LeakingThis")
        registerBinders()

        val groupCallback = object : GroupSortedList.GroupComparatorCallback<T> {
            override fun compare(o1: T, o2: T): Int {
                return if (o1::class.java == o2::class.java) {
                    getBinder(o1).comparator.compare(o1, o2)
                } else {
                    baseComparator.compare(o1, o2)
                }
            }

            override fun areContentsTheSame(oldGroup: T, newGroup: T) = oldGroup == newGroup
            override fun areItemsTheSame(group1: T, group2: T) = group1 == group2
            override fun getUniqueGroupId(group: T) = getBinder(group).getItemId(group)
            override fun getGroupType(group: T) = 0
        }

        val itemCallback = object : GroupSortedList.ItemComparatorCallback<T, T> {
            override fun compare(group: T, o1: T, o2: T): Int {
                return if (o1::class.java == o2::class.java) {
                    getBinder(o1).comparator.compare(o1, o2)
                } else {
                    baseComparator.compare(o1, o2)
                }
            }

            override fun areContentsTheSame(oldItem: T, newItem: T) = oldItem == newItem
            override fun areItemsTheSame(item1: T, item2: T) = item1 == item2
            override fun getUniqueItemId(item: T) = getBinder(item).getItemId(item)
            override fun getChildType(group: T, item: T) = 0
        }

        sortedList = GroupSortedList(klazz, klazz, object : GroupSortedList.VisualArrayCallback() {
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
        }, groupCallback, itemCallback)

        // Workaround for a11y bug that causes TalkBack to skip items after expand/collapse
        val a11yManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = a11yManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN)
        if (enabledServices?.isNotEmpty() == true) sortedList.disallowCollapse = true
    }

    fun clear() {
        sortedList.clearAll()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return registeredBinders.values.first { it.viewType == viewType }.createViewHolder(parent.context, parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (sortedList.isVisualGroupPosition(position)) {
            val group = sortedList.getGroup(position)
            performBind(group, holder, sortedList.isGroupExpanded(group))
        } else {
            performBind(sortedList.getItem(position)!!, holder, true)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <I : T> getBinder(item: I): ListItemBinder<I, C> =
        registeredBinders[item::class.java] as? ListItemBinder<I, C>
            ?: throw IllegalStateException("No binder registered for ${item::class.java.name}")

    private fun performBind(item: T, holder: ViewHolder, isExpanded: Boolean) {
        val binder = getBinder(item)
        when (val behavior = binder.bindBehavior) {
            is ListItemBinder.Item -> behavior.onBind(item, holder.itemView, callback)
            is ListItemBinder.Header -> {
                holder.itemView.setOnClickListener {
                    sortedList.expandCollapseGroup(item, true)
                    behavior.onExpand(item, isGroupExpanded(item), callback)
                }
                behavior.onBind(item, holder.itemView, isExpanded, callback)
            }
            is ListItemBinder.NoBind -> Unit // Do nothing
            else -> throw IllegalArgumentException("Unknown bind behavior: ${binder.bindBehavior::class.java.name}")
        }
    }

    override fun getItemCount() = sortedList.size()

    override fun getItemViewType(position: Int): Int {
        val item = if (sortedList.isVisualGroupPosition(position)) {
            sortedList.getGroup(position)
        } else {
            sortedList.getItem(position)!!
        }
        return getBinder(item).viewType
    }

    // region helpers

    /**
     * See [GroupSortedList.addOrUpdateAllItems]
     */
    fun addOrUpdateAllItems(group: T, items: List<T>) {
        sortedList.addOrUpdateAllItems(group, items)
    }

    /**
     * See [GroupSortedList.addOrUpdateAllItems]
     */
    fun addOrUpdateAllItems(group: T, items: Array<T>) {
        sortedList.addOrUpdateAllItems(group, items)
    }

    /**
     * See [GroupSortedList.addOrUpdateItem]
     */
    fun addOrUpdateItem(group: T, item: T) {
        sortedList.addOrUpdateItem(group, item)
    }

    /**
     * See [GroupSortedList.removeItem]
     */
    fun removeItem(item: T): Boolean {
        return sortedList.removeItem(item)
    }

    /**
     * See [GroupSortedList.removeItem]
     */
    fun removeItem(item: T, removeGroupIfEmpty: Boolean): Boolean {
        return sortedList.removeItem(item, removeGroupIfEmpty)
    }

    /**
     * See [GroupSortedList.getItem]
     */
    fun getItem(group: T, storedPosition: Int): T? {
        return sortedList.getItem(group, storedPosition)
    }

    /**
     * See [GroupSortedList.getItem]
     */
    fun getItem(visualPosition: Int): T? {
        return sortedList.getItem(visualPosition)
    }

    /**
     * See [GroupSortedList.getItemId]
     */
    fun getChildItemId(position: Int): Long {
        return sortedList.getItemId(sortedList.getItem(position)!!)
    }

    override fun getItemId(position: Int): Long {
        throw UnsupportedOperationException("Method getItemId() is unimplemented in BaseExpandableRecyclerAdapter. Use getChildItemId instead.")
    }

    /**
     * See [GroupSortedList.getItems]
     */
    fun getItems(group: T): ArrayList<T>? {
        return sortedList.getItems(group)
    }

    /**
     * See [GroupSortedList.storedIndexOfItem]
     */
    fun storedIndexOfItem(group: T, item: T): Int {
        return sortedList.storedIndexOfItem(group, item)
    }

    /**
     * See [GroupSortedList.addOrUpdateAllGroups]
     */
    fun addOrUpdateAllGroups(groups: Array<T>) {
        sortedList.addOrUpdateAllGroups(groups)
    }

    /**
     * See [GroupSortedList.addOrUpdateGroup]
     */
    fun addOrUpdateGroup(group: T) {
        sortedList.addOrUpdateGroup(group)
    }

    /**
     * See [GroupSortedList.getGroup]
     */
    fun getGroup(groupId: Long): T? {
        return sortedList.getGroup(groupId)
    }

    /**
     * See [GroupSortedList.getGroup]
     */
    fun getGroup(position: Int): T {
        return sortedList.getGroup(position)
    }

    /**
     * See [GroupSortedList.getGroupItemCount]
     */
    fun getGroupItemCount(group: T): Int {
        return sortedList.getGroupItemCount(group)
    }

    /**
     * See [GroupSortedList.expandCollapseGroup]
     */
    fun expandCollapseGroup(group: T) {
        sortedList.expandCollapseGroup(group)
    }

    /**
     * See [GroupSortedList.collapseAll]
     */
    fun collapseAll() {
        sortedList.collapseAll()
    }

    /**
     * See [GroupSortedList.expandAll]
     */
    fun expandAll() {
        sortedList.expandAll()
    }

    /**
     * See [GroupSortedList.clearExpanded]
     */
    fun clearExpanded() {
        sortedList.clearExpanded()
    }

    /**
     * See [GroupSortedList.markExpanded]
     */
    fun markExpanded(groupIds: Set<Long>, isExpanded: Boolean) {
        sortedList.markExpanded(groupIds, isExpanded)
    }

    /**
     * See [GroupSortedList.expandGroup]
     */
    fun expandGroup(group: T) {
        sortedList.expandGroup(group)
    }

    /**
     * See [GroupSortedList.expandGroup]
     */
    fun expandGroup(group: T, isNotifyGroupChange: Boolean) {
        sortedList.expandGroup(group, isNotifyGroupChange)
    }

    /**
     * See [GroupSortedList.collapseGroup]
     */
    fun collapseGroup(group: T) {
        sortedList.collapseGroup(group)
    }

    /**
     * See [GroupSortedList.collapseGroup]
     */
    fun collapseGroup(group: T, isNotifyGroupChange: Boolean) {
        sortedList.collapseGroup(group, isNotifyGroupChange)
    }

    /**
     * See [GroupSortedList.isGroupExpanded]
     */
    fun isGroupExpanded(group: T): Boolean {
        return sortedList.isGroupExpanded(group)
    }

    /**
     * See [GroupSortedList.getGroupVisualPosition]
     */
    fun getGroupVisualPosition(position: Int): Int {
        return sortedList.getGroupVisualPosition(position)
    }

    /**
     * See [GroupSortedList.getItemVisualPosition]
     */
    fun getItemVisualPosition(itemId: Long): Int = sortedList.getItemVisualPosition(itemId)

    /**
     * See [GroupSortedList.isVisualGroupPosition]
     */
    fun isPositionGroupHeader(position: Int): Boolean {
        return sortedList.isVisualGroupPosition(position)
    }

}

abstract class ListItemBinder<T : Any, C : ListItemCallback> {

    var viewType: Int = 0

    @get:LayoutRes
    abstract val layoutResId: Int

    /**
     * Returns an ID for the specified item. By default this returns a negative value that is assigned to
     * this [ListItemBinder] when registered with an adapter. In most cases this should be overridden to provide a
     * unique ID value for the given item, but the default behavior can safely be used for items that are expected to
     * appear no more than once in the list (e.g. an inline loading indicator or error view).
     */
    open fun getItemId(item: T): Long = -viewType.toLong()

    fun createViewHolder(context: Context, parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(context).inflate(layoutResId, parent, false)
        initView(view)
        return object : ViewHolder(view) {}
    }

    open fun initView(view: View) = Unit

    open val comparator: Comparator<T> = Comparator { _, _ -> -1 }

    abstract val bindBehavior: BindBehavior<T, C>

    // region BindBehavior

    /**
     * A base class for defining bind behavior. Ideally this would be a sealed class, but due to generics we instead use
     * inner classes to aid in type inference.
     */
    open class BindBehavior<T, C> internal constructor()

    inner class NoBind : BindBehavior<T, C>()

    inner class Item(
        val onBind: (item: T, view: View, callback: C) -> Unit
    ) : BindBehavior<T, C>()

    inner class Header(
        val collapsible: Boolean = true,
        val collapsedByDefault: Boolean = false,
        val onExpand: (item: T, isExpanded: Boolean, callback: C) -> Unit = { _, _, _ -> },
        val onBind: (item: T, view: View, isCollapsed: Boolean, callback: C) -> Unit
    ) : BindBehavior<T, C>()

    // endregion

}

interface ListItemCallback
