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
import com.instructure.pandautils.mvvm.Event
import com.instructure.teacher.features.speedgrader.commentlibrary.itemviewmodels.CommentItemViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SpeedGraderViewModel @Inject constructor() : ViewModel() {

    val comments: Map<Long, LiveData<CommentViewData>>
        get() = _comments
    private val _comments = mutableMapOf<Long, MutableLiveData<CommentViewData>>()

    val suggestionsData: LiveData<List<CommentItemViewModel>>
        get() = _suggestionsData
    private val _suggestionsData = MutableLiveData<List<CommentItemViewModel>>()

    val events: LiveData<Event<SpeedGraderAction>>
        get() = _events
    private val _events = MutableLiveData<Event<SpeedGraderAction>>()

    var currentSubmissionId: Long? = null

    init {
        _suggestionsData.value = listOf("You are off to a great start. Please add more detail to justify your reasoning.",
            "Great work! But it seems that you may have submitted the wrong file. Please double-check, attach the correct file, and resubmit.",
            "Nicely done, group! Great collaboration!",
            "Great first draft. Take a look at the feedback provided on the document and start working on your second draft.",
            "You are off to a great start. Please add more detail to justify your reasoning.",
            "You are off to a great start. Please add more detail to justify your reasoning.",
            "Great work! But it seems that you may have submitted the wrong file. Please double-check, attach the correct file, and resubmit.",
            "Nicely done, group! Great collaboration!",
            "Great first draft. Take a look at the feedback provided on the document and start working on your second draft.",
            "You are off to a great start. Please add more detail to justify your reasoning.",
            "You are off to a great start. Please add more detail to justify your reasoning.",
            "Great work! But it seems that you may have submitted the wrong file. Please double-check, attach the correct file, and resubmit.",
            "Nicely done, group! Great collaboration!",
            "Great first draft. Take a look at the feedback provided on the document and start working on your second draft.",
            "You are off to a great start. Please add more detail to justify your reasoning.")
            .map { CommentItemViewModel(it) { comment: String -> setSuggestionComment(comment) } }
    }

    private fun setSuggestionComment(comment: String) {
        currentSubmissionId?.let {
            setComment(it, comment, true)
            _events.value = Event(SpeedGraderAction.CommentLibraryClosed)
        }
    }

    fun setComment(submissionId: Long, comment: String, selectedFromSuggestion: Boolean = false) {
        val commentLiveData = _comments.computeIfAbsent(submissionId) { MutableLiveData() }
        commentLiveData.value = CommentViewData(comment, selectedFromSuggestion)
    }

    fun getCommentById(submissionId: Long): LiveData<CommentViewData> {
        return _comments.computeIfAbsent(submissionId) { MutableLiveData() }
    }
}