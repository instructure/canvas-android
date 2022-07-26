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
import com.instructure.canvasapi2.managers.CommunicationChannelsManager
import com.instructure.canvasapi2.managers.NotificationPreferencesManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.notification.preferences.itemviewmodels.EmailNotificationCategoryItemViewModel
import com.instructure.pandautils.features.notification.preferences.itemviewmodels.NotificationCategoryItemViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EmailNotificationPreferencesViewModel @Inject constructor(
    communicationChannelsManager: CommunicationChannelsManager,
    notificationPreferencesManager: NotificationPreferencesManager,
    apiPrefs: ApiPrefs,
    notificationPreferenceUtils: NotificationPreferenceUtils,
    resources: Resources
): NotificationPreferencesViewModel(communicationChannelsManager, notificationPreferencesManager, apiPrefs, notificationPreferenceUtils, resources) {

    override val notificationChannelType: String = "email"

    override fun createCategoryItemViewModel(viewData: NotificationCategoryViewData): NotificationCategoryItemViewModel {
        return EmailNotificationCategoryItemViewModel(viewData)
    }

}