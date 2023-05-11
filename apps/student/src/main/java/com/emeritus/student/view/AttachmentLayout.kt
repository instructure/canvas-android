/*
 * Copyright (C) 2018 - present  Instructure, Inc.
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
 */
package com.emeritus.student.view

import android.content.Context
import androidx.core.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.emeritus.student.R
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.pandautils.utils.children
import com.instructure.pandautils.utils.obtainFor

class AttachmentLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private var spacing: Int = 0
    private var columnCount = 0
    private var previewChildCount = 3

    init {
        attrs?.obtainFor(this, R.styleable.AttachmentLayout) { a, idx ->
            when (idx) {
                R.styleable.AttachmentLayout_al_previewChildCount ->
                    previewChildCount = a.getInteger(idx, previewChildCount)
                R.styleable.AttachmentLayout_al_spacing -> spacing = a.getDimension(idx, 0f).toInt()
            }
        }

        // Add dummy child views in preview mode
        if (isInEditMode && childCount == 0) repeat(previewChildCount) { addView(AttachmentView(context)) }
    }

    fun setPendingRemoteFiles(
        attachments: List<RemoteFile>,
        removeViewOnAction: Boolean,
        callback: (action: AttachmentView.AttachmentAction, attachment: RemoteFile) -> Unit
    ) {
        removeAllViews()
        for (attachment in attachments) {
            val attachmentView = AttachmentView(context)
            attachmentView.setPendingRemoteFile(attachment, removeViewOnAction, callback)
            addView(attachmentView)
        }
    }

    fun setPendingAttachments(
        attachments: List<Attachment>,
        removeViewOnAction: Boolean,
        callback: (action: AttachmentView.AttachmentAction, attachment: Attachment) -> Unit
    ) {
        removeAllViews()
        for (attachment in attachments) {
            val attachmentView = AttachmentView(context)
            attachmentView.setPendingAttachment(attachment, removeViewOnAction, callback)
            addView(attachmentView)
        }
    }

    fun setAttachments(
        attachments: List<Attachment>,
        callback: (action: AttachmentView.AttachmentAction, attachment: Attachment) -> Unit
    ) {
        removeAllViews()
        for (attachment in attachments) {
            val attachmentView = AttachmentView(context)
            attachmentView.setAttachment(attachment, callback)
            addView(attachmentView)
        }
    }

    fun setAttachment(
        attachment: Attachment,
        callback: (action: AttachmentView.AttachmentAction, attachment: Attachment) -> Unit
    ) {
        removeAllViews()
        val attachmentView = AttachmentView(context)
        attachmentView.setAttachment(attachment, callback)
        addView(attachmentView)
    }

    fun setRemoteFileAttachments(
        attachments: List<RemoteFile>,
        callback: (action: AttachmentView.AttachmentAction, attachment: RemoteFile) -> Unit
    ) {
        removeAllViews()
        for (attachment in attachments) {
            val attachmentView = AttachmentView(context)
            attachmentView.setAttachment(attachment, callback)
            addView(attachmentView)
        }
    }

    fun clearAttachmentViews() {
        removeAllViews()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // Skip layout if there are no children
        if (childCount == 0) return

        val childWidth = getChildAt(0).measuredWidth
        val childHeight = getChildAt(0).measuredHeight

        val start = paddingStart
        val top = paddingTop

        var row: Int
        var column: Int
        var childStart: Int
        var childTop: Int
        var childEnd: Int
        var childBottom: Int

        for (i in 0 until childCount) {
            row = i / columnCount
            column = i % columnCount

            childStart = start + column * childWidth + column * spacing
            childTop = top + row * childHeight + row * spacing
            childEnd = childStart + childWidth
            childBottom = childTop + childHeight

            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                getChildAt(i).layout(width - childEnd, childTop, width - childStart, childBottom)
            } else {
                getChildAt(i).layout(childStart, childTop, childEnd, childBottom)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // No dimensions if there are no children
        if (childCount == 0) {
            setMeasuredDimension(0, 0)
            return
        }

        // Ensure we're working with homogeneous child views
        if (children.any { it !is AttachmentView }) throw IllegalStateException("AttachmentLayout can only contain views of type AttachmentView")

        // Establish max available width
        val widthSpec = View.MeasureSpec.getSize(widthMeasureSpec)

        // Get padding
        val horizontalPadding = paddingStart + paddingEnd
        val verticalPadding = paddingBottom + paddingTop

        // Make sure the children get measured
        measureChildren(widthMeasureSpec, heightMeasureSpec)

        // Get child height. Children are homogeneous and should all have the same measured dimensions
        val firstChild = getChildAt(0)
        val childWidth = firstChild.measuredWidth
        val childHeight = firstChild.measuredHeight

        // Determine how many columns we can fit
        columnCount = 1
        while (horizontalPadding + (columnCount + 1) * childWidth + columnCount * spacing < widthSpec) {
            columnCount++
        }

        // Determine how many rows we need
        val rowCount = 1 + (childCount - 1) / columnCount

        // Calculate final dimensions
        var measureWidth = horizontalPadding + columnCount * childWidth + (columnCount - 1) * spacing


        if (layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT || layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            measureWidth = Math.max(widthSpec, measureWidth)
        }

        val measureHeight = verticalPadding + rowCount * childHeight + (rowCount - 1) * spacing

        setMeasuredDimension(measureWidth, measureHeight)
    }
}
