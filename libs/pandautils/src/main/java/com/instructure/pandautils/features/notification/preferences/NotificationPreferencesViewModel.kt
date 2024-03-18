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
import com.instructure.canvasapi2.managers.NotificationPreferencesFrequency
import com.instructure.canvasapi2.managers.NotificationPreferencesManager
import com.instructure.canvasapi2.models.CommunicationChannel
import com.instructure.canvasapi2.models.NotificationPreference
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.features.notification.preferences.itemviewmodels.NotificationCategoryHeaderItemViewModel
import com.instructure.pandautils.features.notification.preferences.itemviewmodels.NotificationCategoryItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import kotlinx.coroutines.launch

abstract class NotificationPreferencesViewModel (
        private val communicationChannelsManager: CommunicationChannelsManager,
        protected val notificationPreferencesManager: NotificationPreferencesManager,
        private val apiPrefs: ApiPrefs,
        private val notificationPreferenceUtils: NotificationPreferenceUtils,
        protected val resources: Resources
) : ViewModel() {
    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<NotificationPreferencesViewData>
        get() = _data
    protected val _data = MutableLiveData<NotificationPreferencesViewData>()

    val events: LiveData<Event<NotificationPreferencesAction>>
        get() = _events
    protected val _events = MutableLiveData<Event<NotificationPreferencesAction>>()

    protected var communicationChannel: CommunicationChannel? = null

    abstract val notificationChannelType: String

    init {
        _state.postValue(ViewState.Loading)
        fetchData()
    }

    fun refresh() {
        _state.postValue(ViewState.Refresh)
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            try {
                apiPrefs.user?.let {
                    val communicationChannels = communicationChannelsManager.getCommunicationChannelsAsync(it.id, true).await().dataOrThrow
                    communicationChannel = communicationChannels.first { notificationChannelType.equals(it.type, true) }
                    communicationChannel?.let { channel ->

                        val notificationPreferences = notificationPreferencesManager.getNotificationPreferencesAsync(channel.userId, channel.id, true).await().dataOrThrow
                        val items = groupNotifications(notificationPreferences.notificationPreferences)

                        if (items.isEmpty()) {
                            _state.postValue(ViewState.Empty(emptyTitle = R.string.no_notifications_to_show, emptyImage = R.drawable.ic_panda_noalerts))
                        } else {
                            _data.postValue(NotificationPreferencesViewData(items))
                            _state.postValue(ViewState.Success)
                        }
                    } ?: throw IllegalStateException()
                } ?: throw  IllegalStateException()
            } catch (e: Exception) {
                e.printStackTrace()
                _state.postValue(ViewState.Error(resources.getString(R.string.errorOccurred)))
            }
        }
    }

    private fun groupNotifications(items: List<NotificationPreference>): List<NotificationCategoryHeaderItemViewModel> {
        val categoryHelperMap = notificationPreferenceUtils.categoryHelperMap
        val titleMap = notificationPreferenceUtils.categoryTitleMap
        val descriptionMap = notificationPreferenceUtils.categoryDescriptionMap
        val groupHeaderMap = notificationPreferenceUtils.categoryGroupHeaderMap

        val categories = hashMapOf<NotificationCategoryHeaderViewData, ArrayList<NotificationCategoryItemViewModel>>()

        for ((categoryName, prefs) in items.filterNotificationPreferences().groupBy { it.category }) {
            val categoryHelper = categoryHelperMap[categoryName] ?: continue
            val header = groupHeaderMap[categoryHelper.categoryGroup] ?: continue

            val categoryItemViewModel = createCategoryItemViewModel(
                    NotificationCategoryViewData(
                            categoryName,
                            titleMap[categoryName],
                            descriptionMap[categoryName],
                            NotificationPreferencesFrequency.fromApiString(prefs[0].frequency),
                            categoryHelper.position,
                            prefs[0].notification
                    )
            )
            if (categories[header] == null) {
                categories[header] = arrayListOf(categoryItemViewModel)
            } else {
                categories[header]?.add(categoryItemViewModel)
            }
        }

        return categories.map {
            NotificationCategoryHeaderItemViewModel(
                    data = it.key,
                    itemViewModels = it.value.sortedBy { it.data.position }
            )
        }.sortedBy { it.data.position }
    }

    abstract fun createCategoryItemViewModel(viewData: NotificationCategoryViewData): NotificationCategoryItemViewModel

    open fun List<NotificationPreference>.filterNotificationPreferences(): List<NotificationPreference> = this
}