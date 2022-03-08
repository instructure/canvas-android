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

package com.instructure.pandautils.features.notification.preferences.itemviewmodels

import com.instructure.pandautils.R
import com.instructure.pandautils.features.notification.preferences.NotificationCategoryViewData
import com.instructure.pandautils.features.notification.preferences.NotificationPreferencesViewType
import com.instructure.pandautils.mvvm.ItemViewModel

class NotificationCategoryItemViewModel(
        val data: NotificationCategoryViewData
) : ItemViewModel {
    override val layoutId: Int = R.layout.item_notification_preference

    override val viewType: Int = NotificationPreferencesViewType.CATEGORY.viewType

    fun onCheckedChanged(checked: Boolean) {

    }
}