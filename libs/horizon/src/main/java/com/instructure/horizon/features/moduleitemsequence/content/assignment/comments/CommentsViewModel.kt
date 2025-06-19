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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.comments

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.CommentsData
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.organisms.cards.CommentCardState
import com.instructure.pandautils.utils.format
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val commentsRepository: CommentsRepository,
    private val apiPrefs: ApiPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentsUiState(onCommentChanged = ::onCommentChanged, onPostClicked = ::postComment))
    val uiState = _uiState.asStateFlow()

    private var assignmentId: Long = -1L
    private var attempt: Int = -1

    private var endCursor: String? = null
    private var startCursor: String? = null
    private var hasNextPage: Boolean = false
    private var hasPreviousPage: Boolean = false

    fun initWithAttempt(assignmentId: Long, attempt: Int) {
        _uiState.update { it.copy(loading = true) }
        this.assignmentId = assignmentId
        this.attempt = attempt
        viewModelScope.tryLaunch {
            val commentsData = commentsRepository.getComments(
                assignmentId = assignmentId,
                userId = apiPrefs.user?.id.orDefault(),
                attempt = attempt,
                forceNetwork = false
            )

            updateState(commentsData)
        } catch { _ ->
            _uiState.update { it.copy(loading = false) }
        }
    }

    private fun loadNextPage() {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.tryLaunch {
            val commentsData = commentsRepository.getComments(
                assignmentId = assignmentId,
                userId = apiPrefs.user?.id.orDefault(),
                attempt = attempt,
                forceNetwork = false,
                endCursor = endCursor,
                nextPage = true
            )

            updateState(commentsData)
        } catch { _ ->
            _uiState.update { it.copy(loading = false) }
        }
    }

    private fun loadPreviousPage() {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.tryLaunch {
            val commentsData = commentsRepository.getComments(
                assignmentId = assignmentId,
                userId = apiPrefs.user?.id.orDefault(),
                attempt = attempt,
                forceNetwork = false,
                startCursor = startCursor,
                nextPage = false
            )

            updateState(commentsData)
        } catch { _ ->
            _uiState.update { it.copy(loading = false) }
        }
    }

    private fun updateState(commentsData: CommentsData) {
        endCursor = commentsData.endCursor
        startCursor = commentsData.startCursor
        hasNextPage = commentsData.hasNextPage
        hasPreviousPage = commentsData.hasPreviousPage

        val commentCards = commentsData.comments.map {
            CommentCardState(
                title = it.authorName,
                date = it.createdAt.format("dd/MM, h:mm a"),
                subtitle = context.getString(R.string.commentsBottomSheet_attempt, attempt),
                commentText = it.commentText,
                fromCurrentUser = it.authorId == apiPrefs.user?.id,
                read = it.read
            )
        }

        val showPagingControls = commentsData.hasNextPage || commentsData.hasPreviousPage
        val nextPageEnabled = commentsData.hasNextPage
        val previousPageEnabled = commentsData.hasPreviousPage

        _uiState.update {
            it.copy(
                comments = commentCards,
                showPagingControls = showPagingControls,
                nextPageEnabled = nextPageEnabled,
                previousPageEnabled = previousPageEnabled,
                onPreviousPageClicked = ::loadPreviousPage,
                onNextPageClicked = ::loadNextPage,
                loading = false
            )
        }
    }

    private fun onCommentChanged(newComment: TextFieldValue) {
        _uiState.update { it.copy(comment = newComment) }
    }

    private fun postComment() {
        val commentText = _uiState.value.comment.text
        if (commentText.isBlank()) return


    }
}