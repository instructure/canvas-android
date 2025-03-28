/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.student.features.inbox.list

import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Conversation
import com.instructure.pandautils.features.inbox.compose.InboxComposeFragment
import com.instructure.pandautils.features.inbox.details.InboxDetailsFragment
import com.instructure.pandautils.features.inbox.list.InboxFragment
import com.instructure.pandautils.features.inbox.list.InboxRouter
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.student.activity.NavigationActivity
import com.instructure.student.router.RouteMatcher
import com.instructure.student.router.RouteMatcher.openMedia

class StudentInboxRouter(private val activity: FragmentActivity, private val fragment: Fragment) : InboxRouter {

    override fun openConversation(conversation: Conversation, scope: InboxApi.Scope) {
        val route = InboxDetailsFragment.makeRoute(conversation.id, conversation.workflowState == Conversation.WorkflowState.UNREAD)
        RouteMatcher.route(activity, route)
    }

    override fun attachNavigationIcon(toolbar: Toolbar) {
        if (activity is NavigationActivity && fragment is InboxFragment) {
            activity.attachNavigationDrawer(fragment, toolbar)
        }
    }

    override fun routeToNewMessage(activity: FragmentActivity) {
        val route = InboxComposeFragment.makeRoute(InboxComposeOptions.buildNewMessage())
        RouteMatcher.route(activity, route)
    }

    override fun routeToCompose(options: InboxComposeOptions) {
        val route = InboxComposeFragment.makeRoute(options)
        RouteMatcher.route(activity, route)
    }

    override fun avatarClicked(conversation: Conversation, scope: InboxApi.Scope) {
        openConversation(conversation, scope)
    }

    override fun routeToAttachment(attachment: Attachment) {
        openMedia(activity, attachment.url)
    }

    override fun popDetailsScreen(activity: FragmentActivity?) {
        activity?.onBackPressed()
    }
}