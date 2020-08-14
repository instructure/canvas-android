/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.student.adapter

import android.content.Context
import android.view.View
import com.instructure.student.holders.InboxViewHolder
import com.instructure.student.interfaces.AdapterToFragmentCallback
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.managers.InboxManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.weavePaginated

class InboxAdapter(
    context: Context,
    private val mAdapterCallback: AdapterToFragmentCallback<Conversation>
) : BaseListRecyclerAdapter<Conversation, InboxViewHolder>(
    context,
    Conversation::class.java,
    emptyList()
) {

    var scope: InboxApi.Scope = InboxApi.Scope.ALL
        set(scope) {
            field = scope
            refresh()
        }

    var canvasContext: CanvasContext? = null
    private var apiCall: WeaveJob? = null
    private val userId = ApiPrefs.user?.id ?: 0

    init {
        itemCallback = object : BaseListRecyclerAdapter.ItemComparableCallback<Conversation>() {
            // Don't sort the data since the API already gives us the correct order
            override fun compare(o1: Conversation, o2: Conversation) = if (o1.id == o2.id) 0 else -1

            override fun areItemsTheSame(item1: Conversation, item2: Conversation) = item1.id == item2.id
            override fun getUniqueItemId(conversation: Conversation) = conversation.id
            override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
                return if (containsNull(oldItem.lastMessagePreview, newItem.lastMessagePreview)
                    || oldItem.workflowState != newItem.workflowState) {
                    false
                } else oldItem.lastMessagePreview == newItem.lastMessagePreview
            }
        }
        loadData()
    }

    private fun containsNull(oldItem: Any?, newItem: Any?) = oldItem == null || newItem == null

    override fun bindHolder(conversation: Conversation, viewHolder: InboxViewHolder, position: Int) {
        viewHolder.bind(conversation, userId, mAdapterCallback)
    }

    override fun createViewHolder(v: View, viewType: Int): InboxViewHolder {
        return InboxViewHolder(v)
    }

    override fun itemLayoutResId(viewType: Int) = InboxViewHolder.HOLDER_RES_ID

    override fun contextReady() {
        setupCallbacks()
    }

    override fun setupCallbacks() = Unit

    override val isPaginated get() = true

    override fun resetData() {
        apiCall?.cancel()
        super.resetData()
    }

    override fun loadFirstPage() {
        apiCall = weavePaginated<List<Conversation>> {
            onRequest { callback ->
                canvasContext?.let {
                    InboxManager.getConversationsFiltered(scope, it.contextId, isRefresh, callback)
                } ?: InboxManager.getConversations(scope, isRefresh, callback)
            }
            onResponse { response ->
                setNextUrl("")
                addAll(response)
                notifyDataSetChanged()
                mAdapterCallback.onRefreshFinished()
            }
            onError { }
            onComplete {
                setNextUrl(null)
                adapterToRecyclerViewCallback.setIsEmpty(size() == 0)
            }
        }
    }

    override fun loadNextPage(nextURL: String) {
        apiCall?.next()
    }

}
