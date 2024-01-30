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
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerAdapter<T : RecyclerView.ViewHolder>(var context: Context) : RecyclerView.Adapter<T>() {
    abstract fun createViewHolder(v: View, viewType: Int): T
    abstract fun itemLayoutResId(viewType: Int): Int
    abstract fun loadData()
    abstract fun size(): Int
    abstract fun clear()
    abstract fun setSelectedItemId(itemId: Long)
    open fun contextReady() {}
    private var selectedPosition = -1

    open fun getSelectedPosition(): Int = selectedPosition

    open fun setSelectedPosition(position: Int) {
        selectedPosition = position
    }

    interface AdapterToRecyclerViewCallback {
        fun setIsEmpty(flag: Boolean)
        fun setDisplayNoConnection(isNoConnection: Boolean)
        fun refresh()
    }

    var adapterToRecyclerViewCallback: AdapterToRecyclerViewCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): T {
        context = parent.context
        contextReady()
        val v = LayoutInflater.from(parent.context).inflate(itemLayoutResId(viewType), parent, false)
        return createViewHolder(v, viewType)
    }

    open val isPaginated: Boolean get() = false

    open fun refresh() {
        adapterToRecyclerViewCallback?.refresh()
    }

    open fun cancel() {}
}
