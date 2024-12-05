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

import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.managers.FileFolderManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.License
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.utils.isNotUser
import com.instructure.pandautils.blueprint.SyncPresenter
import kotlinx.coroutines.Job

class FileSearchPresenter(val canvasContext: CanvasContext) :
    SyncPresenter<FileFolder, FileSearchView>(FileFolder::class.java) {

    private var apiCall: Job? = null

    private var createFolderCall: Job? = null

    private var loadedUsageRights = false

    var usageRights: Boolean = false

    var licenses: ArrayList<License> = ArrayList()

    var searchQuery: String = ""
        set(value) {
            field = value
            if (value.isBlank()) {
                apiCall?.cancel()
                clearData()
                viewCallback?.onRefreshFinished()
                viewCallback?.checkIfEmpty()
            } else {
                loadData(true)
            }
        }

    override fun loadData(forceNetwork: Boolean) {
        apiCall?.cancel()
        apiCall = tryWeave {
            viewCallback?.onRefreshStarted()
            if (!loadedUsageRights && canvasContext.isNotUser) {
                // Determine if this course has the usage rights feature enabled
                val features = awaitApi<List<String>> {
                    FeaturesManager.getEnabledFeaturesForCourse(canvasContext.id, forceNetwork, it)
                }
                usageRights = features.contains("usage_rights_required")
                if (usageRights) {
                    // Grab licenses available
                    licenses = awaitApi { FileFolderManager.getCourseFileLicenses(canvasContext.id, it) }
                }
                loadedUsageRights = true
            }
            val files = awaitApi<List<FileFolder>> {
                FileFolderManager.searchFiles(searchQuery, canvasContext, forceNetwork, it)
            }
            clearData()
            data.addOrUpdate(files)
            viewCallback?.onRefreshFinished()
            viewCallback?.checkIfEmpty()
        } catch {
            Logger.e("Error performing file search: " + it.message)
            viewCallback?.displayError()
        }
    }

    override fun compare(item1: FileFolder, item2: FileFolder): Int {
        val name1: String = if (item1.displayName.isValid()) item1.displayName!! else item1.name.orEmpty()
        val name2: String = if (item2.displayName.isValid()) item2.displayName!! else item2.name.orEmpty()
        return name1.compareTo(name2, true)
    }

    override fun refresh(forceNetwork: Boolean) {
        apiCall?.cancel()
        clearData()
        loadData(forceNetwork)
    }

    override fun onDestroyed() {
        apiCall?.cancel()
        createFolderCall?.cancel()
    }

    companion object {
        const val MIN_QUERY_LENGTH = 3
        const val QUERY_DEBOUNCE = 200L
    }

}
