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
package com.instructure.teacher.PSPDFKit.AnnotationComments

import android.content.Context
import android.view.View
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import instructure.androidblueprint.ListRecyclerAdapter

class AnnotationCommentListAdapter(
        context: Context,
        presenter: AnnotationCommentListPresenter,
        private val editCallback: (CanvaDocAnnotation, Int) -> Unit,
        private val deleteCallback: (CanvaDocAnnotation, Int) -> Unit
) : ListRecyclerAdapter<CanvaDocAnnotation, AnnotationCommentViewHolder, AnnotationCommentListView>(context, presenter) {

    override fun createViewHolder(v: View, viewType: Int): AnnotationCommentViewHolder = AnnotationCommentViewHolder(v)
    override fun itemLayoutResId(viewType: Int) = AnnotationCommentViewHolder.HOLDER_RES
    override fun bindHolder(model: CanvaDocAnnotation, holder: AnnotationCommentViewHolder, position: Int) {
        val canDelete = (presenter as AnnotationCommentListPresenter).docSession.annotationMetadata?.canManage() == true
                || (presenter.docSession.annotationMetadata?.canWrite() == true
                && model.userId == presenter.docSession.annotationMetadata?.userId)
        val canEdit = presenter.docSession.annotationMetadata?.canWrite() == true
                && model.userId == (presenter.docSession.annotationMetadata?.userId)

        holder.bind(model, canEdit, canDelete, editCallback, deleteCallback)
    }
}
