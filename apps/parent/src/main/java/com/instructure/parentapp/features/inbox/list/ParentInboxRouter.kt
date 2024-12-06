/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.inbox.list

import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Conversation
import com.instructure.pandautils.features.inbox.list.InboxRouter
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.utils.FileDownloader
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.parentapp.features.inbox.coursepicker.ParentInboxCoursePickerBottomSheetDialog
import com.instructure.parentapp.util.navigation.Navigation


class ParentInboxRouter(
    private val activity: FragmentActivity,
    private val navigation: Navigation,
    private val fileDownloader: FileDownloader
) : InboxRouter {

    override fun openConversation(conversation: Conversation, scope: InboxApi.Scope) {
        navigation.navigate(activity, navigation.inboxDetailsRoute(conversation.id))
    }

    override fun attachNavigationIcon(toolbar: Toolbar) {
        toolbar.setupAsBackButton {
            activity.onBackPressed()
        }
    }

    override fun routeToNewMessage(activity: FragmentActivity) {
        ParentInboxCoursePickerBottomSheetDialog().show(activity.supportFragmentManager, "ParentInboxCoursePickerBottomSheetDialog")
    }

    override fun routeToCompose(options: InboxComposeOptions) {
        val route = navigation.inboxComposeRoute(options)
        navigation.navigate(activity, route)
    }

    override fun avatarClicked(conversation: Conversation, scope: InboxApi.Scope) {
        openConversation(conversation, scope)
    }

    override fun routeToAttachment(attachment: Attachment) {
        fileDownloader.downloadFileToDevice(attachment)
    }
}
