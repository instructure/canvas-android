/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.notification.preferences

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.CommunicationChannelsManager
import com.instructure.canvasapi2.managers.NotificationPreferencesManager
import com.instructure.canvasapi2.models.NotificationPreference
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.notification.preferences.itemviewmodels.NotificationCategoryHeaderItemViewModel
import com.instructure.pandautils.features.notification.preferences.itemviewmodels.NotificationCategoryItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationPreferencesViewModel @Inject constructor(
        private val communicationChannelsManager: CommunicationChannelsManager,
        private val notificationPreferencesManager: NotificationPreferencesManager,
        private val apiPrefs: ApiPrefs,
        private val resources: Resources
) : ViewModel() {
    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<NotificationPreferencesViewData>
        get() = _data
    private val _data = MutableLiveData<NotificationPreferencesViewData>()

    val events: LiveData<Event<NotificationPreferencesAction>>
        get() = _events
    private val _events = MutableLiveData<Event<NotificationPreferencesAction>>()

    init {
        fetchData(false)
    }

    fun refresh() {
        fetchData(true)
    }

    private fun fetchData(forceNetwork: Boolean) {
        viewModelScope.launch {
            apiPrefs.user?.let {
                val communicationChannels = communicationChannelsManager.getCommunicationChannelsAsync(it.id, forceNetwork).await().dataOrThrow
                val pushChannel = communicationChannels.first { "push".equals(it.type, true) }
                val notificationPreferences = notificationPreferencesManager.getNotificationPreferencesAsync(pushChannel.userId, pushChannel.id, forceNetwork).await().dataOrThrow
                val items = groupNotifications(notificationPreferences.notificationPreferences)
                _data.postValue(NotificationPreferencesViewData(items))
            }
        }
    }

    private fun groupNotifications(items: List<NotificationPreference>): List<NotificationCategoryHeaderItemViewModel> {
        val categoryHelperMap = NotificationPreferenceUtils.categoryHelperMap
        val titleMap = NotificationPreferenceUtils.categoryTitleMap
        val descriptionMap = NotificationPreferenceUtils.categoryDescriptionMap
        val groupHeaderMap = NotificationPreferenceUtils.categoryGroupHeaderMap

        val categories = hashMapOf<NotificationCategoryHeaderViewData, ArrayList<NotificationCategoryItemViewModel>>()

        for ((categoryName, prefs) in items.groupBy { it.category }) {
            val categoryHelper = categoryHelperMap[categoryName] ?: continue
            val header = groupHeaderMap[categoryHelper.categoryGroup] ?: continue

            val categoryHeaderViewData = NotificationCategoryHeaderViewData(
                    header.title,
                    header.position
            )

            val categoryItemViewModel = NotificationCategoryItemViewModel(
                    data = NotificationCategoryViewData(
                            categoryName,
                            titleMap[categoryName],
                            descriptionMap[categoryName],
                            prefs[0].frequency,
                            categoryHelper.position,
                            prefs[0].notification
                    )
            )
            if (categories[categoryHeaderViewData] == null) {
                categories[categoryHeaderViewData] = arrayListOf(categoryItemViewModel)
            } else {
                categories[categoryHeaderViewData]?.add(categoryItemViewModel)
            }
        }

        return categories.map {
            NotificationCategoryHeaderItemViewModel(
                    data = it.key,
                    itemViewModels = it.value.sortedBy { it.data.position }
            )
        }.sortedBy { it.data.position }
    }
}