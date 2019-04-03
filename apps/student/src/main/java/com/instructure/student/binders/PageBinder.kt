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

package com.instructure.student.binders

import android.content.Context
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.student.R
import com.instructure.student.holders.PageViewHolder
import com.instructure.student.interfaces.AdapterToFragmentCallback

class PageBinder : BaseBinder() {
    companion object {

        fun bind(
                context: Context,
                holder: PageViewHolder,
                page: Page,
                courseColor: Int,
                adapterToFragmentCallback: AdapterToFragmentCallback<Page>) {

            holder.title.text = page.title

            if (page.frontPage) {
                holder.icon.setImageDrawable(ColorKeeper.getColoredDrawable(context, R.drawable.vd_pages, courseColor))
            } else {
                holder.icon.setImageDrawable(ColorKeeper.getColoredDrawable(context, R.drawable.vd_document, courseColor))
            }

            holder.modifiedDate.text = String.format(context.getString(R.string.lastModified), DateHelper.getFormattedDate(context, page.updatedAt))
            holder.itemView.setOnClickListener { adapterToFragmentCallback.onRowClicked(page, holder.adapterPosition, true) }
        }
    }
}
