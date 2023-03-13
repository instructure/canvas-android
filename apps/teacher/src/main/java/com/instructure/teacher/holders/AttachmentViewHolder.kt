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
package com.instructure.teacher.holders

import android.content.Context
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Attachment
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.iconRes
import com.instructure.pandautils.utils.loadUri
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.databinding.AdapterAttachmentBinding
import com.instructure.teacher.utils.isMediaSubmissionPlaceholder

class AttachmentViewHolder(private val binding: AdapterAttachmentBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        context: Context,
        position: Int,
        attachment: Attachment,
        isSelected: Boolean,
        callback: (Attachment) -> Unit,
        selectionCallback: (Int) -> Unit
    ) = with(binding) {
        //check if its the selected item
        if(isSelected) {
            isSelectedIcon.setVisible(true)
            isSelectedIcon.setColorFilter(ThemePrefs.brandColor)
        } else {
            isSelectedIcon.setVisible(false)
        }

        if (attachment.isMediaSubmissionPlaceholder) {
            fileNameText.text = context.getString(R.string.speedGraderMediaFile)
        } else {
            fileNameText.text = attachment.displayName ?: attachment.filename
        }

        /*error image is placeholder, we need a design on this, either for all file types or an
            error file drawable */
        if(attachment.thumbnailUrl != null) {
            fileIcon.loadUri(Uri.parse(attachment.thumbnailUrl))
        } else {
            fileIcon.setImageResource(attachment.iconRes)
        }

        itemView.setOnClickListener {
            // Skip callback if this is a media submission placeholder
            if (attachment.isMediaSubmissionPlaceholder) return@setOnClickListener
            callback(attachment)
            selectionCallback(position)
        }
    }
}
