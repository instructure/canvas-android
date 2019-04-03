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
package com.instructure.pandautils.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.onClick
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import kotlinx.android.synthetic.main.view_attachment.view.*
import java.io.File

class AttachmentView(context: Context) : FrameLayout(context) {

    enum class AttachmentAction { PREVIEW, DOWNLOAD, REMOVE }

    init {
        LayoutInflater.from(getContext()).inflate(R.layout.view_attachment, this, true)
    }

    fun setPendingRemoteFile(
            attachment: RemoteFile,
            removeViewOnAction: Boolean,
            callback: (action: AttachmentAction, attachment: RemoteFile) -> Unit
    ) {
        attachmentName.text = attachment.displayName
        setColorAndIcon(context, attachment.contentType, attachment.fileName, previewImage, attachmentIcon)
        setThumbnail(attachment.thumbnailUrl)
        actionButton.setImageResource(R.drawable.vd_close_white)
        actionButton.contentDescription = context.getString(R.string.utils_removeAttachment)
        actionButton.onClick {
            if (removeViewOnAction) (parent as? ViewGroup)?.removeView(this@AttachmentView)
            callback(AttachmentAction.REMOVE, attachment)
        }
    }

    fun setPendingAttachment(
            attachment: Attachment,
            removeViewOnAction: Boolean,
            callback: (action: AttachmentAction, attachment: Attachment) -> Unit
    ) {
        attachmentName.text = attachment.displayName
        setColorAndIcon(context, attachment.contentType, attachment.filename, previewImage, attachmentIcon)
        setThumbnail(attachment.thumbnailUrl)
        actionButton.setImageResource(R.drawable.vd_close_white)
        actionButton.contentDescription = context.getString(R.string.utils_removeAttachment)
        actionButton.onClick {
            if (removeViewOnAction) (parent as? ViewGroup)?.removeView(this@AttachmentView)
            callback(AttachmentAction.REMOVE, attachment)
        }
    }

    fun setAttachment(attachment: Attachment, callback: (action: AttachmentAction, attachment: Attachment) -> Unit) {
        attachmentName.text = attachment.displayName
        setColorAndIcon(context, attachment.contentType, attachment.filename, previewImage, attachmentIcon)
        setThumbnail(attachment.thumbnailUrl)
        onClick { callback(AttachmentAction.PREVIEW, attachment) }
        actionButton.setImageResource(R.drawable.vd_utils_download)
        actionButton.setOnClickListener { callback(AttachmentAction.DOWNLOAD, attachment) }
    }

    fun setAttachment(attachment: RemoteFile, callback: (action: AttachmentAction, attachment: RemoteFile) -> Unit) {
        attachmentName.text = attachment.displayName
        setColorAndIcon(context, attachment.contentType, attachment.fileName, previewImage, attachmentIcon)
        setThumbnail(attachment.thumbnailUrl)
        onClick { callback(AttachmentAction.PREVIEW, attachment) }
        actionButton.setImageResource(R.drawable.vd_utils_download)
        actionButton.setOnClickListener { callback(AttachmentAction.DOWNLOAD, attachment) }
    }

    private fun setThumbnail(path: String?) {
        if (path.isNullOrBlank()) return
        val file = File(path)
        val picasso = Picasso.with(context)
        val creator = if (file.exists() && file.isFile) picasso.load(file) else picasso.load(path)
        creator.fit().centerCrop().transform(ATTACHMENT_PREVIEW_TRANSFORMER).into(previewImage)
    }

    companion object {
        /** Picasso transformation to apply gray overlay on thumbnail */
        @JvmField
        val ATTACHMENT_PREVIEW_TRANSFORMER: Transformation = object : Transformation {
            override fun transform(source: Bitmap?): Bitmap? {
                if (source == null) return null
                val mutableSource = source.copy(source.config, true)
                source.recycle()
                val canvas = Canvas(mutableSource)
                canvas.drawColor(0xBB9B9B9B.toInt())
                return mutableSource
            }

            override fun key(): String = "gray-overlay"
        }

        @JvmStatic
        fun setColorAndIcon(
                context: Context,
                contentType: String?,
                filename: String?,
                preview: ImageView?,
                icon: ImageView?
        ) {
            val type = contentType.orEmpty()
            val name = filename.orEmpty()
            val (colorRes, iconRes) = when {
                type.startsWith("image") -> R.color.attachmentColorImage to R.drawable.vd_utils_image
                type.startsWith("video") -> R.color.attachmentColorVideo to R.drawable.vd_utils_media
                type.startsWith("audio") -> R.color.attachmentColorAudio to R.drawable.vd_utils_audio
                else -> when (name.substringAfterLast(".")) {
                    "doc", "docx" -> R.color.attachmentColorDoc to R.drawable.vd_utils_document
                    "txt", "rtf" -> R.color.attachmentColorTxt to R.drawable.vd_utils_document
                    "pdf" -> R.color.attachmentColorPdf to R.drawable.vd_utils_document
                    "xls" -> R.color.attachmentColorXls to R.drawable.vd_utils_document
                    "zip", "tar", "7z", "apk", "jar", "rar" -> R.color.attachmentColorZip to R.drawable.vd_utils_attachment
                    else -> R.color.attachmentColorMisc to R.drawable.vd_utils_attachment
                }
            }
            preview?.setBackgroundColor(ContextCompat.getColor(context, colorRes))
            icon?.setImageResource(iconRes)
        }
    }
}
