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
package com.instructure.horizon.features.moduleitemsequence.content.page

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.moduleitemsequence.ModuleItemContent
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PageDetailsViewModel @Inject constructor(
    private val pageDetailsRepository: PageDetailsRepository,
    private val htmlContentFormatter: HtmlContentFormatter,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val courseId: Long = savedStateHandle[Const.COURSE_ID] ?: -1L
    private val pageUrl: String = savedStateHandle[ModuleItemContent.Page.PAGE_URL] ?: ""

    private val _uiState = MutableStateFlow(PageDetailsUiState(ltiButtonPressed = ::ltiButtonPressed, onUrlOpened = ::onUrlOpened, courseId = courseId))
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isLoading = true))
            }
            val pageDetails = pageDetailsRepository.getPageDetails(courseId, pageUrl)
            val html = htmlContentFormatter.formatHtmlWithIframes(pageDetails.body.orEmpty())
            val notes = pageDetailsRepository.getNotes(courseId, pageDetails.id)
            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(isLoading = false),
                    pageHtmlContent = html,
                    notes = notes,
                    pageId = pageDetails.id,
                )
            }
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isLoading = false))
            }
        } catch {
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isLoading = false, isError = true))
            }
        }
    }

    private fun ltiButtonPressed(ltiUrl: String) {
        viewModelScope.launch {
            try {
                val authenticatedSessionURL =
                    pageDetailsRepository.authenticateUrl(ltiUrl)

                _uiState.update { it.copy(urlToOpen = authenticatedSessionURL) }
            } catch (e: Exception) {
                _uiState.update { it.copy(urlToOpen = ltiUrl) }
            }
        }
    }

    private fun onUrlOpened() {
        _uiState.update { it.copy(urlToOpen = null) }
    }
}