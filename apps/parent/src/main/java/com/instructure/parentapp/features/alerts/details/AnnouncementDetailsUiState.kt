/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.alerts.details

import android.graphics.Color
import androidx.annotation.ColorInt
import com.instructure.canvasapi2.models.Attachment
import java.util.Date

data class AnnouncementDetailsUiState(
    val pageTitle: String? = null,
    val announcementTitle: String? = null,
    val postedDate: Date? = null,
    val message: String? = null,
    val attachment: Attachment? = null,
    @ColorInt val studentColor: Int = Color.BLACK,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isRefreshing: Boolean = false,
    val showErrorSnack: Boolean = false
)

sealed class AnnouncementDetailsAction {
    data object Refresh : AnnouncementDetailsAction()
    data class OpenAttachment(val attachment: Attachment) : AnnouncementDetailsAction()
    data object SnackbarDismissed :AnnouncementDetailsAction()
}
