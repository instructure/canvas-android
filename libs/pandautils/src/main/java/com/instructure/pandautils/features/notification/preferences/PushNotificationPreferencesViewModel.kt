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
import com.instructure.canvasapi2.managers.NotificationPreferencesFrequency.IMMEDIATELY
import com.instructure.canvasapi2.managers.NotificationPreferencesFrequency.NEVER
import com.instructure.canvasapi2.managers.NotificationPreferencesManager
import com.instructure.canvasapi2.models.NotificationPreference
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.BR
import com.instructure.pandautils.R
import com.instructure.pandautils.features.notification.preferences.itemviewmodels.NotificationCategoryItemViewModel
import com.instructure.pandautils.features.notification.preferences.itemviewmodels.PushNotificationCategoryItemViewModel
import com.instructure.pandautils.mvvm.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PushNotificationPreferencesViewModel @Inject constructor(
    communicationChannelsManager: CommunicationChannelsManager,
    notificationPreferencesManager: NotificationPreferencesManager,
    apiPrefs: ApiPrefs,
    notificationPreferenceUtils: NotificationPreferenceUtils,
    resources: Resources
) : NotificationPreferencesViewModel(communicationChannelsManager, notificationPreferencesManager, apiPrefs, notificationPreferenceUtils, resources) {

    override val notificationChannelType: String
        get() = "push"

    override fun createCategoryItemViewModel(viewData: NotificationCategoryViewData): NotificationCategoryItemViewModel {
        return PushNotificationCategoryItemViewModel(viewData, ::toggleNotification)
    }

    override fun List<NotificationPreference>.filterNotificationPreferences() = filter { it.category in ALLOWED_PUSH_NOTIFICATIONS }

    private fun toggleNotification(enabled: Boolean, categoryName: String) {
        viewModelScope.launch {
            try {
                communicationChannel?.let {
                    notificationPreferencesManager.updatePreferenceCategoryAsync(
                        categoryName,
                        it.id,
                        enabled.frequency.apiString,
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
                _events.postValue(
                    Event(NotificationPreferencesAction.ShowSnackbar(resources.getString(
                        R.string.errorOccurred)))
                )
            }
        }
    }

    private val Boolean.frequency: NotificationPreferencesFrequency
        get() = if (this) IMMEDIATELY else NEVER

    companion object {
        private val ALLOWED_PUSH_NOTIFICATIONS = listOf(
            "announcement",
            "appointment_availability",
            "appointment_cancelations",
            "calendar",
            "conversation_message",
            "course_content",
            "discussion_mention",
            "reported_reply",
            "due_date",
            "grading",
            "invitation",
            "student_appointment_signups",
            "submission_comment",
            "discussion",
            "discussion_entry"
        )
    }
}