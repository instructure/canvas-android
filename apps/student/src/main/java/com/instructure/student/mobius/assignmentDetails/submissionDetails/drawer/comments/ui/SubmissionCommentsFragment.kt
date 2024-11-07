/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsEffectHandler
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsPresenter
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsSharedEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsTabData
import com.instructure.student.mobius.common.FlowSource
import com.instructure.student.mobius.common.LiveDataSource
import com.instructure.student.mobius.common.ui.SubmissionHelper
import com.instructure.student.room.StudentDb
import com.instructure.student.room.entities.CreatePendingSubmissionCommentEntity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SubmissionCommentsFragment : BaseSubmissionCommentsFragment() {

    @Inject
    lateinit var submissionHelper: SubmissionHelper

    @Inject
    lateinit var studentDb: StudentDb

    override fun makeEffectHandler() = SubmissionCommentsEffectHandler(requireContext(), submissionHelper)

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = SubmissionCommentsView(inflater, parent, studentDb)

    override fun makePresenter() = SubmissionCommentsPresenter(studentDb)

    override fun getExternalEventSources() = listOf(
        FlowSource.getSource<SubmissionCommentsSharedEvent, SubmissionCommentsEvent> {
            when (it) {
                is SubmissionCommentsSharedEvent.SendMediaCommentClicked -> SubmissionCommentsEvent.SendMediaCommentClicked(
                    it.file
                )
                is SubmissionCommentsSharedEvent.MediaCommentDialogClosed -> SubmissionCommentsEvent.AddFilesDialogClosed
            }
        },
        FlowSource.getSource<SubmissionComment, SubmissionCommentsEvent> {
            SubmissionCommentsEvent.SubmissionCommentAdded(it)
        },
        LiveDataSource.of<List<CreatePendingSubmissionCommentEntity>, SubmissionCommentsEvent>(
            studentDb.pendingSubmissionCommentDao()
                .findCommentsByAccountAndAssignmentIdLiveData(ApiPrefs.domain, assignment.id)
        ) { pendingComments ->
            val commentIds = pendingComments?.map { it.id } ?: emptyList()
            SubmissionCommentsEvent.PendingSubmissionsUpdated(commentIds)
        }
    )

    companion object {
        fun newInstance(data: SubmissionDetailsTabData.CommentData) = SubmissionCommentsFragment().apply {
            submission = data.submission
            assignment = data.assignment
            attemptId = data.attemptId
            assignmentEnhancementsEnabled = data.assignmentEnhancementsEnabled
        }
    }

}