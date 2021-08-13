/*
 * Copyright (C) 2021 - present Instructure, Inc.
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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.instructure.pandautils.BR
import com.instructure.pandautils.mvvm.ItemViewModel

open class BindableRecyclerViewAdapter : RecyclerView.Adapter<BindableViewHolder>() {

    var itemViewModels: MutableList<ItemViewModel> = mutableListOf()
    private val viewTypeLayoutMap: MutableMap<Int, Int> = mutableMapOf()

    private val groupObserver = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            if (sender is GroupItemViewModel && propertyId == BR.collapsed) {
                toggleGroup(sender)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder {
        val binding: ViewDataBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), viewTypeLayoutMap[viewType]
                ?: 0, parent, false
        )

        return BindableViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        val item = itemViewModels[position]
        if (!viewTypeLayoutMap.containsKey(item.viewType)) {
            viewTypeLayoutMap[item.viewType] = item.layoutId
        }
        return item.viewType
    }

    override fun getItemCount(): Int = itemViewModels.size

    override fun onBindViewHolder(holder: BindableViewHolder, position: Int) {
        holder.bind(itemViewModels[position])
    }

    fun updateItems(items: List<ItemViewModel>?) {
        itemViewModels = items.orEmpty().toMutableList()
        val groups = itemViewModels.filterIsInstance<GroupItemViewModel>()
        setupGroups(groups)
        notifyDataSetChanged()
    }

    private fun setupGroups(groups: List<GroupItemViewModel>) {
        groups.forEach {
            it.removeOnPropertyChangedCallback(groupObserver)
            it.addOnPropertyChangedCallback(groupObserver)
            if (!it.collapsed) {
                itemViewModels.addAll(itemViewModels.indexOf(it) + 1, it.items)
                notifyItemRangeInserted(itemViewModels.indexOf(it) + 1, it.items.size)
                setupGroups(it.items.filterIsInstance<GroupItemViewModel>())
            }
        }
    }

    private fun toggleGroup(group: GroupItemViewModel) {
        val position = itemViewModels.indexOf(group)
        if (group.collapsed) {
            itemViewModels.removeAll(group.items)
            notifyItemRangeRemoved(position + 1, group.items.size)
        } else {
            itemViewModels.addAll(position + 1, group.items)
            setupGroups(group.items.filterIsInstance<GroupItemViewModel>())
            notifyItemRangeInserted(position + 1, group.items.size)
        }
    }

}

class BindableViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(itemViewModel: ItemViewModel) {
        binding.setVariable(BR.itemViewModel, itemViewModel)
    }
}