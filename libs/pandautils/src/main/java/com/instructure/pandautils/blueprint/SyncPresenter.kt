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

import androidx.recyclerview.widget.SortedList
import com.instructure.canvasapi2.models.CanvasComparable
import com.instructure.pandarecycler.util.UpdatableSortedList

abstract class SyncPresenter<
        MODEL : CanvasComparable<*>,
        VIEW : SyncManager<MODEL>>(clazz: Class<MODEL>) :
    Presenter<VIEW> {

    private var listChangeCallback: ListChangeCallback? = null

    abstract fun loadData(forceNetwork: Boolean)

    abstract fun refresh(forceNetwork: Boolean)

    val data: UpdatableSortedList<MODEL>

    var viewCallback: VIEW? = null
        private set

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

    fun clearData() {
        data.clear()
        viewCallback?.clearAdapter()
    }

    val isEmpty: Boolean
        get() = data.size() == 0

    protected fun onRefreshStarted() {
        viewCallback?.onRefreshStarted()
    }

    open fun compare(item1: MODEL, item2: MODEL): Int {
        return -1
    }

    open fun areContentsTheSame(item1: MODEL, item2: MODEL): Boolean {
        return false
    }

    open fun areItemsTheSame(item1: MODEL, item2: MODEL): Boolean {
        return item1.id == item2.id
    }

    fun setListChangeCallback(callback: com.instructure.pandautils.blueprint.ListChangeCallback?) {
        listChangeCallback = callback
    }

    init {
        data = UpdatableSortedList(clazz, object : SortedList.Callback<MODEL>() {
            override fun compare(o1: MODEL, o2: MODEL): Int {
                return this@SyncPresenter.compare(o1, o2)
            }

            override fun onInserted(position: Int, count: Int) {
                if (listChangeCallback != null) {
                    listChangeCallback!!.onInserted(position, count)
                }
            }

            override fun onRemoved(position: Int, count: Int) {
                if (listChangeCallback != null) {
                    listChangeCallback!!.onRemoved(position, count)
                }
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                if (listChangeCallback != null) {
                    listChangeCallback!!.onMoved(fromPosition, toPosition)
                }
            }

            override fun onChanged(position: Int, count: Int) {
                if (listChangeCallback != null) {
                    listChangeCallback!!.onChanged(position, count)
                }
            }

            override fun areContentsTheSame(item1: MODEL, item2: MODEL): Boolean {
                return this@SyncPresenter.areContentsTheSame(item1, item2)
            }

            override fun areItemsTheSame(item1: MODEL, item2: MODEL): Boolean {
                return this@SyncPresenter.areItemsTheSame(item1, item2)
            }
        }, { model -> model.id })
    }
}
