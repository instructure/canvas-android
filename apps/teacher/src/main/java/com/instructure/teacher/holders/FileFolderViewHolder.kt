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
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.isValid
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setInvisible
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import kotlinx.android.synthetic.main.adapter_file_folder.view.*

class FileFolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(item: FileFolder, courseColor: Int, context: Context, callback: (FileFolder) -> Unit) = with(itemView){
        fileFolderLayout.onClick { callback(item) }
        fileIconOrImage.setPublishedStatus(item) // Locked files are "unpublished"

        // Set publish status side bar color
        if (!item.isLocked && !item.isHidden && item.lockDate == null && item.unlockDate == null) {
            // Published
            publishedBar.setVisible()
            restrictedBar.setGone()
        } else if (item.isLocked) {
            // Unpublished
            publishedBar.setInvisible()
            restrictedBar.setGone()
        } else if (item.isHidden || item.lockDate != null || item.unlockDate != null) {
            // Restricted
            publishedBar.setGone()
            restrictedBar.setVisible()
        }

        if(item.displayName.isValid()) { // This is a file
            fileName.text = item.displayName
            fileSize.text = NumberHelper.readableFileSize(context, item.size)
            if(item.thumbnailUrl.isValid()) {
                fileIconOrImage.setImage(item.thumbnailUrl!!)
            } else {
                val contentType = item.contentType.orEmpty()
                when {
                    contentType.contains("pdf") -> fileIconOrImage.setIcon(R.drawable.vd_pdf, courseColor)
                    contentType.contains("presentation") -> fileIconOrImage.setIcon(R.drawable.vd_ppt, courseColor)
                    contentType.contains("spreadsheet") -> fileIconOrImage.setIcon(R.drawable.vd_spreadsheet, courseColor)
                    contentType.contains("wordprocessing") -> fileIconOrImage.setIcon(R.drawable.vd_word_doc, courseColor)
                    contentType.contains("zip") -> fileIconOrImage.setIcon(R.drawable.vd_zip, courseColor)
                    contentType.contains("image") -> fileIconOrImage.setIcon(R.drawable.vd_image, courseColor)
                    else -> fileIconOrImage.setIcon(R.drawable.vd_document, courseColor)
                }
            }
        } else { // This is a folder
            fileName.text = item.name
            fileSize.text = ""
            fileIconOrImage.setIcon(R.drawable.vd_folder_solid, courseColor)
        }
    }

    companion object {
        const val HOLDER_RES_ID: Int = R.layout.adapter_file_folder
    }
}
