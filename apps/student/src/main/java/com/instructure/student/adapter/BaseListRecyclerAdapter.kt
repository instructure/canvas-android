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
package com.instructure.student.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.instructure.canvasapi2.models.CanvasComparable
import com.instructure.canvasapi2.utils.UnsafeJavaUtils
import com.instructure.pandarecycler.PaginatedRecyclerAdapter
import com.instructure.pandarecycler.util.UpdatableSortedList

abstract class BaseListRecyclerAdapter<MODEL : CanvasComparable<*>, T : RecyclerView.ViewHolder>(
    context: Context,
    klazz: Class<MODEL>,
    initialItems: List<MODEL>? = null
) : PaginatedRecyclerAdapter<T>(context) {

    private val list: UpdatableSortedList<MODEL>

    var itemCallback: ItemComparableCallback<MODEL>

    private var selectedItemId: Long = 0

    abstract class ItemComparableCallback<MDL : CanvasComparable<*>> {
        open fun compare(o1: MDL, o2: MDL): Int = UnsafeJavaUtils.canvasCompare(o1, o2)
        open fun areContentsTheSame(oldItem: MDL, newItem: MDL): Boolean = false
        open fun areItemsTheSame(item1: MDL, item2: MDL): Boolean = item1.id == item2.id
        open fun getUniqueItemId(mdl: MDL): Long = mdl.id
    }

    init {
        itemCallback = object : ItemComparableCallback<MODEL>() {}

        val callback: SortedList.Callback<MODEL> = object : SortedList.Callback<MODEL>() {
            override fun compare(o1: MODEL, o2: MODEL): Int = itemCallback.compare(o1, o2)
            override fun onInserted(position: Int, count: Int) = notifyItemRangeInserted(position, count)
            override fun onRemoved(position: Int, count: Int) = notifyItemRangeRemoved(position, count)
            override fun onMoved(fromPosition: Int, toPosition: Int) = notifyItemMoved(fromPosition, toPosition)
            override fun onChanged(position: Int, count: Int) = notifyItemRangeChanged(position, count)
            override fun areContentsTheSame(old: MODEL, new: MODEL) = itemCallback.areContentsTheSame(old, new)
            override fun areItemsTheSame(item1: MODEL, item2: MODEL) = itemCallback.areItemsTheSame(item1, item2)
        }

        list = UpdatableSortedList(
            klazz,
            callback,
            { model: MODEL -> itemCallback.getUniqueItemId(model) },
            DEFAULT_LIST_SIZE
        )

        @Suppress("LeakingThis") setupCallbacks()

        initialItems?.let { addAll(it) }
    }

    abstract fun bindHolder(model: MODEL, holder: T, position: Int)

    override fun onBindViewHolder(baseHolder: T, position: Int) {
        super.onBindViewHolder(baseHolder, position)
        if (position < list.size()) bindHolder(list[position], baseHolder, position)
    }

    // region Selection
    override fun getSelectedPosition(): Int {
        return list.indexOfItemById(selectedItemId)
    }

    override fun setSelectedPosition(position: Int) {
        if (position == -1) return
        if (selectedItemId != -1L) {
            val oldPosition = list.indexOfItemById(selectedItemId)
            if (oldPosition != UpdatableSortedList.NOT_IN_LIST) {
                notifyItemChanged(oldPosition)
            }
        }
        val item = getItemAtPosition(position) ?: return // Workaround for a IndexOutOfBounds issue.
        selectedItemId = itemCallback.getUniqueItemId(item)
        notifyItemChanged(position)
        super.setSelectedPosition(position)
    }

    override fun setSelectedItemId(selectedItemId: Long) {
        this.selectedItemId = selectedItemId
    }
    // endregion

    fun onCallbackFinished() {
        isLoadedFirstPage = true
        shouldShowLoadingFooter()
        adapterToRecyclerViewCallback.setDisplayNoConnection(false)
        adapterToRecyclerViewCallback.setIsEmpty(isAllPagesLoaded && size() == 0)
    }

    /**
     * The loading footer from pagination will be position == size(). So it'll be index out of bounds.
     * Perform a check before calling getItemAtPosition or make sure isPaginated() returns false
     */
    fun getItemAtPosition(position: Int): MODEL? = if (position >= list.size()) null else list[position]

    fun indexOf(item: MODEL): Int = list.indexOf(item)

    open fun add(item: MODEL) {
        list.addOrUpdate(item)
    }

    fun addAll(items: List<MODEL>) {
        list.beginBatchedUpdates()
        items.forEach { add(it) }
        list.endBatchedUpdates()
    }

    fun addAll(items: Array<MODEL>) {
        addAll(listOf(*items))
    }

    fun remove(item: MODEL) {
        list.remove(item)
    }

    override fun clear() {
        // Remove items at end, to avoid unnecessary ARRAY SHIFTING MADNESS
        while (list.size() > 0) list.removeItemAt(list.size() - 1)
        notifyDataSetChanged()
    }

    override fun size(): Int = list.size()

    companion object {
        private const val DEFAULT_LIST_SIZE = 50 // List will increase in size automatically
    }
}
