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
package com.instructure.teacher.features.speedgrader.commentlibrary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.CommentLibraryQuery
import com.instructure.canvasapi2.managers.CommentLibraryManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.awaitQL
import com.instructure.pandautils.mvvm.Event
import com.instructure.teacher.features.speedgrader.commentlibrary.itemviewmodels.SuggestionItemViewModel
import com.instructure.teacher.utils.TeacherPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentLibraryViewModel @Inject constructor(
    private val apiPrefs: ApiPrefs,
    private val commentLibraryManager: CommentLibraryManager,
    private val userManager: UserManager,
    private val teacherPrefs: TeacherPrefs
) : ViewModel() {

    private val commentsBySubmission = mutableMapOf<Long, MutableLiveData<CommentViewData>>()

    val data: LiveData<CommentLibraryViewData>
        get() = _data
    private val _data = MutableLiveData(CommentLibraryViewData(emptyList()))

    val events: LiveData<Event<CommentLibraryAction>>
        get() = _events
    private val _events = MutableLiveData<Event<CommentLibraryAction>>()

    var allComments = listOf<String>()

    var currentSubmissionId: Long? = null
        set(value) {
            field = value
            updateFilteredSuggestions(value ?: -1)
        }

    init {
        loadCommentLibrary()
    }

    private fun updateFilteredSuggestions(currentSubmissionId: Long) {
        val commentLiveData = commentsBySubmission.computeIfAbsent(currentSubmissionId) { MutableLiveData() }
        val query = commentLiveData.value?.comment ?: ""
        filterSuggestions(query)
    }

    private fun loadCommentLibrary() {
        viewModelScope.launch {
            try {
                val userSettings = userManager.getSelfSettings(true).await().dataOrNull
                val commentLibraryEnabled = if (userSettings == null) {
                    teacherPrefs.commentLibraryEnabled
                } else {
                    val commentLibraryEnabled = userSettings.commentLibrarySuggestions
                    teacherPrefs.commentLibraryEnabled = commentLibraryEnabled
                    commentLibraryEnabled
                }

                if (commentLibraryEnabled) {
                    loadCommentLibraryContent()
                }
            } catch (e: Exception) {
                // No-op Silently fail if we don't have info about the comment library, and just don't show anything.
            }
        }
    }

    private suspend fun loadCommentLibraryContent() {
        val userId = apiPrefs.user?.id ?: -1
        val data = awaitQL<CommentLibraryQuery.Data> { CommentLibraryManager().getCommentLibraryItems(userId, it) }

        val user = data.user as CommentLibraryQuery.AsUser
        allComments = user.commentBankItems?.nodes?.map {
            it.comment
        } ?: emptyList()

        val suggestions = allComments.map {
            SuggestionItemViewModel(it, "") { comment: String -> replaceCommentWithSuggestion(comment) }
        }
        _data.value = CommentLibraryViewData(suggestions)
    }

    private fun replaceCommentWithSuggestion(comment: String) {
        currentSubmissionId?.let {
            setCommentBySubmission(it, comment, true)
            _events.value = Event(CommentLibraryAction.CommentLibraryClosed)
        }
    }

    fun setCommentBySubmission(submissionId: Long, comment: String, selectedFromSuggestion: Boolean = false) {
        val commentLiveData = commentsBySubmission.computeIfAbsent(submissionId) { MutableLiveData() }
        commentLiveData.value = CommentViewData(comment, selectedFromSuggestion)

        filterSuggestions(comment)
    }

    private fun filterSuggestions(query: String) {
        val filteredSuggestions = allComments
            .filter { it.contains(query, ignoreCase = true) }
            .map { SuggestionItemViewModel(it, query) { comment: String -> replaceCommentWithSuggestion(comment) } }
        _data.value = CommentLibraryViewData(filteredSuggestions)
    }

    fun getCommentBySubmission(submissionId: Long): LiveData<CommentViewData> {
        return commentsBySubmission.computeIfAbsent(submissionId) { MutableLiveData() }
    }
}