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

package com.instructure.parentapp.features.courses.details.frontpage

import android.graphics.Color
import androidx.annotation.ColorInt


data class FrontPageUiState(
    @ColorInt val studentColor: Int = Color.BLACK,
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val isRefreshing: Boolean = false,
    val htmlContent: String = "",
    val baseUrl: String? = null
)

sealed class FrontPageAction {
    data object Refresh : FrontPageAction()
}

sealed class FrontPageViewModelAction {
    data class ShowSnackbar(val message: String) : FrontPageViewModelAction()
}