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

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.instructure.pandautils.R
import com.instructure.pandautils.BR
import com.instructure.student.features.documentscanning.itemviewmodels.FilterItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.zynksoftware.documentscanner.model.ScannerResults
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DocumentScanningViewModel @Inject constructor(
        private val resources: Resources
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<DocumentScanningViewData>
        get() = _data
    private val _data = MutableLiveData<DocumentScanningViewData>()

    val events: LiveData<Event<DocumentScanningAction>>
        get() = _events
    private val _events = MutableLiveData<Event<DocumentScanningAction>>()

    private lateinit var selectedItem: FilterItemViewModel

    fun setScannerResults(results: ScannerResults) {
        _state.postValue(ViewState.Loading)
        createViewData(results)
    }

    private fun createViewData(results: ScannerResults) {
        if (results.croppedImageFile != null && results.originalImageFile != null) {
            val croppedBitmap = BitmapFactory.decodeFile(results.croppedImageFile!!.path)
            val originalBitmap = BitmapFactory.decodeFile(results.originalImageFile!!.path)
            val grayscaleBitmap = croppedBitmap.toGrayscale()
            val monochromeBitmap = croppedBitmap.toMonochrome()

            //We no longer need these files
            results.croppedImageFile?.delete()
            results.croppedImageFile?.delete()
            results.transformedImageFile?.delete()

            val filters = listOf(
                    createFilterViewModel(croppedBitmap, true, resources.getString(R.string.filter_name_color)),
                    createFilterViewModel(grayscaleBitmap, false, resources.getString(R.string.filter_name_grayscale)),
                    createFilterViewModel(monochromeBitmap, false, resources.getString(R.string.filter_name_monochrome)),
                    createFilterViewModel(originalBitmap, false, resources.getString(R.string.filter_name_original))
            )
            selectedItem = filters[0]

            val viewData = DocumentScanningViewData(
                    croppedBitmap,
                    filters
            )
            _data.postValue(viewData)
            _state.postValue(ViewState.Success)
        } else {
            _state.postValue(ViewState.Error())
        }
    }

    private fun createFilterViewModel(bitmap: Bitmap, selected: Boolean, name: String): FilterItemViewModel {
        return FilterItemViewModel(
                FilterItemViewData(bitmap, name),
                selected,
                this::onFilterSelected
        )
    }

    fun onFilterSelected(itemViewModel: FilterItemViewModel) {
        selectedItem.apply {
            selected = false
            notifyPropertyChanged(BR.selected)
        }
        _data.value?.apply {
            selectedBitmap = itemViewModel.data.bitmap
            notifyPropertyChanged(BR.selectedBitmap)
        }
        selectedItem = itemViewModel
    }

    fun onSaveClicked() {
        _events.postValue(Event(DocumentScanningAction.SaveBitmapAction(selectedItem.data.bitmap, 100)))
    }
}