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

package com.instructure.student.features.documentscanning

import android.graphics.Bitmap
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.instructure.student.features.documentscanning.itemviewmodels.FilterItemViewModel

data class DocumentScanningViewData(
        @get:Bindable var selectedBitmap: Bitmap,
        val filterItemViewModels: List<FilterItemViewModel>
) : BaseObservable()

data class FilterItemViewData(
        val bitmap: Bitmap,
        val name: String
)

sealed class DocumentScanningAction {
    data class SaveBitmapAction(val bitmap: Bitmap, val quality: Int): DocumentScanningAction()
}