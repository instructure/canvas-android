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
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.AccountNotificationManager
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.fragment.InternalWebviewFragment
import com.instructure.student.interfaces.CourseAdapterToFragmentCallback
import com.instructure.student.router.RouteMatcher
import kotlinx.android.synthetic.main.viewholder_announcement_card.view.*

class AnnouncementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {
        const val HOLDER_RES_ID: Int = R.layout.viewholder_announcement_card
    }

    fun bind(
            announcement: AccountNotification,
            callback: CourseAdapterToFragmentCallback
    ) = with(itemView) {
        val color = when (announcement.icon) {
            AccountNotification.ACCOUNT_NOTIFICATION_ERROR -> ContextCompat.getColor(context, R.color.notificationTintError)
            AccountNotification.ACCOUNT_NOTIFICATION_WARNING -> ContextCompat.getColor(context, R.color.notificationTintWarning)
            else -> ThemePrefs.brandColor
        }

        val icon = when (announcement.icon) {
            AccountNotification.ACCOUNT_NOTIFICATION_ERROR,
            AccountNotification.ACCOUNT_NOTIFICATION_WARNING -> R.drawable.ic_warning
            AccountNotification.ACCOUNT_NOTIFICATION_CALENDAR -> R.drawable.ic_calendar_announcement
            AccountNotification.ACCOUNT_NOTIFICATION_QUESTION -> R.drawable.ic_question_mark
            else -> R.drawable.ic_info
        }

        announcementIcon.setImageResource(icon)
        DrawableCompat.setTint(DrawableCompat.wrap(background), color)
        DrawableCompat.setTint(DrawableCompat.wrap(announcementIconView.background), color)

        announcementTitle.text = announcement.subject

        fun refresh() {
            val isExpanded = false
            announcementTitle.setSingleLine(!isExpanded)
            tapToView.setVisible(!isExpanded)
            dismissImageButton.setVisible(!isExpanded)
        }

        fun dismiss() {
            // Fire and forget
            AccountNotificationManager.deleteAccountNotification(announcement.id, object : StatusCallback<AccountNotification>(){})
            callback.onRemoveAnnouncement(announcement, adapterPosition)
        }

        onClick {
            RouteMatcher.route(context, InternalWebviewFragment.makeRoute("", announcement.subject, false, announcement.message))
        }

        dismissImageButton.onClickWithRequireNetwork { dismiss() }
        refresh()
    }

}
