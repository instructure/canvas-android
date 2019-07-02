/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
 */    package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.*
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsTabData
import com.instructure.student.mobius.common.ChannelSource
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.mobius.common.ui.MobiusFragment
import com.instructure.student.mobius.common.ui.Presenter
import com.instructure.student.mobius.common.ui.UpdateInit
import com.spotify.mobius.EventSource

class SubmissionCommentsFragment :
        MobiusFragment<SubmissionCommentsModel, SubmissionCommentsEvent, SubmissionCommentsEffect, SubmissionCommentsView, SubmissionCommentsViewState>() {
    lateinit var data: SubmissionDetailsTabData.CommentData

    override fun makeEffectHandler() = SubmissionCommentsEffectHandler(requireContext())
    override fun makeUpdate() = SubmissionCommentsUpdate()
    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = SubmissionCommentsView(inflater, parent)
    override fun makePresenter() = SubmissionCommentsPresenter

    // TODO update Comment Data
    override fun makeInitModel() = SubmissionCommentsModel(ArrayList<SubmissionComment>(), Submission(), 1L, 1L, data.assignmentId, false)

    override val eventSources: List<EventSource<SubmissionCommentsEvent>> = listOf(
        ChannelSource.getSource<SubmissionCommentsSharedEvent, SubmissionCommentsEvent> {
            when (it) {
                is SubmissionCommentsSharedEvent.SendMediaCommentClicked -> SubmissionCommentsEvent.SendMediaCommentClicked(it.file)
                is SubmissionCommentsSharedEvent.MediaCommentDialogClosed -> SubmissionCommentsEvent.MediaCommentDialogClosed
            }
        }
    )
}