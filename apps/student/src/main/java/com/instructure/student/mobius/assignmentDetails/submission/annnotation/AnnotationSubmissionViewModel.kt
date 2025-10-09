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
package com.instructure.student.mobius.assignmentDetails.submission.annnotation

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.CanvaDocsManager
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.student.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DRAFT_ATTEMPT = "draft"

@HiltViewModel
class AnnotationSubmissionViewModel @Inject constructor(
    private val canvaDocsManager: CanvaDocsManager,
    private val resources: Resources
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val pdfUrl: LiveData<String>
        get() = _pdfUrl
    private val _pdfUrl = MutableLiveData<String>()

    fun loadAnnotatedPdfUrl(submissionId: Long, attempt: String = DRAFT_ATTEMPT) {
        _state.value = ViewState.Loading
        viewModelScope.launch {
            try {
                val docSession = canvaDocsManager.createCanvaDocSessionAsync(submissionId, attempt)
                    .await().dataOrThrow
                val sessionUrl = docSession.canvadocsSessionUrl ?: ""
                if (sessionUrl.isNotEmpty()) {
                    _pdfUrl.value = sessionUrl
                    _state.value = ViewState.Success
                } else {
                    _state.value =
                        ViewState.Error(resources.getString(R.string.failedToLoadSubmission))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = ViewState.Error(resources.getString(R.string.failedToLoadSubmission))
            }
        }
    }
}