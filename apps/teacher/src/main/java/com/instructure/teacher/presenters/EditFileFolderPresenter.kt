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

import com.instructure.canvasapi2.managers.FileFolderManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.teacher.R
import com.instructure.teacher.view.EditFileView
import com.instructure.pandautils.blueprint.FragmentPresenter
import kotlinx.coroutines.Job
import java.util.*

class EditFileFolderPresenter(val currentFileOrFolder: FileFolder, val usageRightsEnabled: Boolean, val licenseList: ArrayList<License>, val courseId: Long) : FragmentPresenter<EditFileView>() {

    var isFile: Boolean = false
        get() = currentFileOrFolder.folderId != 0L

    private var deleteFileFolderJob: Job? = null
    private var updateFileFolderJob: Job? = null
    private var updateUsageRightsJob: Job? = null
    private var getUsageRightsJob: Job? = null
    private var checkEnabledFeatures: Job? = null

    override fun onViewDetached() = Unit
    override fun onDestroyed() {
        deleteFileFolderJob?.cancel()
        updateFileFolderJob?.cancel()
        updateUsageRightsJob?.cancel()
        getUsageRightsJob?.cancel()
        checkEnabledFeatures?.cancel()
    }

    override fun loadData(forceNetwork: Boolean) = Unit
    override fun refresh(forceNetwork: Boolean) = Unit

    fun deleteFileFolder() {
        deleteFileFolderJob = tryWeave {
            val deletedFileFolder =
                    if (isFile) {
                        awaitApi<FileFolder> { FileFolderManager.deleteFile(currentFileOrFolder.id, it) }
                    } else {
                        awaitApi { FileFolderManager.deleteFolder(currentFileOrFolder.id, it) }
                    }

            viewCallback?.folderDeleted(deletedFileFolder)
        } catch {
            viewCallback?.showError(if (isFile) R.string.errorDeletingFile else R.string.errorDeletingFolder)
        }
    }

    fun updateFileFolder(fileFolder: FileFolder, accessStatus: FileAccessStatus, usageJustification: FileUsageRightsJustification?, license: License?, copyrightHolder: String? = null) {
        when (accessStatus) {
            is PublishStatus -> {
                fileFolder.isLocked = false
                fileFolder.isHidden = false
                fileFolder.lockDate = null
                fileFolder.unlockDate = null
            }
            is UnpublishStatus -> {
                fileFolder.isLocked = true
                fileFolder.isHidden = false
                fileFolder.lockDate = null
                fileFolder.unlockDate = null
            }
            is RestrictedStatus -> {
                fileFolder.isLocked = false
                fileFolder.isHidden = true
                fileFolder.lockDate = null
                fileFolder.unlockDate = null
            }
            is RestrictedScheduleStatus -> {
                fileFolder.isLocked = false
                fileFolder.isHidden = false
                fileFolder.lockDate = accessStatus.lockAt.toDate()
                fileFolder.unlockDate = accessStatus.unlockAt.toDate()
            }
        }

        updateFileFolderJob = tryWeave {
            val updatedFileFolder: FileFolder
            val updateFileFolder = UpdateFileFolder(fileFolder.name,
                fileFolder.lockDate.toApiString(),
                fileFolder.unlockDate.toApiString(), fileFolder.isLocked, fileFolder.isHidden)

            // Update file/folder
            if (isFile) {
                var usageRightsUpdated: UsageRights? = null

                usageJustification?.let { justification ->
                    val usageRightsParams: MutableMap<String, Any> = mutableMapOf(Pair("file_ids[]", currentFileOrFolder.id),
                            Pair("usage_rights[use_justification]", justification.apiString))
                    copyrightHolder?.let {
                        // Copyright holder not required by api - only add if one was specified
                        usageRightsParams.put("usage_rights[legal_copyright]", it)
                    }

                    license?.let {
                        // Add license
                        usageRightsParams.put("usage_rights[license]", it.id)
                    }

                    usageRightsUpdated = awaitApi<UsageRights> { FileFolderManager.updateUsageRights(courseId, usageRightsParams, it) }
                }

                // Update usage rights if any were set

                updateFileFolder.onDuplicate = "rename"
                updatedFileFolder = awaitApi { FileFolderManager.updateFile(fileFolder.id, updateFileFolder, it) }
                updatedFileFolder.usageRights = usageRightsUpdated
            } else {
                updatedFileFolder = awaitApi { FileFolderManager.updateFolder(fileFolder.id, updateFileFolder, it) }
            }

            viewCallback?.fileFolderUpdated(updatedFileFolder)

        } catch {
            it.printStackTrace()
            viewCallback?.showError(if (isFile) R.string.errorUpdatingFile else R.string.errorUpdatingFolder)
        }
    }
}
