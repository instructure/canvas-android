/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.AnnouncementManager
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.filterWithQuery
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandarecycler.util.Types
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.toast
import com.instructure.student.R
import com.instructure.student.holders.DiscussionExpandableViewHolder
import com.instructure.student.holders.DiscussionListHolder
import com.instructure.student.holders.EmptyViewHolder
import com.instructure.student.holders.NoViewholder
import com.instructure.student.interfaces.AdapterToFragmentCallback
import kotlinx.coroutines.Job
import retrofit2.Response
import java.util.*

open class DiscussionListRecyclerAdapter(
    context: Context,
    private val canvasContext: CanvasContext,
    private val isDiscussions: Boolean,
    private val callback: AdapterToDiscussionsCallback,
    private val isTesting: Boolean = false
) : ExpandableRecyclerAdapter<String, DiscussionTopicHeader, RecyclerView.ViewHolder>(
    context,
    String::class.java,
    DiscussionTopicHeader::class.java
) {

    private var discussions: List<DiscussionTopicHeader> = emptyList()

    private var discussionsListJob: Job? = null

    var searchQuery = ""
        set(value) {
            field = value
            clear()
            populateData()
        }

    interface AdapterToDiscussionsCallback : AdapterToFragmentCallback<DiscussionTopicHeader>{
        fun discussionOverflow(group: String?, discussionTopicHeader: DiscussionTopicHeader)
        fun askToDeleteDiscussion(discussionTopicHeader: DiscussionTopicHeader)
        fun onRefreshStarted()
    }

    init {
        isExpandedByDefault = true
        if (!isTesting) loadData()
    }

    override fun createViewHolder(v: View, viewType: Int): RecyclerView.ViewHolder =
            if (viewType == Types.TYPE_HEADER) {
                if (isDiscussions) DiscussionExpandableViewHolder(v) else NoViewholder(v)
            } else {
                DiscussionListHolder(v)
            }


    override fun itemLayoutResId(viewType: Int): Int =
            if (viewType == Types.TYPE_HEADER) {
                if (isDiscussions) DiscussionExpandableViewHolder.HOLDER_RES_ID else NoViewholder.HOLDER_RES_ID
            } else {
                DiscussionListHolder.HOLDER_RES_ID
            }


    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, group: String, discussionTopicHeader: DiscussionTopicHeader) {
        context.let { (holder as DiscussionListHolder).bind(it, discussionTopicHeader, ColorKeeper.getOrGenerateColor(canvasContext), isDiscussions, callback) }
    }

    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, group: String, isExpanded: Boolean) {
        if (isDiscussions) {
            (holder as? DiscussionExpandableViewHolder)?.bind(isExpanded, isDiscussions, group) { discussionGroup ->
                expandCollapseGroup(discussionGroup)
            }
        }
    }

    override fun onBindEmptyHolder(holder: RecyclerView.ViewHolder, group: String) {
        (holder as EmptyViewHolder).bind(context.resources.getString(R.string.utils_emptyDiscussions))
    }

    override fun loadData() {
        callback.onRefreshStarted()
        discussionsListJob = tryWeave {
            discussions = awaitApi {
                if (isDiscussions) DiscussionManager.getAllDiscussionTopicHeaders(canvasContext, isRefresh, it)
                else AnnouncementManager.getAllAnnouncements(canvasContext, isRefresh, it)
            }
            populateData()
        } catch {
            callback.onRefreshFinished()
            context.toast(R.string.errorOccurred)
        }
    }

    private fun populateData() {
        val filtered = discussions.filterWithQuery(searchQuery, DiscussionTopicHeader::title)
        if(isDiscussions) {
            addOrUpdateAllItems(PINNED, filtered.filter { getHeaderType(it) == PINNED })
            addOrUpdateAllItems(CLOSED_FOR_COMMENTS, filtered.filter { getHeaderType(it) == CLOSED_FOR_COMMENTS })
            addOrUpdateAllItems(UNPINNED, filtered.filter { getHeaderType(it) == UNPINNED })
        } else {
            addOrUpdateAllItems(ANNOUNCEMENTS, filtered)
        }
        callback.onRefreshFinished()
        onCallbackFinished(ApiType.API)
        adapterToRecyclerViewCallback.setIsEmpty(size() == 0)
    }

    override fun cancel() {
        discussionsListJob?.cancel()
    }

    private fun getHeaderType(discussionTopicHeader: DiscussionTopicHeader): String {
        if(discussionTopicHeader.pinned) return PINNED
        if(discussionTopicHeader.locked) return CLOSED_FOR_COMMENTS
        return UNPINNED
    }

    companion object {
        // Named funny to preserve the order.
        const val PINNED = "1_PINNED"
        const val UNPINNED = "2_UNPINNED"
        const val CLOSED_FOR_COMMENTS = "3_CLOSED_FOR_COMMENTS"
        const val ANNOUNCEMENTS = "ANNOUNCEMENTS"
        const val DELETE = "delete"
    }

    private val mDiscussionTopicHeaderPinnedCallback = object : StatusCallback<DiscussionTopicHeader>() {
        override fun onResponse(response: Response<DiscussionTopicHeader>, linkHeaders: LinkHeaders, type: ApiType) {
            response.body()?.let { addOrUpdateItem(PINNED, it) }
        }
    }

    private val mDiscussionTopicHeaderUnpinnedCallback = object : StatusCallback<DiscussionTopicHeader>() {
        override fun onResponse(response: Response<DiscussionTopicHeader>, linkHeaders: LinkHeaders, type: ApiType) {
            response.body()?.let { addOrUpdateItem(UNPINNED, it) }
        }
    }

    private val mDiscussionTopicHeaderClosedForCommentsCallback = object : StatusCallback<DiscussionTopicHeader>() {
        override fun onResponse(response: Response<DiscussionTopicHeader>, linkHeaders: LinkHeaders, type: ApiType) {
            response.body()?.let { addOrUpdateItem(CLOSED_FOR_COMMENTS, it) }
        }
    }

    private val mDiscussionTopicHeaderOpenedForCommentsCallback = object : StatusCallback<DiscussionTopicHeader>() {
        override fun onResponse(response: Response<DiscussionTopicHeader>, linkHeaders: LinkHeaders, type: ApiType) {
            response.body()?.let { addOrUpdateItem(if(it.pinned) PINNED else UNPINNED, it) }
        }
    }

    fun requestMoveDiscussionTopicToGroup(groupTo: String, groupFrom: String, discussionTopicHeader: DiscussionTopicHeader) {
        // Move from this group into another
        when(groupFrom) {
            PINNED -> {
                when(groupTo) {
                    UNPINNED -> DiscussionManager.unpinDiscussionTopicHeader(canvasContext, discussionTopicHeader.id, mDiscussionTopicHeaderUnpinnedCallback)
                    CLOSED_FOR_COMMENTS -> DiscussionManager.lockDiscussionTopicHeader(canvasContext, discussionTopicHeader.id, mDiscussionTopicHeaderClosedForCommentsCallback)
                    DELETE -> { callback.askToDeleteDiscussion(discussionTopicHeader) }
                }
            }
            UNPINNED -> {
                when(groupTo) {
                    PINNED -> DiscussionManager.pinDiscussionTopicHeader(canvasContext, discussionTopicHeader.id, mDiscussionTopicHeaderPinnedCallback)
                    CLOSED_FOR_COMMENTS -> DiscussionManager.lockDiscussionTopicHeader(canvasContext, discussionTopicHeader.id, mDiscussionTopicHeaderClosedForCommentsCallback)
                    DELETE -> { callback.askToDeleteDiscussion(discussionTopicHeader) }
                }
            }
            CLOSED_FOR_COMMENTS -> {
                when(groupTo) {
                    PINNED -> DiscussionManager.pinDiscussionTopicHeader(canvasContext, discussionTopicHeader.id, mDiscussionTopicHeaderPinnedCallback)
                    CLOSED_FOR_COMMENTS -> DiscussionManager.unlockDiscussionTopicHeader(canvasContext, discussionTopicHeader.id, mDiscussionTopicHeaderOpenedForCommentsCallback)
                    DELETE -> { callback.askToDeleteDiscussion(discussionTopicHeader) }
                }
            }
            DELETE -> { callback.askToDeleteDiscussion(discussionTopicHeader) }
        }
    }

    fun deleteDiscussionTopicHeader(discussionTopicHeader: DiscussionTopicHeader) {
        DiscussionManager.deleteDiscussionTopicHeader(canvasContext, discussionTopicHeader.id, object : StatusCallback<Void>() {
            override fun onResponse(response: Response<Void>, linkHeaders: LinkHeaders, type: ApiType) {
                removeItem(discussionTopicHeader, false)
            }
        })
    }

    override fun createGroupCallback(): GroupSortedList.GroupComparatorCallback<String> {
        return object : GroupSortedList.GroupComparatorCallback<String> {
            override fun compare(group1: String, group2: String): Int {
                if (group1 == null || group2 == null) return -1
                return group1.compareTo(group2)
            }

            override fun areContentsTheSame(oldGroup: String, newGroup: String): Boolean = oldGroup == newGroup
            override fun areItemsTheSame(group1: String, group2: String): Boolean = group1 == group2
            override fun getUniqueGroupId(group: String): Long = group.hashCode().toLong()
            override fun getGroupType(group: String): Int = Types.TYPE_HEADER
        }
    }

    override fun createItemCallback(): GroupSortedList.ItemComparatorCallback<String, DiscussionTopicHeader> {
        return object : GroupSortedList.ItemComparatorCallback<String, DiscussionTopicHeader> {
            override fun compare(group: String, item1: DiscussionTopicHeader, item2: DiscussionTopicHeader): Int =
                    if(PINNED == group) {
                        item1.position.compareTo(item2.position)
                    } else {
                        if(isDiscussions) item2.lastReplyDate?.compareTo(item1.lastReplyDate ?: Date(0)) ?: -1
                        else -1
                    }

            override fun areContentsTheSame(item1: DiscussionTopicHeader, item2: DiscussionTopicHeader): Boolean = item1.title == item2.title && item1.status == item2.status
            override fun areItemsTheSame(item1: DiscussionTopicHeader, item2: DiscussionTopicHeader): Boolean = item1.id == item2.id
            override fun getUniqueItemId(discussionTopicHeader: DiscussionTopicHeader): Long = discussionTopicHeader.id
            override fun getChildType(group: String, item: DiscussionTopicHeader): Int = Types.TYPE_ITEM
        }
    }
}
