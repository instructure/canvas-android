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
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.instructure.pandarecycler.interfaces.PaginatedLoadingFooterRecyclerAdapterInterface
import com.instructure.pandarecycler.interfaces.PaginatedRecyclerAdapterInterface
import instructure.com.pandarecycler.R

abstract class PaginatedRecyclerAdapter<VIEW_HOLDER : RecyclerView.ViewHolder>(context: Context) :
    BaseRecyclerAdapter<VIEW_HOLDER>(context), PaginatedRecyclerAdapterInterface,
    PaginatedLoadingFooterRecyclerAdapterInterface<VIEW_HOLDER> {
    var isLoadedFirstPage = false

    private var shouldLoadNextPage = false // If the user scrolls to the end of the first page really fast, nextUrl is null. When true nextUrl will be loaded when its set

    var isAllPagesLoaded = false
        set(value) {
            field = value
            isRefresh = false
        }

    var isRefresh = false

    private var nextUrl: String? = null

    init {
        @Suppress("LeakingThis")
        setupCallbacks()
    }

    override fun loadData() {
        if (!isLoadedFirstPage) {
            loadFirstPage()
        } else if (nextUrl != null) {
            loadNextPage(nextUrl!!)
            nextUrl = null
        } else {
            // The previous page has not loaded yet, when it does, setNextUrl will load the next page
            shouldLoadNextPage = true
        }
    }

    // region Footer
    override fun onBindViewHolder(baseHolder: VIEW_HOLDER, position: Int) {
        if (isLoadingFooterPosition(position)) {
            onBindLoadingFooterViewHolder(baseHolder, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VIEW_HOLDER {
        return if (viewType == LOADING_FOOTER_TYPE) {
            onCreateLoadingFooterViewHolder(parent)
        } else {
            super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun getItemCount(): Int {
        // + 1 for the loading footer
        return if (shouldShowLoadingFooter()) size() + 1 else size()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateLoadingFooterViewHolder(parent: ViewGroup): VIEW_HOLDER {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_loading, parent, false)
        return object : RecyclerView.ViewHolder(view) {} as VIEW_HOLDER
    }

    override fun onBindLoadingFooterViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
    override fun getItemViewType(position: Int): Int {
        return if (isLoadingFooterPosition(position)) {
            LOADING_FOOTER_TYPE
        } else {
            super.getItemViewType(position)
        }
    }

    /**
     * Override for custom logic of showing the loading footer
     */
    fun shouldShowLoadingFooter(): Boolean {
        // Show the loader after first page is loaded to avoid first page insertion animation
        return !isAllPagesLoaded && size() > 0 && isPaginated
    }

    fun isLoadingFooterPosition(position: Int): Boolean {
        // List size is offset by one therefore last position = size
        return size() == position && isPaginated
    }
    // endregion

    // region Pagination
    override fun loadFirstPage() {
        if (isPaginated) throw UnsupportedOperationException("Method must be overridden since isPaginated() is true")
    }

    override fun loadNextPage(nextURL: String) {
        if (isPaginated) throw UnsupportedOperationException("Method must be overridden since isPaginated() is true")
    }

    override fun setupCallbacks() {}

    /**
     * The nextUrl to load
     *
     * Set nextUrl to null when all pages are loaded.
     */
    fun setNextUrl(nextUrl: String?) {
        this.nextUrl = nextUrl
        isLoadedFirstPage = true
        if (nextUrl != null && shouldLoadNextPage) {
            loadNextPage(nextUrl)
            shouldLoadNextPage =
                false // The previous page has not loaded yet, when it does, setNextUrl will load the next page
        } else if (nextUrl == null) {
            isAllPagesLoaded = true
            isRefresh = false
            if (!shouldShowLoadingFooter()) {
                notifyItemRemoved(size())
            }
        }
    }

    override fun refresh() {
        super.refresh()
        resetData()
        isRefresh = true
        loadData()
    }

    fun silentRefresh() {
        nextUrl = null
        resetBooleans()
        isRefresh = false // We don't care about fresh data here, just want to update the offline state
        loadData()
    }

    override fun resetData() {
        clear()
        nextUrl = null
        resetBooleans()
    }
    // endregion

    private fun resetBooleans() {
        isLoadedFirstPage = false
        isAllPagesLoaded = false
        shouldLoadNextPage = false
    }

    companion object {
        // Loading footer appears as a spinner at the bottom
        const val LOADING_FOOTER_TYPE = 1222233
    }
}
