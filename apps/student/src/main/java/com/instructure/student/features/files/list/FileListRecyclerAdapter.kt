/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 */
package com.instructure.student.features.files.list

import android.content.Context
import android.view.View
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.utils.textAndIconColor
import com.instructure.student.adapter.BaseListRecyclerAdapter
import com.instructure.student.holders.FileViewHolder
import com.instructure.student.util.StudentPrefs

open class FileListRecyclerAdapter(
    context: Context,
    val canvasContext: CanvasContext,
    private val possibleMenuOptions: List<FileListFragment.FileMenuType>, // Used for testing, see protected constructor below
    private val folder: FileFolder?,
    private val fileFolderCallback: FileFolderCallback,
    private val fileListRepository: FileListRepository
) : BaseListRecyclerAdapter<FileFolder, FileViewHolder>(context, FileFolder::class.java) {

    private var isTesting = false
    private val contextColor by lazy { canvasContext.textAndIconColor }

    private var apiCall: WeaveJob? = null

    /* This overloaded constructor is for testing purposes ONLY, and should not be used to create instances of this adapter. */
    protected constructor(
        context: Context,
        canvasContext: CanvasContext,
        possibleMenuOptions: List<FileListFragment.FileMenuType>,
        folder: FileFolder,
        itemCallback: FileFolderCallback,
        isTesting: Boolean,
        fileListRepository: FileListRepository
    ) : this(context, canvasContext, possibleMenuOptions, folder, itemCallback, fileListRepository) {
        this.isTesting = isTesting
    }

    init {
        itemCallback = object : ItemComparableCallback<FileFolder>() {
            override fun compare(o1: FileFolder, o2: FileFolder) = o1.compareTo(o2)
            override fun areContentsTheSame(item1: FileFolder, item2: FileFolder) = compareFileFolders(item1, item2)
            override fun areItemsTheSame(item1: FileFolder, item2: FileFolder) = item1.id == item2.id
            override fun getUniqueItemId(fileFolder: FileFolder) = fileFolder.id
        }
        if (!isTesting) loadData()
    }

    override fun bindHolder(item: FileFolder, holder: FileViewHolder, position: Int) {
        holder.bind(item, contextColor, context, FileListFragment.getFileMenuOptions(item, canvasContext, fileListRepository.isOnline(), folder), fileFolderCallback)
    }

    override fun createViewHolder(v: View, viewType: Int) = FileViewHolder(v)

    override fun itemLayoutResId(viewType: Int) = FileViewHolder.HOLDER_RES_ID

    override val isPaginated get() = true

    override fun loadFirstPage() {
        apiCall?.cancel()
        apiCall = tryWeave {
            if (folder == null) {
                setNextUrl(null)
                throw IllegalArgumentException("Folder is null")
            }
            // Check if the folder is marked as stale (i.e. items were added/changed/removed)
            val isStale = StudentPrefs.staleFolderIds.contains(folder.id)

            // Force network for pull-to-refresh and stale folders
            val forceNetwork = isRefresh || isStale

            val items = fileListRepository.getFirstPageItems(folder.id, forceNetwork)
            addAll(items.dataOrThrow)
            if (items is DataResult.Success) {
                setNextUrl(items.linkHeaders.nextUrl)
            }

            // Mark folder as no longer stale
            if (isStale) StudentPrefs.staleFolderIds = StudentPrefs.staleFolderIds - folder.id

            fileFolderCallback.onRefreshFinished()
            onCallbackFinished()
        } catch {
            fileFolderCallback.onRefreshFinished()
            onCallbackFinished()
        }
    }

    override fun loadNextPage(nextURL: String) {
        apiCall = tryWeave {
            if (folder == null) {
                setNextUrl(null)
                throw IllegalArgumentException("Folder is null")
            }
            val nextResult = fileListRepository.getNextPage(nextURL, folder.id, isRefresh)
            addAll(nextResult.dataOrThrow)
            if (nextResult is DataResult.Success) {
                setNextUrl(nextResult.linkHeaders.nextUrl)
            }
        } catch {
            fileFolderCallback.onRefreshFinished()
            onCallbackFinished()
        }
    }

    private fun compareFileFolders(oldItem: FileFolder, newItem: FileFolder): Boolean {
        // File items
        if (oldItem.isFile && newItem.isFile) {
            val sameName = oldItem.displayName == newItem.displayName
            val sameSize = oldItem.size == newItem.size
            return sameName && sameSize
        }

        // Folder item
        if (oldItem.name != null && newItem.name != null) {
            val sameName = oldItem.name == newItem.name
            val sameSize = oldItem.size == newItem.size
            return sameName && sameSize
        }

        // If old and new aren't one of the same object types then contents have changed
        return false
    }

    override fun cancel() {
        apiCall?.cancel()
    }
}

interface FileFolderCallback {
    fun onItemClicked(item: FileFolder)
    fun onOpenItemMenu(item: FileFolder, anchorView: View)
    fun onRefreshFinished()
}
