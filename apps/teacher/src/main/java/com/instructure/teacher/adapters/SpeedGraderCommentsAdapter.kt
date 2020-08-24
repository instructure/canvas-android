/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.adapters

import android.content.Context
import android.view.View
import com.instructure.canvasapi2.models.Assignee
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.teacher.holders.SpeedGraderCommentHolder
import com.instructure.teacher.models.SubmissionCommentWrapper
import com.instructure.teacher.presenters.SpeedGraderCommentsPresenter
import com.instructure.teacher.viewinterface.SpeedGraderCommentsView
import instructure.androidblueprint.ListRecyclerAdapter

class SpeedGraderCommentsAdapter(
        context: Context,
        presenter: SpeedGraderCommentsPresenter,
        val courseId: Long,
        val assignee: Assignee,
        val gradeAnonymously: Boolean,
        val onAttachmentClicked: (Attachment) -> Unit
) : ListRecyclerAdapter<SubmissionCommentWrapper, SpeedGraderCommentHolder, SpeedGraderCommentsView>(context, presenter) {
    private val currentUser = ApiPrefs.user ?: throw IllegalStateException("Current user must not be null")

    override fun itemLayoutResId(viewType: Int) = SpeedGraderCommentHolder.HOLDER_RES_ID

    override fun createViewHolder(v: View, viewType: Int) = SpeedGraderCommentHolder(v)

    override fun bindHolder(model: SubmissionCommentWrapper, holder: SpeedGraderCommentHolder, position: Int) {
        val presenter = presenter as SpeedGraderCommentsPresenter
        holder.bind(model, currentUser, courseId, assignee, gradeAnonymously, onAttachmentClicked, presenter)
    }
}
