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

package com.instructure.pandautils.features.file.upload

import android.content.ContentResolver
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.R
import com.instructure.pandautils.dialogs.UploadFilesDialog
import com.instructure.pandautils.features.shareextension.target.ShareExtensionTargetAction
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.services.FileUploadService
import com.instructure.pandautils.utils.FileUploadUtils
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.android.synthetic.main.dialog_files_upload.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileUploadDialogViewModel @Inject constructor(
        private val contentResolver: ContentResolver
) : ViewModel() {

    val events: LiveData<Event<FileUploadAction>>
        get() = _events
    private val _events = MutableLiveData<Event<FileUploadAction>>()

    fun onCameraClicked() {
        _events.postValue(Event(FileUploadAction.TakePhoto))
    }

    fun onGalleryClicked() {
        _events.postValue(Event(FileUploadAction.PickPhoto))
    }

    fun onFilesClicked() {
        _events.postValue(Event(FileUploadAction.PickFile))
    }
}