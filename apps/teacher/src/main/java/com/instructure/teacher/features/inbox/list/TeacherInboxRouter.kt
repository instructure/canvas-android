/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.teacher.features.inbox.list

import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.router.Route
import com.instructure.pandautils.features.inbox.compose.InboxComposeFragment
import com.instructure.pandautils.features.inbox.details.InboxDetailsFragment
import com.instructure.pandautils.features.inbox.list.InboxFragment
import com.instructure.pandautils.features.inbox.list.InboxRouter
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.teacher.R
import com.instructure.teacher.activities.InitActivity
import com.instructure.teacher.adapters.StudentContextFragment
import com.instructure.teacher.events.ConversationDeletedEvent
import com.instructure.teacher.events.ConversationUpdatedEvent
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.router.RouteMatcher.openMedia
import com.instructure.teacher.utils.setupBackButtonAsBackPressedOnly
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TeacherInboxRouter(private val activity: FragmentActivity, private val fragment: Fragment) : InboxRouter {

    override fun openConversation(conversation: Conversation, scope: InboxApi.Scope) {
        val route = InboxDetailsFragment.makeRoute(conversation.id)
        RouteMatcher.route(activity, route)
    }

    override fun attachNavigationIcon(toolbar: Toolbar) {
        if (activity is InitActivity) {
            activity.attachNavigationDrawer()
            activity.attachToolbar(toolbar)
        } else {
            toolbar.setupBackButtonAsBackPressedOnly(fragment)
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
        val canvasContext = CanvasContext.fromContextCode(conversation.contextCode)
        val isAvatarClickable = conversation.participants.size == 1 || conversation.participants.size == 2
        if (canvasContext is Course && isAvatarClickable) {
            val userId = if (conversation.participants.size == 1) {
                conversation.participants.first().id
            } else {
                conversation.participants.first { it.id != ApiPrefs.user?.id }.id
            }
            val bundle = StudentContextFragment.makeBundle(userId, canvasContext.id, false)
            RouteMatcher.route(activity, Route(StudentContextFragment::class.java, null, bundle))
        } else {
            openConversation(conversation, scope)
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onConversationUpdated(event: ConversationUpdatedEvent) {
        event.once(javaClass.simpleName) {
            if (fragment is InboxFragment) {
                fragment.conversationUpdated()
            }
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onConversationDeleted(event: ConversationDeletedEvent) {
        event.once(javaClass.simpleName) {
            if (fragment is InboxFragment) {
                fragment.conversationUpdated()
            }

            //pop current detail fragment if tablet
            if (activity.resources.getBoolean(R.bool.isDeviceTablet)) {
                val fragmentManager = fragment.parentFragmentManager
                val currentFrag = fragmentManager.findFragmentById(R.id.detail)
                if (currentFrag != null) {
                    val transaction = fragmentManager.beginTransaction()
                    transaction.remove(currentFrag)
                    transaction.commit()
                }
            }
        }
    }

    override fun routeToAttachment(attachment: Attachment) {
        openMedia(activity, attachment.url)
    }
}