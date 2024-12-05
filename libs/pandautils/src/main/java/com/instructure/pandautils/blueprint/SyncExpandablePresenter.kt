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

import com.instructure.canvasapi2.models.CanvasComparable
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandarecycler.util.GroupSortedList.*
import com.instructure.pandarecycler.util.Types

abstract class SyncExpandablePresenter<
        GROUP,
        MODEL : CanvasComparable<*>,
        VIEW : SyncExpandableManager<GROUP, MODEL>>(
    groupClass: Class<GROUP>, modelClass: Class<MODEL>
) : Presenter<VIEW> {

    abstract fun loadData(forceNetwork: Boolean)
    abstract fun refresh(forceNetwork: Boolean)

    private var listChangeCallback: ListChangeCallback? = null

    val data: GroupSortedList<GROUP, MODEL>

    var viewCallback: VIEW? = null
        private set

    //region Comparators
    private var visualArrayCallback: VisualArrayCallback = object : VisualArrayCallback() {
        override fun onInserted(position: Int, count: Int) {
            listChangeCallback?.onInserted(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            listChangeCallback?.onRemoved(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            listChangeCallback?.onMoved(fromPosition, toPosition)
        }

        override fun onChanged(position: Int, count: Int) {
            listChangeCallback?.onChanged(position, count)
        }
    }

    var comparatorCallback: GroupComparatorCallback<GROUP> = object : GroupComparatorCallback<GROUP> {
        override fun compare(o1: GROUP, o2: GROUP): Int {
            return this@SyncExpandablePresenter.compare(o1, o2)
        }

        override fun areContentsTheSame(group1: GROUP, group2: GROUP): Boolean {
            return this@SyncExpandablePresenter.areContentsTheSame(group1, group2)
        }

        override fun areItemsTheSame(group1: GROUP, group2: GROUP): Boolean {
            return this@SyncExpandablePresenter.areItemsTheSame(group1, group2)
        }

        override fun getUniqueGroupId(group: GROUP): Long {
            return this@SyncExpandablePresenter.getUniqueGroupId(group)
        }

        override fun getGroupType(group: GROUP): Int {
            return groupType
        }
    }

    private var itemComparatorCallback: ItemComparatorCallback<GROUP, MODEL> = object : ItemComparatorCallback<GROUP, MODEL> {
        override fun compare(group: GROUP, o1: MODEL, o2: MODEL): Int {
            return this@SyncExpandablePresenter.compare(group, o1, o2)
        }

        override fun areContentsTheSame(item1: MODEL, item2: MODEL): Boolean {
            return this@SyncExpandablePresenter.areContentsTheSame(item1, item2)
        }

        override fun areItemsTheSame(item1: MODEL, item2: MODEL): Boolean {
            return this@SyncExpandablePresenter.areItemsTheSame(item1, item2)
        }

        override fun getUniqueItemId(item: MODEL): Long {
            return this@SyncExpandablePresenter.getUniqueItemId(item)
        }

        override fun getChildType(group: GROUP, item: MODEL): Int {
            return childType
        }
    }

    //endregion

    init {
        data = GroupSortedList(
            groupClass,
            modelClass,
            visualArrayCallback,
            comparatorCallback,
            itemComparatorCallback
        )
    }

    override fun onViewAttached(view: VIEW): Presenter<VIEW> {
        viewCallback = view
        return this
    }

    override fun onViewDetached() {
        viewCallback = null
    }

    override fun onDestroyed() {
        viewCallback = null
    }

    val isEmpty: Boolean
        get() = data.size() == 0

    fun clearData() {
        data.clearAll()
        viewCallback?.clearAdapter()

    }

    protected fun onRefreshStarted() {
        viewCallback?.onRefreshStarted()
    }

    open fun compare(group: GROUP, item1: MODEL, item2: MODEL): Int = -1

    open fun compare(group1: GROUP, group2: GROUP): Int = -1

    open fun areContentsTheSame(item1: MODEL, item2: MODEL): Boolean = false

    fun areContentsTheSame(group1: GROUP, group2: GROUP): Boolean = false

    open fun areItemsTheSame(item1: MODEL, item2: MODEL): Boolean = false

    open fun areItemsTheSame(group1: GROUP, group2: GROUP): Boolean = false

    val childType: Int get() = Types.TYPE_ITEM

    val groupType: Int get() = Types.TYPE_HEADER

    open fun getUniqueItemId(item: MODEL): Long = item.hashCode().toLong()

    open fun getUniqueGroupId(group: GROUP): Long = group.hashCode().toLong()

    fun setListChangeCallback(callback: ListChangeCallback?) {
        listChangeCallback = callback
    }

}
