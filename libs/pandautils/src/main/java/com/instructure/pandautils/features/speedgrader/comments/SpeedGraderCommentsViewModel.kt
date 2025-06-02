/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.speedgrader.comments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.ApiPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpeedGraderCommentsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val speedGraderCommentsRepository: SpeedGraderCommentsRepository,
    private val apiPrefs: ApiPrefs
): ViewModel() {

    private val _uiState = MutableStateFlow(SpeedGraderCommentsUiState())
    val uiState = _uiState.asStateFlow()

    private val submissionId: Long = savedStateHandle.get<Long>(SUBMISSION_ID_KEY) ?: -1L

    init {
        viewModelScope.launch {
            fetchData()
        }
    }

    private suspend fun fetchData() {
        val comments = speedGraderCommentsRepository.getSubmissionComments(submissionId)
        _uiState.update { state ->
            state.copy(
                comments = comments.submission?.commentsConnection?.edges
                    ?.mapNotNull { edge ->
                        edge?.node?.let {
                            SpeedGraderComment(
                                id = it.mediaCommentId ?: "",
                                authorName = it.author?.name ?: "Unknown",
                                authorId = it.author?._id ?: "",
                                authorAvatarUrl = it.author?.avatarUrl ?: "",
                                content = it.comment ?: "",
                                createdAt = it.createdAt.toString(),
                                isOwnComment = apiPrefs.user?.id?.toString() == it.author?._id,
                                attachments = it.attachments?.map { attachment ->
                                    SpeedGraderCommentAttachment(
                                        id = attachment.id,
                                        url = attachment.url ?: "",
                                        thumbnailUrl = attachment.thumbnailUrl ?: "",
                                        createdAt = attachment.createdAt.toString(),
                                        displayName = attachment.displayName ?: "",
                                        contentType = attachment.contentType ?: "",
                                        size = attachment.toString(),

                                    )
                                } ?: emptyList()
                            )
                        }

                } ?: emptyList(),
                isLoading = false,
            )
        }
    }

    companion object {
        const val SUBMISSION_ID_KEY = "submissionId"
    }
}