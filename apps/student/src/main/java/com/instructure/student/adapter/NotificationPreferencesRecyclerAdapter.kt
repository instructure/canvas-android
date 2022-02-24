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
package com.instructure.student.adapter

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.managers.NotificationPreferencesManager
import com.instructure.canvasapi2.models.CommunicationChannel
import com.instructure.canvasapi2.models.NotificationPreference
import com.instructure.canvasapi2.models.NotificationPreferenceResponse
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandarecycler.util.Types
import com.instructure.pandautils.utils.toast
import com.instructure.student.R
import com.instructure.student.holders.NotificationPreferencesHeaderViewHolder
import com.instructure.student.holders.NotificationPreferencesViewHolder
import com.instructure.pandautils.features.notification.preferences.NotificationCategory
import com.instructure.pandautils.features.notification.preferences.NotificationCategoryHeader
import com.instructure.pandautils.features.notification.preferences.NotificationPreferenceUtils
import java.util.*

class NotificationPreferencesRecyclerAdapter(context: Context) : ExpandableRecyclerAdapter<NotificationCategoryHeader, NotificationCategory, RecyclerView.ViewHolder>(context, NotificationCategoryHeader::class.java, NotificationCategory::class.java ) {

    private lateinit var mCurrentChannel: CommunicationChannel
    private var mApiCall: WeaveJob? = null
    private val mUpdateCalls = WeakHashMap<String, WeaveJob>()

    init {
        isExpandedByDefault = true
    }

    override fun createViewHolder(v: View, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        Types.TYPE_HEADER -> NotificationPreferencesHeaderViewHolder(v)
        else -> NotificationPreferencesViewHolder(v)
    }


    override fun itemLayoutResId(viewType: Int): Int = when (viewType) {
        Types.TYPE_HEADER -> NotificationPreferencesHeaderViewHolder.HOLDER_RES_ID
        else -> NotificationPreferencesViewHolder.HOLDER_RES_ID
    }

    override fun onBindChildHolder(baseHolder: RecyclerView.ViewHolder, notificationCategoryHeader: NotificationCategoryHeader, notificationCategory: NotificationCategory) {
        val holder = baseHolder as NotificationPreferencesViewHolder
        holder.bind(notificationCategory) { category, isChecked ->
            mUpdateCalls[category.name]?.cancel()
            mUpdateCalls[category.name] = tryWeave {
                awaitApi<NotificationPreferenceResponse> {
                    NotificationPreferencesManager.updatePreferenceCategory(category.notification ?: category.name, mCurrentChannel.id, isChecked.frequency, it)
                }
                category.frequency = isChecked.frequency
            } catch {
                category.frequency = isChecked.not().frequency
                notifyDataSetChanged()
            }
        }
    }

    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, notificationCategoryHeader: NotificationCategoryHeader, isExpanded: Boolean) {
        (holder as? NotificationPreferencesHeaderViewHolder)?.bind(notificationCategoryHeader)
    }

    override fun createGroupCallback(): GroupSortedList.GroupComparatorCallback<NotificationCategoryHeader> {
        return object : GroupSortedList.GroupComparatorCallback<NotificationCategoryHeader> {
            override fun compare(o1: NotificationCategoryHeader, o2: NotificationCategoryHeader) = o1.position - o2.position
            override fun areContentsTheSame(oldGroup: NotificationCategoryHeader, newGroup: NotificationCategoryHeader) = false
            override fun areItemsTheSame(group1: NotificationCategoryHeader, group2: NotificationCategoryHeader) = group1.position == group2.position
            override fun getUniqueGroupId(group: NotificationCategoryHeader) = group.position.toLong()
            override fun getGroupType(group: NotificationCategoryHeader) = Types.TYPE_HEADER
        }
    }

    override fun createItemCallback(): GroupSortedList.ItemComparatorCallback<NotificationCategoryHeader, NotificationCategory> {
        return object : GroupSortedList.ItemComparatorCallback<NotificationCategoryHeader, NotificationCategory> {
            override fun compare(group: NotificationCategoryHeader, o1: NotificationCategory, o2: NotificationCategory) = o1.position - o2.position
            override fun areContentsTheSame(oldItem: NotificationCategory, newItem: NotificationCategory) = false
            override fun areItemsTheSame(item1: NotificationCategory, item2: NotificationCategory) = item1.position == item2.position
            override fun getUniqueItemId(item: NotificationCategory) = item.title?.hashCode()?.toLong() ?: -1L
            override fun getChildType(group: NotificationCategoryHeader, item: NotificationCategory) = Types.TYPE_ITEM
        }
    }

    private val Boolean.frequency: String
        get() = if (this) NotificationPreferencesManager.IMMEDIATELY else NotificationPreferencesManager.NEVER

    fun fetchNotificationPreferences(channel: CommunicationChannel) {
        mCurrentChannel = channel
        clear()
        mApiCall?.cancel()
        mApiCall = tryWeave {
            val response = awaitApi<NotificationPreferenceResponse> {
                NotificationPreferencesManager.getNotificationPreferences(channel.userId, channel.id, true, it)
            }
            groupNotifications(response.notificationPreferences)
        } catch {
            context.toast(R.string.errorOccurred)
            (context as? Activity)?.finish()
        }
    }

    private fun groupNotifications(items: List<NotificationPreference>) {
        val categoryHelperMap = NotificationPreferenceUtils.categoryHelperMap
        val titleMap = NotificationPreferenceUtils.categoryTitleMap
        val descriptionMap = NotificationPreferenceUtils.categoryDescriptionMap
        val groupHeaderMap = NotificationPreferenceUtils.categoryGroupHeaderMap

        for ((categoryName, prefs) in items.groupBy { it.category } ) {
            val categoryHelper = categoryHelperMap[categoryName] ?: continue
            val header = groupHeaderMap[categoryHelper.categoryGroup] ?: continue

            val category = NotificationCategory(
                    categoryName,
                    titleMap[categoryName],
                    descriptionMap[categoryName],
                    prefs[0].frequency,
                    categoryHelper.position,
                    prefs[0].notification
            )

            addOrUpdateItem(header, category)
        }
    }

    override fun cancel() {
        mUpdateCalls.values.forEach { it?.cancel() }
        mApiCall?.cancel()
    }

}
