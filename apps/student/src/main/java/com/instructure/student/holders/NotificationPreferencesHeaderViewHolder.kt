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
package com.instructure.student.holders

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.instructure.student.R
import com.instructure.pandautils.features.notification.preferences.NotificationCategoryHeader
import kotlinx.android.synthetic.main.viewholder_notification_prefs_header.view.*

class NotificationPreferencesHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {
        const val HOLDER_RES_ID = R.layout.viewholder_notification_prefs_header
    }

    fun bind(header: NotificationCategoryHeader) {
        itemView.title.text = header.title
    }
}
