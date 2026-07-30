/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.features.announcement.details

import com.instructure.horizon.horizonui.platform.LoadingState
import java.util.Date

data class AnnouncementDetailsUiState(
    val loadingState: LoadingState = LoadingState(),
    val title: String = "",
    val items: List<AnnouncementDetailsItem> = emptyList(),
)

data class AnnouncementDetailsItem(
    val author: String,
    val date: Date,
    val isHtmlContent: Boolean,
    val content: String,
)
