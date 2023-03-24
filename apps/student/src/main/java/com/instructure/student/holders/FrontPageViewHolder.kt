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

package com.instructure.student.holders

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Page
import com.instructure.student.R
import com.instructure.student.databinding.AdapterCourseBrowserHomeBinding
import com.instructure.student.interfaces.AdapterToFragmentCallback

class FrontPageViewHolder(view: View) : RecyclerView.ViewHolder(view) {


    companion object {
        const val HOLDER_RES_ID = R.layout.adapter_course_browser_home

        fun bind(context: Context, holder: FrontPageViewHolder, page: Page, adapterToFragmentCallback: AdapterToFragmentCallback<Page>) {
            val binding = AdapterCourseBrowserHomeBinding.bind(holder.itemView)
            binding.homeLabel.text = context.getString(R.string.frontPage)
            binding.homeSubLabel.text = page.title
            holder.itemView.setOnClickListener {
                adapterToFragmentCallback.onRowClicked(page, holder.adapterPosition, true)
            }
        }

    }
}
