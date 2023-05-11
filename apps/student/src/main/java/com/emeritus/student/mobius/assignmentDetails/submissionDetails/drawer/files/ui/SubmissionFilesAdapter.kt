/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.files.ui

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.pandautils.utils.setVisible
import com.emeritus.student.R
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFileData
import kotlinx.android.synthetic.main.adapter_submission_file.view.*

internal class SubmissionFilesAdapter(val consumer: (fileId: Long) -> Unit) :
    RecyclerView.Adapter<SubmissionFilesHolder>() {

    private var files: List<SubmissionFileData> = emptyList()

    fun setData(newFiles: List<SubmissionFileData>) {
        files = newFiles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionFilesHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_submission_file, parent, false)
        return SubmissionFilesHolder(view)
    }

    override fun getItemCount() = files.size

    override fun onBindViewHolder(holder: SubmissionFilesHolder, position: Int) {
        holder.bind(files[position], consumer)
    }

}

internal class SubmissionFilesHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(data: SubmissionFileData, consumer: (fileId: Long) -> Unit) = with(itemView) {
        // File icon
        fileIcon.setImageResource(data.icon)
        fileIcon.imageTintList = ColorStateList.valueOf(data.iconColor)
        fileIcon.setVisible(!data.thumbnailUrl.isValid())

        // Thumbnail
        Glide.with(context).clear(thumbnail)
        thumbnail.setVisible(data.thumbnailUrl.isValid())
        data.thumbnailUrl.validOrNull()?.let { Glide.with(context).load(it).into(thumbnail) }

        // Title
        fileName.text = data.name

        // Selection
        selectedIcon.setVisible(data.isSelected)
        selectedIcon.imageTintList = ColorStateList.valueOf(data.selectionColor)
        setOnClickListener { consumer(data.id) }
    }
}
