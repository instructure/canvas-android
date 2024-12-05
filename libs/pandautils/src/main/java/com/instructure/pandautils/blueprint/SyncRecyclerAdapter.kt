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

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.instructure.canvasapi2.models.CanvasComparable
import com.instructure.pandarecycler.util.UpdatableSortedList
import java.lang.ref.WeakReference

/**
 * This is a stripped down base for our ListRecyclerAdapters. Most of the coupled functionality
 * was torn out of this and added to BaseListPresenter
 */
abstract class SyncRecyclerAdapter<
        MODEL : CanvasComparable<*>,
        HOLDER : RecyclerView.ViewHolder,
        VIEW : SyncManager<MODEL>>(
    context: Context,
    private val presenter: SyncPresenter<MODEL, VIEW>
) : RecyclerView.Adapter<HOLDER>() {

    abstract fun bindHolder(model: MODEL, holder: HOLDER, position: Int)

    abstract fun createViewHolder(binding: ViewBinding, viewType: Int): HOLDER

    private val contextReference: WeakReference<Context> = WeakReference(context)

    init {
        presenter.setListChangeCallback(object : ListChangeCallback {
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
        notifyDataSetChanged()
    }

    abstract fun bindingInflater(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> ViewBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HOLDER {
        val binding = bindingInflater(viewType)(LayoutInflater.from(context), parent, false)
        return createViewHolder(binding, viewType)
    }

    override fun onBindViewHolder(baseHolder: HOLDER, position: Int) {
        if (position < list.size()) {
            bindHolder(list[position], baseHolder, baseHolder.adapterPosition)
        }
    }

    fun size(): Int = presenter.data.size()

    override fun getItemCount(): Int = size()

    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    // region MODEL Helpers
    /**
     * The loading footer from pagination will be position == size(). So it'll be index out of bounds.
     * Perform a check before calling getItemAtPosition or make sure isPaginated() returns false
     *
     * @param position The position of the item requested
     * @return A model item
     */
    fun getItemAtPosition(position: Int): MODEL? {
        return if (list.size() == 0) null else list[position]
    }

    fun indexOf(item: MODEL): Int = list.indexOf(item)

    fun add(item: MODEL) {
        list.addOrUpdate(item)
    }

    fun addAll(items: List<MODEL>) {
        list.beginBatchedUpdates()
        for (item in items) {
            add(item)
        }
        list.endBatchedUpdates()
    }

    fun removeItemAt(position: Int) {
        list.removeItemAt(position)
    }

    fun remove(item: MODEL) {
        removeItem(item)
    }

    fun removeItem(item: MODEL): Boolean {
        return list.remove(item)
    }

    // endregion
    val list: UpdatableSortedList<MODEL>
        get() = presenter.data

    protected val context: Context?
        get() = contextReference.get()
}
