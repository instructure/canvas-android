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
package com.instructure.teacher.features.files.search

import android.content.Context
import android.view.View
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.teacher.holders.FileFolderViewHolder
import instructure.androidblueprint.SyncRecyclerAdapter

class FileSearchAdapter(
    context: Context,
    private val courseTextColor: Int,
    presenter: FileSearchPresenter,
    private val callback: (FileFolder) -> Unit
) : SyncRecyclerAdapter<FileFolder, FileFolderViewHolder, FileSearchView>(context, presenter) {

    override fun itemLayoutResId(viewType: Int) = FileFolderViewHolder.HOLDER_RES_ID

    override fun createViewHolder(v: View, viewType: Int) = FileFolderViewHolder(v)

    override fun bindHolder(model: FileFolder, holder: FileFolderViewHolder, position: Int) {
        holder.bind(model, courseTextColor, context!!, callback)
    }

}
