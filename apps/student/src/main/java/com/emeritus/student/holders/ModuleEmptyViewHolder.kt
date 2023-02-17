/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
package com.emeritus.student.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.emeritus.student.R
import kotlinx.android.synthetic.main.viewholder_module_empty.view.*

class ModuleEmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(text: String?) = with(itemView){
        titleText.text = text
    }

    companion object {
        const val HOLDER_RES_ID = R.layout.viewholder_module_empty
    }
}
