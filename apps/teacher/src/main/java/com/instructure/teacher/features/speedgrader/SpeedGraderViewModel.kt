/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.teacher.features.speedgrader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.pandautils.mvvm.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpeedGraderViewModel @Inject constructor() : ViewModel() {

    val comments: Map<Long, LiveData<String>>
        get() = _comments
    private val _comments = mutableMapOf<Long, MutableLiveData<String>>()

    fun setComment(submissionId: Long, comment: String) {
        val commentLiveData = _comments.computeIfAbsent(submissionId) { MutableLiveData() }
        commentLiveData.value = comment
    }

    fun getCommentById(submissionId: Long): LiveData<String> {
        return _comments.computeIfAbsent(submissionId) { MutableLiveData() }
    }
}