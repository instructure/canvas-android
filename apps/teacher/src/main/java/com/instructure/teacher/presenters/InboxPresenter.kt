/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */
package com.instructure.teacher.presenters

import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.managers.InboxManager
import com.instructure.canvasapi2.managers.UnreadCountManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.UnreadConversationCount
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.canvasapi2.utils.weave.weavePaginated
import com.instructure.teacher.viewinterface.InboxView
import instructure.androidblueprint.SyncPresenter

class InboxPresenter : SyncPresenter<Conversation, InboxView>(Conversation::class.java) {

    var scope: InboxApi.Scope = InboxApi.Scope.ALL
        set(scope) {
            field = scope
            refresh(true)
        }

    var canvasContext: CanvasContext? = null
    var apiCall: WeaveJob? = null
    var unreadCountCall: WeaveJob? = null

    private var isLoading = false

    override fun loadData(forceNetwork: Boolean) {
        if ((data.size() > 0 || isLoading) && !forceNetwork) return

        isLoading = true
        viewCallback?.onRefreshStarted()
        apiCall = weavePaginated<List<Conversation>> {
            onRequest { callback ->
                viewCallback?.onRefreshStarted()
                canvasContext?.let { InboxManager.getConversationsFiltered(scope, it.contextId, forceNetwork, callback) }
                        ?: InboxManager.getConversations(scope, forceNetwork, callback)
            }
            onResponse { response ->
                isLoading = false
                data.addOrUpdate(response)
                viewCallback?.onRefreshFinished()
                viewCallback?.checkIfEmpty()
            }
            onError {
                isLoading = false
            }
        }

        unreadCountCall = tryWeave {
            val inboxUnreadCount = awaitApi<UnreadConversationCount> { UnreadCountManager.getUnreadConversationCount(it, true) }
            val unreadCountInt = (inboxUnreadCount.unreadCount ?: "0").toInt()
            viewCallback?.unreadCountUpdated(unreadCountInt)
        } catch {}
    }

    override fun refresh(forceNetwork: Boolean) {
        apiCall?.cancel()
        unreadCountCall?.cancel()
        clearData()
        loadData(forceNetwork)
    }

    fun nextPage() = apiCall?.next()

    public override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return if (containsNull(oldItem.lastMessagePreview, newItem.lastMessagePreview) || oldItem.workflowState != newItem.workflowState
                || oldItem.isStarred != newItem.isStarred) {
            false
        } else oldItem.lastMessagePreview == newItem.lastMessagePreview
    }

    public override fun areItemsTheSame(item1: Conversation, item2: Conversation) = item1.id == item2.id

    // We don't want to sort the items locally, but we do need id comparison for item updates
    override fun compare(item1: Conversation, item2: Conversation) = if (item1.id == item2.id) 0 else -1

    private fun containsNull(oldItem: Any?, newItem: Any?) = oldItem == null || newItem == null
}
