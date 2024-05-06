/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.mvvm

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes

sealed class ViewState {
    object Loading : ViewState()
    data class LoadingWithAnimation(
        @StringRes val titleRes: Int,
        @StringRes val messageRes: Int,
        @RawRes val animationRes: Int
    ) : ViewState()
    object Success : ViewState()
    object Refresh : ViewState()
    object LoadingNextPage : ViewState()
    data class Empty(@StringRes val emptyTitle: Int? = null, @StringRes val emptyMessage: Int? = null, @DrawableRes val emptyImage: Int? = null) : ViewState()
    data class Error(val errorMessage: String = "", @DrawableRes val errorImage: Int? = null) : ViewState()

    fun isInLoadingState(): Boolean {
        return this is Loading || this is Refresh || this is LoadingNextPage
    }

    fun isSuccessState(): Boolean {
        return this is Success
    }
}