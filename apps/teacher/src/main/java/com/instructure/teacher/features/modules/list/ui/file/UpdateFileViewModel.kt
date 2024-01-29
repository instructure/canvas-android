/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 */

package com.instructure.teacher.features.modules.list.ui.file

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.UpdateFileFolder
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.utils.FileFolderUpdatedEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@HiltViewModel
class UpdateFileViewModel @Inject constructor(val fileApi: FileFolderAPI.FilesFoldersInterface) : ViewModel() {

    val data: LiveData<UpdateFileViewData>
        get() = _data
    private val _data = MutableLiveData<UpdateFileViewData>()

    val events: LiveData<Event<UpdateFileEvent>>
        get() = _events
    private val _events = MutableLiveData<Event<UpdateFileEvent>>()

    private var fileId: Long = -1L

    fun loadData(contentDetails: ModuleContentDetails, fileId: Long) {
        viewModelScope.launch {
            this@UpdateFileViewModel.fileId = fileId
            val file = fileApi.getFile(fileId, RestParams(isForceReadFromNetwork = true)).dataOrNull

            val availability = when {
                contentDetails.locked == true -> FileAvailability.UNPUBLISHED
                contentDetails.hidden == true -> FileAvailability.HIDDEN
                contentDetails.lockAt != null || contentDetails.unlockAt != null -> FileAvailability.SCHEDULED
                else -> FileAvailability.PUBLISHED
            }

            val visibility = file?.visibilityLevel?.let { FileVisibility.valueOf(it.uppercase()) } ?: FileVisibility.INHERIT

            _data.postValue(
                UpdateFileViewData(
                    title = file?.displayName ?: "",
                    selectedAvailability = availability,
                    selectedVisibility = visibility,
                    lockAt = contentDetails.lockDate,
                    unlockAt = contentDetails.unlockDate
                )
            )
        }
    }

    fun onAvailabilityChanged(isChecked: Boolean, availability: FileAvailability) {
        if (isChecked) {
            _data.postValue(
                data.value?.copy(
                    selectedAvailability = availability
                )
            )
        }
    }

    fun onVisibilityChanged(isChecked: Boolean, visibility: FileVisibility) {
        if (isChecked) {
            _data.postValue(
                data.value?.copy(
                    selectedVisibility = visibility
                )
            )
        }
    }

    fun close() {
        _events.postValue(Event(UpdateFileEvent.Close))
    }

    fun update() {
        viewModelScope.launch {
            val updateFileFolder = UpdateFileFolder(
                locked = data.value?.selectedAvailability == FileAvailability.UNPUBLISHED,
                hidden = data.value?.selectedAvailability == FileAvailability.HIDDEN,
                lockAt = data.value?.lockAt?.toString(),
                unlockAt = data.value?.unlockAt?.toString(),
                visibilityLevel = data.value?.selectedVisibility?.name?.lowercase()
            )
            val updatedFile = fileApi.updateFile(fileId, updateFileFolder, RestParams(isForceReadFromNetwork = true)).dataOrNull
            if (updatedFile != null) {
                EventBus.getDefault().post(FileFolderUpdatedEvent(updatedFile))
                _events.postValue(Event(UpdateFileEvent.Close))
            }
        }
    }
}