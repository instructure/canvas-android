/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.emeritus.student.holders

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.isValid
import com.instructure.pandautils.utils.*
import com.emeritus.student.R
import com.emeritus.student.adapter.FileFolderCallback
import com.emeritus.student.fragment.FileListFragment
import kotlinx.android.synthetic.main.viewholder_file.view.*

class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(item: FileFolder, tint: Int, context: Context, hasOptions: List<FileListFragment.FileMenuType>, callback: FileFolderCallback): Unit = with(itemView) {
        // Set up click listeners
        onClick { callback.onItemClicked(item) }
        onLongClick { overflowButton.performClick() }

        if (hasOptions.isNotEmpty()) {
            // User has options
            overflowButton.setVisible()
            overflowButton.onClick { callback.onOpenItemMenu(item, overflowButton) }
        } else {
            // No options, don't show the... "kabob"
            overflowButton.setGone()
            overflowButton.setOnClickListener(null)
        }

        if (item.displayName.isValid()) {
            // This is a file
            fileName.text = item.displayName
            fileName.contentDescription = itemView.resources.getString(R.string.fileTalkBack, item.displayName)
            fileSize.text = NumberHelper.readableFileSize(context, item.size)
            if (item.thumbnailUrl.isValid()) {
                val glide = Glide.with(context)
                glide.clear(fileIcon)
                glide.load(item.thumbnailUrl).into(fileIcon)
            } else {
                val contentType = item.contentType.orEmpty()
                val iconRes = when {
                    contentType.contains("pdf") -> R.drawable.ic_pdf
                    contentType.contains("presentation") -> R.drawable.ic_ppt
                    contentType.contains("spreadsheet") -> R.drawable.ic_spreadsheet
                    contentType.contains("wordprocessing") -> R.drawable.ic_word_doc
                    contentType.contains("zip") -> R.drawable.ic_zip
                    contentType.contains("image") -> R.drawable.ic_image
                    else -> R.drawable.ic_document
                }
                fileIcon.setColoredResource(iconRes, tint)
            }
        } else {
            // This is a folder
            fileName.text = item.name
            fileName.contentDescription = itemView.resources.getString(R.string.folderTalkBack, item.name)
            val itemCount = item.filesCount + item.foldersCount;
            fileSize.text = context.resources.getQuantityString(R.plurals.item_count, itemCount, itemCount);
            fileIcon.setColoredResource(R.drawable.ic_folder_solid, tint)
        }
    }

    companion object {
        const val HOLDER_RES_ID = R.layout.viewholder_file
    }
}
