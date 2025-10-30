/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.dashboard.widget.announcement

import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardState
import java.util.Date

data class DashboardAnnouncementBannerUiState(
    val state: DashboardItemState = DashboardItemState.LOADING,
    val cardState: DashboardPaginatedWidgetCardState = DashboardPaginatedWidgetCardState(),
    val onRefresh: (() -> Unit) -> Unit,
)

data class AnnouncementBannerItem(
    val title: String,
    val source: String? = null,
    val date: Date?,
    val type: AnnouncementType,
    val route: String
)

enum class AnnouncementType {
    COURSE,
    GLOBAL
}
