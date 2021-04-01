/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.student.binding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.setGone
import com.instructure.student.BR
import com.instructure.student.view.EmptyView

@BindingAdapter("itemViewModels")
fun bindHelpLinks(container: ViewGroup, itemViewModels: List<ItemViewModel>?) {
    itemViewModels?.forEach {
        val binding: ViewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(container.context), it.layoutId, container, false)
        binding.setVariable(BR.itemViewModel, it)
        container.addView(binding.root)
    }
}

@BindingAdapter("emptyViewState")
fun bindEmptyViewState(emptyView: EmptyView, state: ViewState?) {
    when (state) {
        is ViewState.Success -> emptyView.setGone()
        is ViewState.Loading -> emptyView.setLoading()
        is ViewState.Error -> emptyView.setGone() // Currently just set this to gone, we don't need an empty view in the dialog, but need to find a generic solution for this.
    }
}

@BindingAdapter("itemViewModels")
fun bindItemViewModels(recyclerView: RecyclerView, itemViewModels: List<ItemViewModel>?) {
    val adapter = getOrCreateAdapter(recyclerView)
    adapter.updateItems(itemViewModels)
}

private fun getOrCreateAdapter(recyclerView: RecyclerView): BindableRecyclerViewAdapter {
    return if (recyclerView.adapter != null && recyclerView.adapter is BindableRecyclerViewAdapter) {
        recyclerView.adapter as BindableRecyclerViewAdapter
    } else {
        val bindableRecyclerAdapter = BindableRecyclerViewAdapter()
        recyclerView.adapter = bindableRecyclerAdapter
        bindableRecyclerAdapter
    }
}

