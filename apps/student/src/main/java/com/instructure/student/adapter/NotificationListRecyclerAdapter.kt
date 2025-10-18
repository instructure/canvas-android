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
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.CourseManager.createCourseMap
import com.instructure.canvasapi2.managers.CourseManager.getCoursesWithGradingScheme
import com.instructure.canvasapi2.managers.GroupManager.createGroupMap
import com.instructure.canvasapi2.managers.GroupManager.getAllGroups
import com.instructure.canvasapi2.managers.StreamManager.getCourseStream
import com.instructure.canvasapi2.managers.StreamManager.getUserStream
import com.instructure.canvasapi2.managers.StreamManager.hideStreamItem
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.HiddenStreamItem
import com.instructure.canvasapi2.models.StreamItem
import com.instructure.canvasapi2.utils.APIHelper.hasNetworkConnection
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.pandarecycler.util.GroupSortedList.GroupComparatorCallback
import com.instructure.pandarecycler.util.GroupSortedList.ItemComparatorCallback
import com.instructure.pandarecycler.util.Types
import com.instructure.student.holders.ExpandableViewHolder
import com.instructure.student.holders.NotificationViewHolder
import com.instructure.student.interfaces.NotificationAdapterToFragmentCallback
import retrofit2.Call
import retrofit2.Response
import java.util.Date

class NotificationListRecyclerAdapter(
    context: Context,
    private val canvasContext: CanvasContext,
    private val adapterToFragmentCallback: NotificationAdapterToFragmentCallback<StreamItem>
) : ExpandableRecyclerAdapter<Date, StreamItem, RecyclerView.ViewHolder>(
    context,
    Date::class.java,
    StreamItem::class.java
) {
    private var notificationCheckboxCallback: NotificationCheckboxCallback? = null
    private var streamItems: List<StreamItem>? = null
    private var courseMap: Map<Long, Course>? = null
    private var groupMap: Map<Long, Group>? = null

    private lateinit var streamCallback: StatusCallback<List<StreamItem>>
    private lateinit var coursesCallback: StatusCallback<List<Course>>
    private lateinit var groupsCallback: StatusCallback<List<Group>>

    private val checkedStreamItems = HashSet<StreamItem>()
    private val deletedStreamItems = HashSet<StreamItem>()
    private var isEditMode = false

    // With multiple callbacks, some could fail while others don't. This manages when to display no connection when offline
    private var isNoNetwork = false

    init {
        isExpandedByDefault = true
        loadData()
    }

    interface NotificationCheckboxCallback {
        fun onCheckChanged(streamItem: StreamItem, isChecked: Boolean, position: Int)
        fun isEditMode(): Boolean
    }

    override fun createViewHolder(v: View, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        Types.TYPE_HEADER -> ExpandableViewHolder(v)
        else -> NotificationViewHolder(v)
    }

    override fun itemLayoutResId(viewType: Int): Int = when (viewType) {
        Types.TYPE_HEADER -> ExpandableViewHolder.HOLDER_RES_ID
        else -> NotificationViewHolder.HOLDER_RES_ID
    }

    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, date: Date, streamItem: StreamItem) {
        (holder as NotificationViewHolder).bind(
            context,
            streamItem,
            notificationCheckboxCallback!!,
            adapterToFragmentCallback
        )
    }

    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, date: Date, isExpanded: Boolean) {
        (holder as ExpandableViewHolder).bind(
            context,
            date,
            DateHelper.getFormattedDate(context, date),
            isExpanded,
            viewHolderHeaderClicked
        )
    }

    override val isPaginated get() = true

    override fun loadFirstPage() {
        getCoursesWithGradingScheme(true, coursesCallback)
        getAllGroups(groupsCallback, true)
        if (canvasContext.type == CanvasContext.Type.USER) {
            getUserStream(streamCallback, true)
        } else {
            getCourseStream(canvasContext, streamCallback, true)
        }
    }

    override fun loadNextPage(nextURL: String) {
        if (canvasContext.type === CanvasContext.Type.USER) {
            getUserStream(streamCallback, true)
        } else {
            getCourseStream(canvasContext, streamCallback, true)
        }
    }

    override fun setupCallbacks() {
        notificationCheckboxCallback = object : NotificationCheckboxCallback {
            override fun onCheckChanged(streamItem: StreamItem, isChecked: Boolean, position: Int) {
                streamItem.isChecked = isChecked
                if (isChecked && !deletedStreamItems.contains(streamItem)) {
                    checkedStreamItems.add(streamItem)
                } else {
                    checkedStreamItems.remove(streamItem)
                }

                //If we aren't in the edit mode, enable edit mode for future clicks
                if (!isEditMode) {
                    isEditMode = true
                } else if (checkedStreamItems.size == 0) { // If this was the last item, cancel
                    isEditMode = false
                }
                adapterToFragmentCallback.onShowEditView(checkedStreamItems.size > 0)
                notifyItemChanged(position)
            }

            override fun isEditMode(): Boolean {
                return isEditMode
            }
        }

        coursesCallback = object : StatusCallback<List<Course>>() {
            override fun onResponse(response: Response<List<Course>>, linkHeaders: LinkHeaders, type: ApiType) {
                courseMap = createCourseMap(response.body())
                populateActivityStreamAdapter()
            }
        }

        groupsCallback = object : StatusCallback<List<Group>>() {
            override fun onResponse(response: Response<List<Group>>, linkHeaders: LinkHeaders, type: ApiType) {
                groupMap = createGroupMap(response.body()!!)
                populateActivityStreamAdapter()
            }
        }

        streamCallback = object : StatusCallback<List<StreamItem>>() {
            private fun checkPreviouslyCheckedItems(items: List<StreamItem>) {
                for (item in items) {
                    if (checkedStreamItems.contains(item)) {
                        // Update it do the actual item (the right object reference)
                        checkedStreamItems.remove(item)
                        checkedStreamItems.add(item)
                        item.isChecked = true
                    }
                }
            }

            override fun onResponse(response: Response<List<StreamItem>>, linkHeaders: LinkHeaders, type: ApiType) {
                val streamItems = response.body()!!
                checkPreviouslyCheckedItems(streamItems)
                this@NotificationListRecyclerAdapter.streamItems = streamItems
                populateActivityStreamAdapter()
                adapterToFragmentCallback.onRefreshFinished()

                //Clear out the cached deleted items.
                deletedStreamItems.clear()
                setNextUrl(linkHeaders.nextUrl)
            }

            override fun onFail(call: Call<List<StreamItem>>?, error: Throwable, response: Response<*>?) {
                if (!hasNetworkConnection()) {
                    onNoNetwork()
                } else {
                    adapterToRecyclerViewCallback?.setIsEmpty(true)
                }
            }

            override fun onFinished(type: ApiType) {
                this@NotificationListRecyclerAdapter.onCallbackFinished(type)
            }
        }
    }

    // endregion
    // region Data
    fun confirmButtonClicked() {
        for (streamItem in checkedStreamItems) {
            hideStreamItem(streamItem)
            deletedStreamItems.add(streamItem)
        }
        isEditMode = false
        clearMarked()
    }

    fun cancelButtonClicked() {
        for (streamItem in checkedStreamItems) {
            streamItem.isChecked = false
        }
        isEditMode = false
        clearMarked()
        notifyDataSetChanged()
    }

    private fun clearMarked() {
        checkedStreamItems.clear()
        adapterToFragmentCallback.onShowEditView(false)
    }

    override fun onNoNetwork() {
        super.onNoNetwork()
        isNoNetwork = true
    }

    override fun onCallbackFinished(type: ApiType?) {
        // Workaround for the multiple callbacks, some will succeed while others don't
        isLoadedFirstPage = true
        shouldShowLoadingFooter()
        val adapterToRecyclerViewCallback = adapterToRecyclerViewCallback
        if (adapterToRecyclerViewCallback != null) {
            if (!isNoNetwork) { // Double negative, only happens when there is network
                adapterToRecyclerViewCallback.setDisplayNoConnection(false)
                // We check mStreamItems here as onCallbackFinished is called prior to populating the adapter
                adapterToRecyclerViewCallback.setIsEmpty(isAllPagesLoaded && (streamItems?.isEmpty() ?: true))
            }
        }
    }

    override fun refresh() {
        isNoNetwork = false
        adapterToRecyclerViewCallback?.setDisplayNoConnection(false)
        streamCallback.reset()
        super.refresh()
    }

    private fun populateActivityStreamAdapter() {
        if (isNoNetwork) { // Workaround for the multiple callbacks, which mess up the generic solution
            adapterToRecyclerViewCallback?.setDisplayNoConnection(true)
            adapterToRecyclerViewCallback?.setIsEmpty(size() == 0)
        }

        // Wait until all calls return;
        if (courseMap == null || groupMap == null || streamItems == null) return
        for (streamItem in streamItems!!) {
            // Skip conversation type items from notification list.
            val itemType = streamItem.getStreamItemType()
            if (itemType === StreamItem.Type.CONVERSATION) {
                continue
            }

            streamItem.setCanvasContextFromMap(courseMap!!, groupMap!!)

            // Make sure there's something there
            if (streamItem.updatedDate == null) continue
            addOrUpdateItem(DateHelper.getCleanDate(streamItem.updatedDate!!.time), streamItem)
        }
        streamItems = null
    }

    private fun hideStreamItem(streamItem: StreamItem) {
        hideStreamItem(
            streamItem.id,
            object : StatusCallback<HiddenStreamItem>() {
                override fun onResponse(response: Response<HiddenStreamItem>, linkHeaders: LinkHeaders, type: ApiType) {
                    if (response.body()!!.isHidden) {
                        removeItem(streamItem)
                        adapterToFragmentCallback.onItemRemoved()
                    }
                }

                override fun onFail(call: Call<HiddenStreamItem>?, error: Throwable, response: Response<*>?) {
                    deletedStreamItems.remove(streamItem)
                }
            }
        )
    }

    override fun cancel() {
        streamCallback.cancel()
        coursesCallback.cancel()
        groupsCallback.cancel()
    }
    // endregion

    // region Expandable Callbacks
    override fun createGroupCallback(): GroupComparatorCallback<Date> {
        return object : GroupComparatorCallback<Date> {
            override fun compare(o1: Date, o2: Date): Int = o2.compareTo(o1)
            override fun areContentsTheSame(oldGroup: Date, newGroup: Date): Boolean = oldGroup == newGroup
            override fun areItemsTheSame(group1: Date, group2: Date): Boolean = group1.time == group2.time
            override fun getUniqueGroupId(group: Date): Long = group.time
            override fun getGroupType(group: Date): Int = Types.TYPE_HEADER
        }
    }

    override fun createItemCallback(): ItemComparatorCallback<Date, StreamItem> {
        return object : ItemComparatorCallback<Date, StreamItem> {
            override fun compare(group: Date, o1: StreamItem, o2: StreamItem): Int = o1.compareTo(o2)
            override fun areContentsTheSame(old: StreamItem, new: StreamItem): Boolean {
                return old.getTitle(context) == new.getTitle(context)
            }
            override fun areItemsTheSame(item1: StreamItem, item2: StreamItem): Boolean = item1.id == item2.id
            override fun getUniqueItemId(item: StreamItem): Long = item.id
            override fun getChildType(group: Date, item: StreamItem): Int = Types.TYPE_ITEM
        }
    }
    // endregion
}
