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
package com.instructure.pandautils.features.notification.preferences.itemviewmodels

import android.content.res.Resources
import androidx.databinding.Bindable
import com.instructure.canvasapi2.managers.NotificationPreferencesFrequency
import com.instructure.pandautils.R
import com.instructure.pandautils.features.notification.preferences.NotificationCategoryViewData
import com.instructure.pandautils.features.notification.preferences.NotificationPreferencesViewType

class EmailNotificationCategoryItemViewModel(
    data: NotificationCategoryViewData,
    val resources: Resources,
    val onClick: (String, NotificationPreferencesFrequency) -> Unit
) : NotificationCategoryItemViewModel(data) {
    override val layoutId: Int = R.layout.item_email_notification_preference

    override val viewType: Int = NotificationPreferencesViewType.EMAIL_CATEGORY.viewType

    @get:Bindable
    val frequency: String
        get() = resources.getString(data.frequency.stringRes)

    fun onClick() {
        onClick(data.categoryName, data.frequency)
    }
}