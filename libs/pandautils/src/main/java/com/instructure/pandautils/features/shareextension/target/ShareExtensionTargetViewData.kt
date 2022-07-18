/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.shareextension.target

import android.net.Uri
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.features.file.upload.FileUploadType
import com.instructure.pandautils.features.shareextension.target.itemviewmodels.ShareExtensionAssignmentItemViewModel
import com.instructure.pandautils.features.shareextension.target.itemviewmodels.ShareExtensionCourseItemViewModel

data class ShareExtensionTargetViewData(val courses: List<ShareExtensionCourseItemViewModel>,
                                        @get:Bindable var assignments: List<ShareExtensionAssignmentItemViewModel> = emptyList()) : BaseObservable()

data class ShareExtensionCourseViewData(val title: String,
                                        val color: Int)

data class ShareExtensionAssignmentViewData(val title: String)

data class FileUploadTargetData(
        val course: CanvasContext? = null,
        val assignment: Assignment? = null,
        val fileUploadType: FileUploadType
)

sealed class ShareExtensionTargetAction {
    object AssignmentTargetSelected : ShareExtensionTargetAction()
    object FilesTargetSelected : ShareExtensionTargetAction()
}