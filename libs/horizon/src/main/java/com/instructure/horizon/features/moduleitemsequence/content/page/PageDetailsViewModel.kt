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
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedData
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.domain.usecase.GetPageDetailsUseCase
import com.instructure.horizon.domain.usecase.notebook.AddNoteUseCase
import com.instructure.horizon.domain.usecase.notebook.GetNotesUseCase
import com.instructure.horizon.features.moduleitemsequence.ModuleItemContent
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.pandautils.utils.NetworkStateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PageDetailsViewModel @Inject constructor(
    private val getPageDetailsUseCase: GetPageDetailsUseCase,
    private val htmlContentFormatter: HtmlContentFormatter,
    private val oAuthApi: OAuthAPI.OAuthInterface,
    private val getNotesUseCase: GetNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val networkStateProvider: NetworkStateProvider,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val courseId: Long = savedStateHandle[Const.COURSE_ID] ?: -1L
    private val pageUrl: String = savedStateHandle[ModuleItemContent.Page.PAGE_URL] ?: ""

    private val _uiState = MutableStateFlow(
        PageDetailsUiState(
            ltiButtonPressed = ::ltiButtonPressed,
            onUrlOpened = ::onUrlOpened,
            addNote = ::addNote,
            courseId = courseId,
            isOnline = networkStateProvider.isOnline(),
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isLoading = true), isOnline = networkStateProvider.isOnline())
            }
            val pageDetails = getPageDetailsUseCase(GetPageDetailsUseCase.Params(courseId, pageUrl))
            val html = htmlContentFormatter.formatHtmlWithIframes(pageDetails.body.orEmpty(), courseId)
            val notes = fetchNotes(pageDetails.id)
            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(isLoading = false),
                    pageHtmlContent = html,
                    notes = notes,
                    pageId = pageDetails.id,
                    pageUrl = pageUrl
                )
            }
        } catch {
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isLoading = false, isError = true))
            }
        }
    }

    fun refreshNotes() {
        viewModelScope.tryLaunch {
            val notes = fetchNotes(uiState.value.pageId)
            _uiState.update { it.copy(notes = notes) }
        } catch { }
    }

    private suspend fun fetchNotes(pageId: Long) = try {
        getNotesUseCase(
            GetNotesUseCase.Params(
                courseId = courseId,
                objectTypeAndId = "Page" to pageId.toString(),
                itemCount = NOTES_PAGE_LIMIT,
            )
        ).notes
    } catch (e: Exception) {
        emptyList()
    }

    private fun ltiButtonPressed(ltiUrl: String) {
        viewModelScope.launch {
            try {
                val authenticatedSessionURL = oAuthApi.getAuthenticatedSession(
                    ltiUrl,
                    RestParams(isForceReadFromNetwork = true)
                ).dataOrNull?.sessionUrl ?: ltiUrl
                _uiState.update { it.copy(urlToOpen = authenticatedSessionURL) }
            } catch (e: Exception) {
                _uiState.update { it.copy(urlToOpen = ltiUrl) }
            }
        }
    }

    private fun onUrlOpened() {
        _uiState.update { it.copy(urlToOpen = null) }
    }

    private fun addNote(highlightedData: NoteHighlightedData, type: String) {
        viewModelScope.tryLaunch {
            addNoteUseCase(
                AddNoteUseCase.Params(
                    courseId = courseId.toString(),
                    objectId = uiState.value.pageId.toString(),
                    objectType = "Page",
                    highlightedData = highlightedData,
                    userComment = "",
                    type = NotebookType.valueOf(type),
                )
            )
            refreshNotes()
        } catch {}
    }

    companion object {
        private const val NOTES_PAGE_LIMIT = 100
    }
}
