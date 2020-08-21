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
package com.instructure.pandarecycler

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.pandarecycler.BaseRecyclerAdapter.AdapterToRecyclerViewCallback
import com.instructure.pandarecycler.interfaces.EmptyViewInterface

class PandaRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    private var emptyView: EmptyViewInterface? = null

    private var isEmpty = false

    var isSelectionEnabled = false

    private var baseRecyclerAdapter: BaseRecyclerAdapter<*>? = null

    // When swipe to refresh, keeps the empty view from showing
    private var isRefresh = false

    private val mAdapterToRecyclerViewCallback: AdapterToRecyclerViewCallback = object : AdapterToRecyclerViewCallback {
        override fun setIsEmpty(flag: Boolean) {
            isEmpty = flag
            isRefresh = false
            checkIfEmpty()
        }

        override fun setDisplayNoConnection(isNoConnection: Boolean) {
            emptyView?.setDisplayNoConnection(isNoConnection)
            setIsEmpty(isNoConnection)
        }

        override fun refresh() {
            isRefresh = true
            reset()
        }
    }

    private val mPaginatedScrollListener = PaginatedScrollListener { baseRecyclerAdapter?.loadData() }

    init {
        addOnItemTouchListener(
            RecyclerItemClickListener(context) { _, position ->
                if (isSelectionEnabled) (adapter as BaseRecyclerAdapter<*>?)?.setSelectedPosition(position)
            }
        )
    }

    /**
     * The Observer is located in the Recyclerview, because checkIfEmpty sets the visibility
     */
    private val observer: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            checkIfEmpty()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            isRefresh = false
            checkIfEmpty()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            checkIfEmpty()
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        val oldAdapter = getAdapter()
        oldAdapter?.unregisterAdapterDataObserver(observer)
        super.setAdapter(adapter)
        baseRecyclerAdapter = adapter as BaseRecyclerAdapter<*>?
        baseRecyclerAdapter?.let {
            reset()
            it.registerAdapterDataObserver(observer)
            it.adapterToRecyclerViewCallback = mAdapterToRecyclerViewCallback
            if (it.isPaginated) {
                addOnScrollListener(mPaginatedScrollListener)
            }
        }
        checkIfEmpty()
    }

    private fun reset() {
        if (baseRecyclerAdapter?.isPaginated == true) mPaginatedScrollListener.resetScroll()
    }

    fun setEmptyView(emptyView: EmptyViewInterface?) {
        this.emptyView = emptyView
        checkIfEmpty()
    }

    private fun checkIfEmpty() {
        val emptyView = emptyView ?: return
        val adapter = baseRecyclerAdapter ?: return
        if (adapter.size() == 0 && !isRefresh) {
            this.visibility = View.GONE
            emptyView.setVisibility(View.VISIBLE)
            if (isEmpty) {
                emptyView.setListEmpty()
            } else {
                emptyView.setLoading()
            }
        } else {
            this.visibility = View.VISIBLE
            emptyView.setVisibility(View.GONE)
        }
    }
}
