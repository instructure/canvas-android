/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 */
package com.instructure.horizon.features.moduleitemsequence.content.lti

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.moduleitemsequence.ModuleItemContent
import com.instructure.pandautils.utils.Const
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExternalToolViewModel @Inject constructor(
    private val externalToolRepository: ExternalToolRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val courseId: Long = savedStateHandle[Const.COURSE_ID] ?: -1L
    private val url: String = Uri.decode(savedStateHandle[ModuleItemContent.ExternalTool.URL] ?: "")
    private val externalUrl: String = Uri.decode(savedStateHandle[ModuleItemContent.ExternalTool.EXTERNAL_URL] ?: "")

    private val _uiState = MutableStateFlow(
        ExternalToolUiState(
            previewUrl = externalUrl,
            urlToOpen = url,
            onOpenExternallyClicked = ::openExternally,
            onPreviewError = ::setPreviewError,
            onPageFinished = ::pageFinished,
            onLinkOpened = ::onLinkOpened
        )
    )

    val uiState = _uiState.asStateFlow()

    private fun openExternally() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(openExternallyLoading = true)
            }
            val authUrl = externalToolRepository.authenticateUrl(url)
            _uiState.update {
                it.copy(authenticatedUrl = authUrl, openExternallyLoading = false)
            }
        } catch {
            _uiState.update {
                it.copy(openExternallyLoading = false)
            }
        }
    }

    private fun setPreviewError() {
        _uiState.update {
            it.copy(previewState = PreviewState.ERROR)
        }
    }

    private fun pageFinished() {
        // We need to check if the previewState is ERROR because the pageFinished callback is called after the error callback
        if (_uiState.value.previewState == PreviewState.ERROR) return
        _uiState.update {
            it.copy(previewState = PreviewState.SUCCESS)
        }
    }

    private fun onLinkOpened() {
        _uiState.update {
            it.copy(authenticatedUrl = null)
        }
    }
}