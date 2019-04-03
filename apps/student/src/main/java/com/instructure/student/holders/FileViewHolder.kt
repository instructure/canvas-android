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

package com.instructure.student.holders

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.bumptech.glide.Glide
import com.instructure.student.R
import com.instructure.student.adapter.FileFolderCallback
import com.instructure.student.fragment.FileListFragment
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.isValid
import com.instructure.pandautils.utils.*
import kotlinx.android.synthetic.main.viewholder_file.view.*
import java.text.DecimalFormat

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
            fileSize.text = readableFileSize(context, item.size)
            if (item.thumbnailUrl.isValid()) {
                val glide = Glide.with(context)
                glide.clear(fileIcon)
                glide.load(item.thumbnailUrl).into(fileIcon)
            } else {
                val contentType = item.contentType.orEmpty()
                val iconRes = when {
                    contentType.contains("pdf") -> R.drawable.vd_pdf
                    contentType.contains("presentation") -> R.drawable.vd_ppt
                    contentType.contains("spreadsheet") -> R.drawable.vd_spreadsheet
                    contentType.contains("wordprocessing") -> R.drawable.vd_word_doc
                    contentType.contains("zip") -> R.drawable.vd_zip
                    contentType.contains("image") -> R.drawable.vd_image
                    else -> R.drawable.vd_document
                }
                fileIcon.setColoredResource(iconRes, tint)
            }
        } else {
            // This is a folder
            fileName.text = item.name
            fileName.contentDescription = itemView.resources.getString(R.string.folderTalkBack, item.name)
            val itemCount = item.filesCount + item.foldersCount;
            fileSize.text = context.resources.getQuantityString(R.plurals.item_count, itemCount, itemCount);
            fileIcon.setColoredResource(R.drawable.vd_folder_solid, tint)
        }
    }

    companion object {
        const val HOLDER_RES_ID = R.layout.viewholder_file
    }

    // Helper function to make the size of a file look better
    private fun readableFileSize(context: Context, size: Long): String {
        val units = context.resources.getStringArray(R.array.file_size_units)
        var digitGroups = 0
        if (size > 0) digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        val displaySize = size / Math.pow(1024.0, digitGroups.toDouble())
        return DecimalFormat("#,##0.#").format(displaySize) + " " + units[digitGroups]
    }
}
