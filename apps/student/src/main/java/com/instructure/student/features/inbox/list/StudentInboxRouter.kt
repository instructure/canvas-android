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
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.Conversation
import com.instructure.pandautils.features.inbox.list.InboxRouter
import com.instructure.student.activity.NavigationActivity
import com.instructure.student.fragment.InboxConversationFragment
import com.instructure.student.router.RouteMatcher

class StudentInboxRouter(private val activity: FragmentActivity) : InboxRouter {

    override fun openConversation(conversation: Conversation, scope: InboxApi.Scope) {
        val route = InboxConversationFragment.makeRoute(conversation, InboxApi.conversationScopeToString(scope))
        RouteMatcher.route(activity, route)
    }

    override fun attachNavigationIcon(toolbar: Toolbar) {
        if (activity is NavigationActivity) {
            activity.attachNavigationIcon(toolbar)
        }
    }
}