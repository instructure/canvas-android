/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.binding

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.instructure.pandautils.BR
import com.instructure.pandautils.mvvm.ItemViewModel

class BindableSpinnerAdapter(
        context: Context,
        @LayoutRes private val viewResource: Int,
        private var itemViewModels: List<ItemViewModel>
) : ArrayAdapter<ItemViewModel>(context, viewResource, itemViewModels) {

        fun setCourses(itemViewModels: List<ItemViewModel>) {
                this.itemViewModels = itemViewModels
                notifyDataSetChanged()
        }

        override fun getCount(): Int = itemViewModels.size

        @SuppressLint("InflateParams")
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val binding: ViewDataBinding = DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context), viewResource, parent, false
                )
                binding.setVariable(BR.itemViewModel, itemViewModels[position])
                return binding.root
        }

        @SuppressLint("InflateParams")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val binding: ViewDataBinding = DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context), viewResource, parent, false
                )
                binding.setVariable(BR.itemViewModel, itemViewModels[position])
                return binding.root
        }
}