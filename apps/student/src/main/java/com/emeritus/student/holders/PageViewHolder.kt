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

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.utils.ColorKeeper
import com.emeritus.student.R
import com.emeritus.student.interfaces.AdapterToFragmentCallback
import kotlinx.android.synthetic.main.viewholder_page.view.*

class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(
        context: Context,
        page: Page,
        iconColor: Int,
        adapterToFragmentCallback: AdapterToFragmentCallback<Page>
    ) = with(itemView) {
        title.text = page.title

        if (page.frontPage) {
            icon.setImageDrawable(ColorKeeper.getColoredDrawable(context, R.drawable.ic_pages, iconColor))
        } else {
            icon.setImageDrawable(ColorKeeper.getColoredDrawable(context, R.drawable.ic_document, iconColor))
        }

        modified.text = String.format(
            context.getString(R.string.lastModified),
            DateHelper.getFormattedDate(context, page.updatedAt)
        )

        setOnClickListener { adapterToFragmentCallback.onRowClicked(page, adapterPosition, true) }
    }

    companion object {
        const val HOLDER_RES_ID: Int = R.layout.viewholder_page
    }
}
