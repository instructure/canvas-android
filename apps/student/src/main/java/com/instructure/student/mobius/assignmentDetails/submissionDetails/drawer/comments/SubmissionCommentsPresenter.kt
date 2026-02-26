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
 */
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments

import android.content.Context
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.room.studentdb.StudentDb
import com.instructure.pandautils.utils.color
import com.instructure.student.R
import com.instructure.student.mobius.common.ui.Presenter
import kotlinx.coroutines.runBlocking
import java.util.Date

class SubmissionCommentsPresenter(private val studentDb: StudentDb) : Presenter<SubmissionCommentsModel, SubmissionCommentsViewState> {

    private fun Date?.getSubmissionFormattedDate(context: Context): String {
        val atSeparator = context.getString(R.string.at)
        return DateHelper.getMonthDayAtTime(context, this, atSeparator) ?: ""
    }

    override fun present(
        model: SubmissionCommentsModel,
        context: Context
    ): SubmissionCommentsViewState {
        val self = ApiPrefs.user ?: return SubmissionCommentsViewState(
            false,
            listOf(CommentItemState.Empty)
        )

        val tint = CanvasContext.emptyCourseContext(model.assignment.courseId).color

        val comments = model.comments.filter {
            it.attempt == null ||
                    it.attempt == model.attemptId ||
                    !model.assignmentEnhancementsEnabled ||
                    (it.attempt == 0L && model.attemptId == 1L)
        }.map { comment ->
            val date = comment.createdAt ?: Date(0)
            CommentItemState.CommentItem(
                id = comment.id,
                authorName = comment.author?.displayName.orEmpty(),
                authorPronouns = comment.author?.pronouns,
                avatarUrl = comment.author?.avatarImageUrl.orEmpty(),
                sortDate = date,
                dateText = date.getSubmissionFormattedDate(context),
                message = comment.comment.orEmpty(),
                isAudience = comment.authorId != self.id,
                media = comment.mediaComment,
                attachments = comment.attachments,
                tint = tint
            )
        }

        val submissions = model.submissionHistory
            .filter { it.attempt == model.attemptId || !model.assignmentEnhancementsEnabled }
            .filter { it.workflowState != "unsubmitted" && it.submissionType != null }
            .map { submission ->
                val date = submission.submittedAt ?: Date(0)
                CommentItemState.SubmissionItem(
                    authorName = self.shortName ?: self.name,
                    authorPronouns = self.pronouns,
                    avatarUrl = self.avatarUrl.orEmpty(),
                    sortDate = date,
                    dateText = date.getSubmissionFormattedDate(context),
                    submission = submission,
                    tint = tint
                )
            }
        val pendingItems = runBlocking {
            studentDb
                .pendingSubmissionCommentDao()
                .findCommentsByAccountAndAssignmentId(ApiPrefs.domain, model.assignment.id)
                .filter { it.attemptId == model.attemptId || !model.assignmentEnhancementsEnabled }
                .map { pendingComment ->
                    val date = Date(pendingComment.lastActivityDate.toInstant().toEpochMilli())
                    CommentItemState.PendingCommentItem(
                        authorName = self.shortName ?: self.name,
                        authorPronouns = self.pronouns,
                        avatarUrl = self.avatarUrl.orEmpty(),
                        sortDate = date,
                        pendingComment = pendingComment
                    )
                }
        }

        val items = (comments + submissions + pendingItems).sortedByDescending {
            when (it) {
                is CommentItemState.CommentItem -> it.sortDate
                is CommentItemState.PendingCommentItem -> it.sortDate
                is CommentItemState.SubmissionItem -> it.sortDate
                else -> throw IllegalArgumentException("Unhandled CommentItemState type: ${it::class.java.simpleName}")
            }
        }

        return SubmissionCommentsViewState(
            model.isFileButtonEnabled,
            items.takeUnless { it.isEmpty() } ?: listOf(CommentItemState.Empty)
        )
    }
}
