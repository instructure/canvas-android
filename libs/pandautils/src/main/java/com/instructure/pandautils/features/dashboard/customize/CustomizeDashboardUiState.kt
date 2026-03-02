/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.pandautils.features.dashboard.customize

import com.instructure.pandautils.features.dashboard.widget.SettingType
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata

data class WidgetSettingItem(
    val key: String,
    val type: SettingType,
    val value: Any
)

data class WidgetItem(
    val metadata: WidgetMetadata,
    val displayName: String,
    val settings: List<WidgetSettingItem> = emptyList()
)

data class CustomizeDashboardUiState(
    val widgets: List<WidgetItem> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val isDashboardRedesignEnabled: Boolean = false,
    val onMoveUp: (String) -> Unit = {},
    val onMoveDown: (String) -> Unit = {},
    val onToggleVisibility: (String) -> Unit = {},
    val onToggleDashboardRedesign: (Boolean) -> Unit = {},
    val onUpdateSetting: (widgetId: String, key: String, value: Any) -> Unit = { _, _, _ -> },
    val globalSettings: List<WidgetSettingItem> = emptyList(),
    val feedbackUrl: String = ""
)