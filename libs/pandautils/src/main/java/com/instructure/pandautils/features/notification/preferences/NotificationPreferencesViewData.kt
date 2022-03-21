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

import com.instructure.pandautils.features.notification.preferences.itemviewmodels.NotificationCategoryHeaderItemViewModel

data class NotificationPreferencesViewData(val items: List<NotificationCategoryHeaderItemViewModel>)

data class NotificationCategoryHeaderViewData(val title: String, val position: Int)

data class NotificationCategoryViewData(
        val name: String,
        val title: String?,
        val description: String?,
        var frequency: String,
        val position: Int,
        val notification: String?
) {
    val categoryName: String
        get() = notification ?: name
}

enum class NotificationPreferencesViewType(val viewType: Int) {
    HEADER(0),
    CATEGORY(1)
}

sealed class NotificationPreferencesAction {
    data class ShowSnackbar(val snackbar: String): NotificationPreferencesAction()
}