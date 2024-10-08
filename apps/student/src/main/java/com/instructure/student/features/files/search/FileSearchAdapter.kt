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
package com.instructure.student.features.files.search

import android.content.Context
import android.view.View
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.utils.color
import com.instructure.student.adapter.BaseListRecyclerAdapter
import com.instructure.student.features.files.list.FileFolderCallback
import com.instructure.student.holders.FileViewHolder

class FileSearchAdapter(
    context: Context,
    private val canvasContext: CanvasContext,
    private val fileSearchRepository: FileSearchRepository,
    private val viewCallback: FileSearchView
) : BaseListRecyclerAdapter<FileFolder, FileViewHolder>(context, FileFolder::class.java) {

    private var apiCall: WeaveJob? = null

    private val callback = object : FileFolderCallback {
        override fun onOpenItemMenu(item: FileFolder, anchorView: View) = Unit
        override fun onRefreshFinished() = Unit
        override fun onItemClicked(item: FileFolder) = viewCallback.fileClicked(item)
    }

    val isEmpty get() = size() == 0

    var searchQuery: String = ""
        set(value) {
            field = value
            if (value.isBlank()) {
                apiCall?.cancel()
                clear()
                viewCallback.onRefreshFinished()
                viewCallback.checkIfEmpty()
            } else {
                performSearch()
            }
        }

    init {
        itemCallback = object : BaseListRecyclerAdapter.ItemComparableCallback<FileFolder>() {
            override fun compare(o1: FileFolder, o2: FileFolder) = o1.compareTo(o2)
            override fun areContentsTheSame(item1: FileFolder, item2: FileFolder) = compareFileFolders(item1, item2)
            override fun areItemsTheSame(item1: FileFolder, item2: FileFolder) = item1.id == item2.id
            override fun getUniqueItemId(fileFolder: FileFolder) = fileFolder.id
        }
        loadData()
    }

    override fun bindHolder(item: FileFolder, holder: FileViewHolder, position: Int) {
        holder.bind(item, canvasContext.color, context, emptyList(), callback)
    }

    override fun createViewHolder(v: View, viewType: Int) = FileViewHolder(v)

    override fun itemLayoutResId(viewType: Int) = FileViewHolder.HOLDER_RES_ID

    override val isPaginated get() = false

    private fun performSearch() {
        apiCall = tryWeave {
            viewCallback.onRefreshStarted()
            val files = fileSearchRepository.searchFiles(canvasContext, searchQuery)
            clear()
            addAll(files)
            viewCallback.onRefreshFinished()
            viewCallback.checkIfEmpty()
        } catch {
            Logger.e("Error performing file search: " + it.message)
            viewCallback.displayError()
        }
    }

    private fun compareFileFolders(oldItem: FileFolder, newItem: FileFolder): Boolean {
        val sameName = oldItem.displayName == newItem.displayName
        val sameSize = oldItem.size == newItem.size
        return sameName && sameSize
    }

    override fun cancel() {
        apiCall?.cancel()
    }

}
