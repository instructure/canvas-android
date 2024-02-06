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

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.UpdateFileFolder
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.FileFolderUpdatedEvent
import com.instructure.teacher.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class UpdateFileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val fileApi: FileFolderAPI.FilesFoldersInterface,
    private val eventBus: EventBus
) : ViewModel() {

    private val fileId: Long = savedStateHandle.get<Long>("contentId") ?: -1L
    private val contentDetails: ModuleContentDetails = savedStateHandle.get<ModuleContentDetails>("contentDetails") ?: ModuleContentDetails()

    val data: LiveData<UpdateFileViewData>
        get() = _data
    private val _data = MutableLiveData<UpdateFileViewData>()

    val events: LiveData<Event<UpdateFileEvent>>
        get() = _events
    private val _events = MutableLiveData<Event<UpdateFileEvent>>()

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    init {
        _state.postValue(ViewState.Loading)
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val file = fileApi.getFile(fileId, RestParams(isForceReadFromNetwork = true)).dataOrNull

            val availability = when {
                contentDetails.locked == true -> FileAvailability.UNPUBLISHED
                contentDetails.hidden == true -> FileAvailability.HIDDEN
                contentDetails.lockAt != null || contentDetails.unlockAt != null -> FileAvailability.SCHEDULED
                else -> FileAvailability.PUBLISHED
            }

            val visibility =
                file?.visibilityLevel?.let { FileVisibility.valueOf(it.uppercase()) } ?: FileVisibility.INHERIT

            _data.postValue(
                UpdateFileViewData(
                    selectedAvailability = availability,
                    selectedVisibility = visibility,
                    lockAt = contentDetails.lockDate,
                    unlockAt = contentDetails.unlockDate,
                    lockAtDateString = DateHelper.getFormattedDate(context, contentDetails.lockDate),
                    lockAtTimeString = DateHelper.getFormattedTime(context, contentDetails.lockDate),
                    unlockAtDateString = DateHelper.getFormattedDate(context, contentDetails.unlockDate),
                    unlockAtTimeString = DateHelper.getFormattedTime(context, contentDetails.unlockDate),
                )
            )
            _state.postValue(ViewState.Success)
        }
    }

    fun onAvailabilityChanged(availability: FileAvailability) {
        _data.postValue(
            data.value?.copy(
                selectedAvailability = availability
            )
        )
    }

    fun onVisibilityChanged(visibility: FileVisibility) {
        _data.postValue(
            data.value?.copy(
                selectedVisibility = visibility
            )
        )
    }

    fun close() {
        _events.postValue(Event(UpdateFileEvent.Close))
    }

    fun update() {
        viewModelScope.launch {
            _state.postValue(ViewState.Loading)
            val updateFileFolder = UpdateFileFolder(
                locked = data.value?.selectedAvailability == FileAvailability.UNPUBLISHED,
                hidden = data.value?.selectedAvailability == FileAvailability.HIDDEN,
                lockAt = if (data.value?.selectedAvailability == FileAvailability.SCHEDULED) data.value?.lockAt?.toApiString()
                    .orEmpty() else "",
                unlockAt = if (data.value?.selectedAvailability == FileAvailability.SCHEDULED) data.value?.unlockAt?.toApiString()
                    .orEmpty() else "",
                visibilityLevel = data.value?.selectedVisibility?.name?.lowercase()
            )
            val updatedFile =
                fileApi.updateFile(fileId, updateFileFolder, RestParams(isForceReadFromNetwork = true)).dataOrNull
            if (updatedFile != null) {
                eventBus.post(FileFolderUpdatedEvent(updatedFile))
                _events.postValue(Event(UpdateFileEvent.Close))
            } else {
                _state.postValue(
                    ViewState.Error(
                        context.getString(R.string.errorOccurred),
                        R.drawable.ic_panda_nofiles
                    )
                )
            }
        }
    }

    fun updateLockAt() {
        _events.postValue(Event(UpdateFileEvent.ShowDatePicker(selectedDate = data.value?.lockAt, minDate = data.value?.unlockAt, callback = ::setLockAtDate)))
    }

    fun updateLockTime() {
        _events.postValue(Event(UpdateFileEvent.ShowTimePicker(data.value?.lockAt, ::setLockTime)))
    }

    fun updateUnlockAt() {
        _events.postValue(Event(UpdateFileEvent.ShowDatePicker(selectedDate = data.value?.unlockAt, maxDate = data.value?.lockAt, callback = ::setUnlockAtDate)))
    }

    fun updateUnlockTime() {
        _events.postValue(Event(UpdateFileEvent.ShowTimePicker(data.value?.unlockAt, ::setUnlockTime)))
    }

    private fun setUnlockAtDate(year: Int, month: Int, day: Int) {
        val selectedDate = getSelectedDate(data.value?.unlockAt, year, month, day)
        _data.postValue(
            data.value?.copy(
                unlockAt = selectedDate,
                unlockAtDateString = DateHelper.getFormattedDate(context, selectedDate),
                unlockAtTimeString = DateHelper.getFormattedTime(context, selectedDate)
            )
        )
    }

    private fun setLockAtDate(year: Int, month: Int, day: Int) {
        val selectedDate = getSelectedDate(data.value?.lockAt, year, month, day)
        _data.postValue(
            data.value?.copy(
                lockAt = selectedDate,
                lockAtDateString = DateHelper.getFormattedDate(context, selectedDate),
                lockAtTimeString = DateHelper.getFormattedTime(context, selectedDate)
            )
        )
    }

    private fun setUnlockTime(hourOfDay: Int, minutes: Int) {
        val selectedDate = getSelectedTime(data.value?.unlockAt, hourOfDay, minutes)
        _data.postValue(
            data.value?.copy(
                unlockAt = selectedDate,
                unlockAtDateString = DateHelper.getFormattedDate(context, selectedDate),
                unlockAtTimeString = DateHelper.getFormattedTime(context, selectedDate)
            )
        )
    }

    private fun setLockTime(hourOfDay: Int, minutes: Int) {
        val selectedDate = getSelectedTime(data.value?.lockAt, hourOfDay, minutes)
        _data.postValue(
            data.value?.copy(
                lockAt = selectedDate,
                lockAtDateString = DateHelper.getFormattedDate(context, selectedDate),
                lockAtTimeString = DateHelper.getFormattedTime(context, selectedDate)
            )
        )
    }

    fun clearUnlockDate() {
        _data.postValue(
            data.value?.copy(
                unlockAt = null,
                unlockAtDateString = null,
                unlockAtTimeString = null
            )
        )
    }

    fun clearLockDate() {
        _data.postValue(
            data.value?.copy(
                lockAt = null,
                lockAtDateString = null,
                lockAtTimeString = null
            )
        )
    }

    private fun getSelectedDate(previousDate: Date?, year: Int, month: Int, day: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = previousDate ?: Date()
        calendar.set(year, month, day, 0, 0)
        return calendar.time
    }

    private fun getSelectedTime(previousDate: Date?, hourOfDay: Int, minutes: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = previousDate ?: Date()
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minutes)
        return calendar.time
    }
}