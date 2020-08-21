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

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PaginatedScrollListener(
    private var threshold: Int = 15,
    private val onLoad: () -> Unit
) : RecyclerView.OnScrollListener() {

    private var previousTotal = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0
    private var firstVisibleItem = 0
    private var isLoading = false

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {}

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        visibleItemCount = recyclerView.childCount
        totalItemCount = recyclerView.layoutManager?.itemCount ?: 0
        firstVisibleItem = (recyclerView.layoutManager as? LinearLayoutManager?)?.findFirstVisibleItemPosition() ?: 0
        if (isLoading) {
            if (totalItemCount > previousTotal) {
                isLoading = false
                previousTotal = totalItemCount
            }
        }
        if (!isLoading && totalItemCount - visibleItemCount <= firstVisibleItem + threshold) {
            if (firstVisibleItem != RecyclerView.NO_POSITION) {
                onLoad()
                Log.v("scroll", "end called")
                isLoading = true
            }
        }
    }

    fun resetScroll() {
        previousTotal = 0
        visibleItemCount = 0
        totalItemCount = 0
        firstVisibleItem = 0
    }
}
