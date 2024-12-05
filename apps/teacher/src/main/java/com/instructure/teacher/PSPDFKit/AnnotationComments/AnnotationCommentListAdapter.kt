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
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.teacher.databinding.AdapterAnnotationCommentBinding
import com.instructure.pandautils.blueprint.ListRecyclerAdapter

class AnnotationCommentListAdapter(
        context: Context,
        presenter: AnnotationCommentListPresenter,
        private val editCallback: (CanvaDocAnnotation, Int) -> Unit,
        private val deleteCallback: (CanvaDocAnnotation, Int) -> Unit
) : ListRecyclerAdapter<CanvaDocAnnotation, AnnotationCommentViewHolder, AnnotationCommentListView>(context, presenter) {

    override fun createViewHolder(binding: ViewBinding, viewType: Int) = AnnotationCommentViewHolder(binding as AdapterAnnotationCommentBinding)

    override fun bindingInflater(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> ViewBinding = AdapterAnnotationCommentBinding::inflate

    override fun bindHolder(model: CanvaDocAnnotation, holder: AnnotationCommentViewHolder, position: Int) {
        val annotationCommentPresenter = presenter as AnnotationCommentListPresenter
        val canDelete = annotationCommentPresenter.docSession.annotationMetadata?.canManage() == true
                || (annotationCommentPresenter.docSession.annotationMetadata?.canWrite() == true
                && model.userId == annotationCommentPresenter.docSession.annotationMetadata?.userId)
        val canEdit = annotationCommentPresenter.docSession.annotationMetadata?.canWrite() == true
                && model.userId == (annotationCommentPresenter.docSession.annotationMetadata?.userId)

        holder.bind(model, canEdit, canDelete, editCallback, deleteCallback)
    }
}
