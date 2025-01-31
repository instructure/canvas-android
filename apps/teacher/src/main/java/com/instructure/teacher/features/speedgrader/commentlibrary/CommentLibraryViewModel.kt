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
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.managers.CommentLibraryManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.utils.unaccent
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
    private val teacherPrefs: TeacherPrefs,
    private val firebaseCrashlytics: FirebaseCrashlytics
) : ViewModel() {

    val data: LiveData<CommentLibraryViewData>
        get() = _data
    private val _data = MutableLiveData(CommentLibraryViewData(emptyList()))

    val events: LiveData<Event<CommentLibraryAction>>
        get() = _events
    private val _events = MutableLiveData<Event<CommentLibraryAction>>()

    var currentSubmissionId: Long? = null
        set(value) {
            field = value
            updateFilteredSuggestions(value ?: -1)
        }

    private val commentsBySubmission = mutableMapOf<Long, MutableLiveData<CommentViewData>>()

    private var allSuggestions = listOf<String>()

    init {
        loadCommentLibrary()
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
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    private suspend fun loadCommentLibraryContent() {
        val userId = apiPrefs.user?.id ?: -1
        allSuggestions = commentLibraryManager.getCommentLibraryItems(userId)

        val suggestions = allSuggestions.map {
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

        // We need this check because when we put the app to background and go back, setCommentBySubmission will be triggered
        // for all the submissions that has cached pending comment and we want the filtering to represent the submission we are currently on.
        if (currentSubmissionId == submissionId) {
            filterSuggestions(comment)
        }
    }

    fun getCommentBySubmission(submissionId: Long): LiveData<CommentViewData> {
        return commentsBySubmission.computeIfAbsent(submissionId) { MutableLiveData() }
    }

    private fun updateFilteredSuggestions(currentSubmissionId: Long) {
        val commentLiveData = commentsBySubmission.computeIfAbsent(currentSubmissionId) { MutableLiveData() }
        val query = commentLiveData.value?.comment ?: ""
        filterSuggestions(query)
    }

    private fun filterSuggestions(query: String) {
        val filteredSuggestions = allSuggestions
            .filter { it.unaccent().contains(query.unaccent(), ignoreCase = true) }
            .map { SuggestionItemViewModel(it, query) { comment: String -> replaceCommentWithSuggestion(comment) } }
        _data.value = CommentLibraryViewData(filteredSuggestions)
    }
}