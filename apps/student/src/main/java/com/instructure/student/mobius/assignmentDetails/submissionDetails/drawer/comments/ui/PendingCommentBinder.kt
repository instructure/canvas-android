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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.ui

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.PendingSubmissionComment
import com.instructure.student.R
import com.instructure.pandautils.adapters.BasicItemBinder
import com.instructure.student.db.Db
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.CommentItemState
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.ui.views.CommentDirection
import com.squareup.sqldelight.Query
import kotlinx.android.synthetic.main.adapter_submission_comment_pending.view.*

class PendingCommentBinder : BasicItemBinder<CommentItemState.PendingCommentItem, SubmissionCommentsAdapterCallback>() {

    override val layoutResId = R.layout.adapter_submission_comment_pending

    override fun constructViewHolder(context: Context, view: View): RecyclerView.ViewHolder {
        return PendingCommentHolder(view)
    }

    override val bindBehavior = ItemWithHolder { holder, item, callback, _ ->
        if (holder !is PendingCommentHolder) throw IllegalStateException("Invalid holder type for PendingCommentBinder")

        commentHolder.direction = CommentDirection.OUTGOING
        commentHolder.usernameText = item.authorName
        commentHolder.setAvatar(item.avatarUrl, item.authorName)

        holder.setListenerForItem(item.pendingComment.id) { comment ->
            commentHolder.commentText = comment.message
            if (comment.errorFlag) {
                onClick { callback.onRetryComment(comment.id) }
                commentHolder.dateText = context.getString(R.string.error)
                errorLayout.setVisible()
                sendingLayout.setGone()
            } else {
                setOnClickListener(null)
                commentHolder.dateText = context.getString(R.string.sending)
                errorLayout.setGone()
                sendingLayout.setVisible()
                if (comment.fileCount == 0L) {
                    indeterminateProgressBar.setVisible()
                    progressBar.setGone()
                } else {
                    indeterminateProgressBar.setGone()
                    progressBar.setVisible()
                    progressBar.max = 1000
                    val progress = 1000 * (comment.progress ?: 0.0)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressBar.setProgress(progress.toInt(), true)
                    } else {
                        progressBar.progress = progress.toInt()
                    }
                }
            }
        }
    }

    override fun onRecycle(holder: RecyclerView.ViewHolder) {
        (holder as? PendingCommentHolder)?.clearListener()
    }

    private class PendingCommentHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val db = Db.instance.pendingSubmissionCommentQueries

        private var listener: Query.Listener? = null

        private var query: Query<PendingSubmissionComment>? = null

        fun clearListener() = listener?.let { query?.removeListener(it) }

        fun setListenerForItem(itemId: Long, onUpdate: (PendingSubmissionComment) -> Unit) {
            clearListener()
            query = db.getCommentById(itemId)
            listener = object : Query.Listener {
                override fun queryResultsChanged() {
                    query?.executeAsOneOrNull()?.let {
                        Handler(Looper.getMainLooper()).post { onUpdate(it) }
                    }
                }
            }
            query!!.addListener(listener!!)
            query!!.executeAsOneOrNull()?.let { onUpdate(it) }
        }
    }
}
