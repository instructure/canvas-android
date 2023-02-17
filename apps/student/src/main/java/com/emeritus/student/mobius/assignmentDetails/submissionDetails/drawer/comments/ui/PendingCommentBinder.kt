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
package com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.comments.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.pandautils.adapters.BasicItemBinder
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.emeritus.student.PendingSubmissionComment
import com.emeritus.student.R
import com.emeritus.student.db.Db
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.comments.CommentItemState
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.comments.ui.views.CommentDirection
import com.squareup.sqldelight.Query
import kotlinx.android.synthetic.main.adapter_submission_comment_pending.view.*

class PendingCommentBinder : BasicItemBinder<CommentItemState.PendingCommentItem, SubmissionCommentsAdapterCallback>() {

    override val layoutResId = R.layout.adapter_submission_comment_pending

    override fun constructViewHolder(context: Context, view: View): RecyclerView.ViewHolder {
        return PendingCommentHolder(view)
    }

    override val bindBehavior = ItemWithHolder { holder, item, callback, _ ->
        check(holder is PendingCommentHolder) { "Invalid holder type for PendingCommentBinder" }

        commentHolder.direction = CommentDirection.OUTGOING
        commentHolder.usernameText = Pronouns.span(item.authorName, item.authorPronouns)
        commentHolder.setAvatar(item.avatarUrl, item.authorName)

        holder.setListenerForItem(item.pendingComment.id) { comment ->
            commentHolder.commentText = comment.message
            if (comment.errorFlag) {
                onClick { displayRetryOptions(errorLayout, comment.id, callback) }
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
                    progressBar.setProgress(progress.toInt(), true)
                }
            }
        }
    }

    private fun displayRetryOptions(anchor: View, commentId: Long, callback: SubmissionCommentsAdapterCallback) {
        val popup = PopupMenu(anchor.context, anchor)
        popup.menu.add(0, 0, 0, R.string.retry)
        popup.menu.add(0, 1, 0, R.string.delete).apply {
            val color = ContextCompat.getColor(anchor.context, R.color.textDanger)
            val span = SpannableString(title)
            span.setSpan(ForegroundColorSpan(color), 0, span.length, 0)
            title = span
        }
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                0 -> callback.onRetryPendingComment(commentId)
                1 -> callback.onDeletePendingComment(commentId)
            }
            true
        }
        popup.show()
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
