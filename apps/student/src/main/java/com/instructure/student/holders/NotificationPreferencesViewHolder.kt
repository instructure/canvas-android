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

import android.view.View
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.managers.NotificationPreferencesManager
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.pandautils.features.notification.preferences.NotificationCategory
import kotlinx.android.synthetic.main.viewholder_notification_preference.view.*

class NotificationPreferencesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    init {
        ViewStyler.themeSwitch(itemView.context, itemView.toggle, ThemePrefs.brandColor)
    }

    fun bind(item: NotificationCategory, callback: (NotificationCategory, Boolean) -> Unit) = with(itemView) {
        title.text = item.title
        description.setVisible(item.description != null).text = item.description
        toggle.isChecked = !item.frequency.equals(NotificationPreferencesManager.NEVER, ignoreCase = true)
        toggle.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@OnCheckedChangeListener
            callback(item, isChecked)
        })
    }

    companion object {
        const val HOLDER_RES_ID: Int = R.layout.viewholder_notification_preference
    }
}
