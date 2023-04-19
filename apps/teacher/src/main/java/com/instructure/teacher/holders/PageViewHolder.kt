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

package com.instructure.teacher.holders

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.databinding.AdapterPageBinding

class PageViewHolder(private val binding: AdapterPageBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(context: Context, page: Page, iconColor: Int, callback: (Page) -> Unit) = with(binding) {
        pageLayout.setOnClickListener { callback(page) }
        pageTitle.text = page.title
        pageIcon.setIcon(R.drawable.ic_pages, iconColor)
        pageIcon.setPublishedStatus(page.published)
        publishedBar.visibility = if (page.published) View.VISIBLE else View.INVISIBLE

        if (page.updatedAt != null) {
            updatedDate.text = context.getString(R.string.updated, DateHelper.getMonthDayAtTime(context, page.createdAt, context.getString(R.string.at)))
        } else {
            updatedDate.setGone()
        }

        statusIndicator.setVisible(page.frontPage)

        // set the content description on the container so we can tell the user that it is published as the last piece of information. When a content description is on a container
        pageLayout.contentDescription = page.title + " " + updatedDate.text + " " + if (page.published) context.getString(R.string.published) else context.getString(R.string.not_published)
    }
}
