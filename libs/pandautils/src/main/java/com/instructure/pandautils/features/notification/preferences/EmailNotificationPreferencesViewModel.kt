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
package com.instructure.pandautils.features.notification.preferences

import android.content.res.Resources
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.CommunicationChannelsManager
import com.instructure.canvasapi2.managers.NotificationPreferencesFrequency
import com.instructure.canvasapi2.managers.NotificationPreferencesManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.BR
import com.instructure.pandautils.R
import com.instructure.pandautils.features.notification.preferences.itemviewmodels.EmailNotificationCategoryItemViewModel
import com.instructure.pandautils.features.notification.preferences.itemviewmodels.NotificationCategoryItemViewModel
import com.instructure.pandautils.mvvm.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailNotificationPreferencesViewModel @Inject constructor(
    communicationChannelsManager: CommunicationChannelsManager,
    notificationPreferencesManager: NotificationPreferencesManager,
    apiPrefs: ApiPrefs,
    notificationPreferenceUtils: NotificationPreferenceUtils,
    resources: Resources
): NotificationPreferencesViewModel(communicationChannelsManager, notificationPreferencesManager, apiPrefs, notificationPreferenceUtils, resources) {

    override val notificationChannelType: String
        get() = "email"

    override fun createCategoryItemViewModel(viewData: NotificationCategoryViewData): NotificationCategoryItemViewModel {
        return EmailNotificationCategoryItemViewModel(viewData, resources, ::notificationCategorySelected)
    }

    private fun notificationCategorySelected(categoryName: String, frequency: NotificationPreferencesFrequency) {
        _events.postValue(Event(NotificationPreferencesAction.ShowFrequencySelectionDialog(categoryName, frequency)))
    }

    fun updateFrequency(categoryName: String, selectedFrequency: NotificationPreferencesFrequency) {
        val selectedItem = _data.value?.items?.flatMap { it.itemViewModels }?.find { it.data.categoryName == categoryName } as? EmailNotificationCategoryItemViewModel
        if (selectedItem == null) return

        val previousFrequency = selectedItem.data.frequency
        updateItemFrequency(selectedItem, selectedFrequency)

        viewModelScope.launch {
            try {
                val channel = communicationChannel
                if (channel != null) {
                    notificationPreferencesManager.updatePreferenceCategoryAsync(categoryName, channel.id, selectedFrequency.apiString).await().dataOrThrow
                } else {
                    _events.postValue(Event(NotificationPreferencesAction.ShowSnackbar(resources.getString(R.string.errorOccurred))))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                updateItemFrequency(selectedItem, previousFrequency)
                _events.postValue(Event(NotificationPreferencesAction.ShowSnackbar(resources.getString(R.string.errorOccurred))))
            }
        }
    }

    private fun updateItemFrequency(item: NotificationCategoryItemViewModel, frequency: NotificationPreferencesFrequency) {
        item.data.frequency = frequency
        item.notifyPropertyChanged(BR.frequency)
    }
}