/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.student.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.student.holders.CanvasContextViewHolder

class CanvasContextDialogAdapter(
    private val canvasContexts: List<CanvasContext>,
    private val callback: (canvasContext: CanvasContext) -> Unit
) : RecyclerView.Adapter<CanvasContextViewHolder>() {

    override fun onBindViewHolder(holder: CanvasContextViewHolder, position: Int) {
        holder.bind(canvasContexts[position], callback)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CanvasContextViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(CanvasContextViewHolder.HOLDER_RES_ID, parent, false)
        return CanvasContextViewHolder(itemView)
    }

    override fun getItemCount(): Int = canvasContexts.size

}
