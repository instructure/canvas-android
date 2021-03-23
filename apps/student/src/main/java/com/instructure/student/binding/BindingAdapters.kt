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
import com.instructure.pandautils.mvvm.SubViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.setGone
import com.instructure.student.BR
import com.instructure.student.view.EmptyView

@BindingAdapter("subViewModels")
fun bindHelpLinks(container: ViewGroup, subViewModels: List<SubViewModel>?) {
    subViewModels?.forEach {
        val binding: ViewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(container.context), it.layoutId, container, false)
        binding.setVariable(BR.subViewModel, it)
        container.addView(binding.root)
    }
}

@BindingAdapter("emptyViewState")
fun bindEmptyViewState(emptyView: EmptyView, state: ViewState?) {
    when (state) {
        is ViewState.Success -> emptyView.setGone()
        is ViewState.Loading -> emptyView.setLoading()
    }
}