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
package com.instructure.teacher.presenters

import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.managers.FileFolderManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CreateFolder
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.License
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.awaitApis
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.blueprint.SyncPresenter
import com.instructure.teacher.viewinterface.FileListView
import kotlinx.coroutines.Job

class FileListPresenter(var currentFolder: FileFolder, val mCanvasContext: CanvasContext) : SyncPresenter<FileFolder, FileListView>(FileFolder::class.java) {

    private var apiCalls: Job? = null
    private var createFolderCall: Job? = null

    var usageRights: Boolean = false
    var licenses: ArrayList<License> = ArrayList()

    override fun loadData(forceNetwork: Boolean) {
        apiCalls = tryWeave {
            viewCallback?.onRefreshStarted()
            if(currentFolder.id == -1L) {
                // We are at the root - grab the root folder and everything in it
                val (folder, files, folders) = awaitApis<FileFolder, List<FileFolder>, List<FileFolder>>(
                        { FileFolderManager.getRootFolderForContext(mCanvasContext, forceNetwork, it) },
                        { FileFolderManager.getAllFilesRoot(mCanvasContext, forceNetwork, it) },
                        { FileFolderManager.getAllFoldersRoot(mCanvasContext, forceNetwork, it) })
                currentFolder = folder
                data.addOrUpdate(files)
                data.addOrUpdate(folders)
            } else {
                // Inside a folder - grab its contents
                val (files, folders) = awaitApis<List<FileFolder>, List<FileFolder>>(
                        { FileFolderManager.getAllFiles(currentFolder.id, forceNetwork, it) },
                        { FileFolderManager.getAllFolders(currentFolder.id, forceNetwork, it) })
                data.addOrUpdate(files)
                data.addOrUpdate(folders)
            }

            if (!CanvasContext.Type.isUser(mCanvasContext)) {
                // Determine if this course has the usage rights feature enabled
                val features = awaitApi<List<String>> { FeaturesManager.getEnabledFeaturesForCourse((mCanvasContext as? Group)?.courseId ?: mCanvasContext.id, forceNetwork, it) }
                usageRights = features.contains("usage_rights_required")
            }

            if (usageRights) {
                // Grab licenses available
                licenses = awaitApi { FileFolderManager.getCourseFileLicenses(mCanvasContext.id, it) }
            }

            viewCallback?.onRefreshFinished()
            viewCallback?.checkIfEmpty()
        } catch {
            Logger.e("Error loading file list: " + it.message);
        }
    }

    fun createFolder(folderName: String) {
        createFolderCall = tryWeave {
            val newFolder = awaitApi<FileFolder> { FileFolderManager.createFolder(currentFolder.id, CreateFolder(folderName), it) }
            data.addOrUpdate(newFolder)
            viewCallback?.folderCreationSuccess()
        } catch {
            viewCallback?.folderCreationError()
        }
    }

    override fun compare(item1: FileFolder, item2: FileFolder) = item1.compareTo(item2)

    override fun refresh(forceNetwork: Boolean) {
        apiCalls?.cancel()
        clearData()
        loadData(forceNetwork)
    }

    override fun onDestroyed() {
        apiCalls?.cancel()
        createFolderCall?.cancel()
    }
}
