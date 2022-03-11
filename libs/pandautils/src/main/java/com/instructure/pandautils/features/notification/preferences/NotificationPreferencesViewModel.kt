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
import com.instructure.canvasapi2.models.CommunicationChannel
import com.instructure.canvasapi2.models.NotificationPreference
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.BR
import com.instructure.pandautils.R
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
        private val notificationPreferenceUtils: NotificationPreferenceUtils,
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

    private var pushChannel: CommunicationChannel? = null

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
                    pushChannel = communicationChannels.first { "push".equals(it.type, true) }
                    pushChannel?.let { channel ->

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

        for ((categoryName, prefs) in items.groupBy { it.category }) {
            val categoryHelper = categoryHelperMap[categoryName] ?: continue
            val header = groupHeaderMap[categoryHelper.categoryGroup] ?: continue

            val categoryItemViewModel = NotificationCategoryItemViewModel(
                    data = NotificationCategoryViewData(
                            categoryName,
                            titleMap[categoryName],
                            descriptionMap[categoryName],
                            prefs[0].frequency,
                            categoryHelper.position,
                            prefs[0].notification
                    ),
                    toggle = this::toggleNotification
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

    private fun toggleNotification(enabled: Boolean, categoryName: String) {
        viewModelScope.launch {
            try {
                pushChannel?.let {
                    notificationPreferencesManager.updatePreferenceCategoryAsync(
                            categoryName,
                            it.id,
                            enabled.frequency,
                    ).await().dataOrThrow
                } ?: throw IllegalStateException()
            } catch (e: Exception) {
                e.printStackTrace()
                _data.value?.items?.forEach {
                    val itemViewModel = it.itemViewModels.firstOrNull { it.data.categoryName == categoryName }
                    itemViewModel?.let {
                        it.apply {
                            data.frequency = enabled.not().frequency
                            notifyPropertyChanged(BR.checked)
                        }
                        return@forEach
                    }
                }
                _events.postValue(Event(NotificationPreferencesAction.ShowSnackbar(resources.getString(R.string.errorOccurred))))
            }
        }
    }

    private val Boolean.frequency: String
        get() = if (this) NotificationPreferencesManager.IMMEDIATELY else NotificationPreferencesManager.NEVER
}