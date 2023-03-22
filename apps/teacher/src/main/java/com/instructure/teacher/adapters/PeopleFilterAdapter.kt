/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.teacher.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.teacher.databinding.PeopleFilterAdapterItemBinding
import com.instructure.teacher.holders.PeopleListFilterViewHolder

class PeopleFilterAdapter(
    private val canvasContexts: ArrayList<CanvasContext>,
    private val canvasContextIdList: ArrayList<Long>,
    private val mCanvasContextCallback: (canvasContext: CanvasContext, isChecked: Boolean) -> Unit
) : RecyclerView.Adapter<PeopleListFilterViewHolder>() {

    private lateinit var binding: PeopleFilterAdapterItemBinding

    override fun onBindViewHolder(holder: PeopleListFilterViewHolder, position: Int) {
        holder.bind(canvasContexts[position], canvasContextIdList, mCanvasContextCallback, binding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleListFilterViewHolder {
        binding = PeopleFilterAdapterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PeopleListFilterViewHolder(binding.root)
    }

    override fun getItemCount(): Int = canvasContexts.size
}
