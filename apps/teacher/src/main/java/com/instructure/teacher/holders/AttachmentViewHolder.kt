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
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Attachment
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.iconRes
import com.instructure.pandautils.utils.loadUri
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.utils.isMediaSubmissionPlaceholder
import kotlinx.android.synthetic.main.adapter_attachment.view.*

class AttachmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        const val HOLDER_RES_ID = R.layout.adapter_attachment
    }

    fun bind(context: Context, position: Int, attachment: Attachment, isSelected: Boolean, callback: (Attachment) -> Unit, selectionCallback: (Int) -> Unit) = with(itemView) {
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
