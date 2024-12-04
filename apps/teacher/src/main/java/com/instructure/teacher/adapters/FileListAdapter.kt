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
package com.instructure.teacher.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.teacher.databinding.AdapterFileFolderBinding
import com.instructure.teacher.holders.FileFolderViewHolder
import com.instructure.teacher.presenters.FileListPresenter
import com.instructure.teacher.viewinterface.FileListView
import com.instructure.pandautils.blueprint.SyncRecyclerAdapter

class FileListAdapter(
    private val mContext: Context,
    private val iconColor: Int,
    presenter: FileListPresenter,
    private val mCallback: (FileFolder) -> Unit
) : SyncRecyclerAdapter<FileFolder, FileFolderViewHolder, FileListView>(mContext, presenter) {

    override fun bindHolder(model: FileFolder, holder: FileFolderViewHolder, position: Int) {
        holder.bind(model, iconColor, mContext, mCallback)
    }

    override fun createViewHolder(binding: ViewBinding, viewType: Int) = FileFolderViewHolder(binding as AdapterFileFolderBinding)

    override fun bindingInflater(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> ViewBinding = AdapterFileFolderBinding::inflate
}
