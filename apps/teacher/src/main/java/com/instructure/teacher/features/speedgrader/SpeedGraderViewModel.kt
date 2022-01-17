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
import com.instructure.canvasapi2.CommentLibraryQuery
import com.instructure.canvasapi2.managers.CommentLibraryManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.awaitQL
import com.instructure.pandautils.mvvm.Event
import com.instructure.teacher.features.speedgrader.commentlibrary.itemviewmodels.CommentItemViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class SpeedGraderViewModel @Inject constructor(
    private val apiPrefs: ApiPrefs,
    private val commentLibraryManager: CommentLibraryManager,
    private val userManager: UserManager
) : ViewModel() {

    private val comments = mutableMapOf<Long, MutableLiveData<CommentViewData>>()

    val suggestionsData: LiveData<List<CommentItemViewModel>>
        get() = _suggestionsData
    private val _suggestionsData = MutableLiveData<List<CommentItemViewModel>>(emptyList())

    val events: LiveData<Event<SpeedGraderAction>>
        get() = _events
    private val _events = MutableLiveData<Event<SpeedGraderAction>>()

    var currentSubmissionId: Long? = null

    init {
        loadCommentLibrary()
    }

    private fun loadCommentLibrary() {
        viewModelScope.launch {
            try {
                val userSettings = userManager.getSelfSettings(true).await()
                val commentLibraryEnabled = userSettings.dataOrNull?.commentLibrarySuggestions == true

                if (commentLibraryEnabled) {
                    loadCommentLibraryContent()
                }
            } catch (e: Exception) {
                // TODO If error comes from refresh we might show an error toast, otherwise make it silent.
            }
        }
    }

    private suspend fun loadCommentLibraryContent() {
        val userId = apiPrefs.user?.id ?: -1
        val data = awaitQL<CommentLibraryQuery.Data> { CommentLibraryManager().getCommentLibraryItems(userId, it) }

        val user = data.user as CommentLibraryQuery.AsUser
        val suggestions = user.commentBankItems?.nodes?.map {
            CommentItemViewModel(it.comment) { comment: String -> setCommentFromSuggestion(comment) }
        } ?: emptyList()

        _suggestionsData.value = suggestions
    }

    private fun setCommentFromSuggestion(comment: String) {
        currentSubmissionId?.let {
            setCommentById(it, comment, true)
            _events.value = Event(SpeedGraderAction.CommentLibraryClosed)
        }
    }

    fun setCommentById(submissionId: Long, comment: String, selectedFromSuggestion: Boolean = false) {
        val commentLiveData = comments.computeIfAbsent(submissionId) { MutableLiveData() }
        commentLiveData.value = CommentViewData(comment, selectedFromSuggestion)
    }

    fun getCommentById(submissionId: Long): LiveData<CommentViewData> {
        return comments.computeIfAbsent(submissionId) { MutableLiveData() }
    }
}