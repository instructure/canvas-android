/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.emeritus.student.AnnotationComments

import android.content.Context
import android.view.View
import com.instructure.canvasapi2.models.DocSession
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.emeritus.student.adapter.BaseListRecyclerAdapter

class AnnotationCommentListRecyclerAdapter(
        context: Context,
        private val docSession: DocSession,
        private val editCallback: (CanvaDocAnnotation, Int) -> Unit,
        private val deleteCallback: (CanvaDocAnnotation, Int) -> Unit
) : BaseListRecyclerAdapter<CanvaDocAnnotation, AnnotationCommentViewHolder>(context, CanvaDocAnnotation::class.java) {

    init {
        itemCallback = object : ItemComparableCallback<CanvaDocAnnotation>() {
            override fun compare(item1: CanvaDocAnnotation, item2: CanvaDocAnnotation): Int {
                val createdAt1 = item1.createdAt
                val createdAt2 = item2.createdAt

                return if(createdAt1 != null && createdAt2 != null) {
                    createdAt1.compareTo(createdAt2)
                } else if(createdAt1 != null && createdAt2 == null) {
                    1
                } else {
                    -1
                }
            }
        }
    }

    override fun createViewHolder(v: View, viewType: Int) = AnnotationCommentViewHolder(v)
    override fun itemLayoutResId(viewType: Int) = AnnotationCommentViewHolder.holderRes
    override fun bindHolder(model: CanvaDocAnnotation, holder: AnnotationCommentViewHolder, position: Int) {
        val canDelete = docSession.annotationMetadata?.canManage() ?: false
                || (docSession.annotationMetadata?.canWrite() == true && model.userId == docSession.annotationMetadata?.userId)
        val canEdit = (docSession.annotationMetadata?.canWrite() == true
                && model.userId == docSession.annotationMetadata?.userId)
        holder.bind(model, canEdit, canDelete, editCallback, deleteCallback)
    }
}
