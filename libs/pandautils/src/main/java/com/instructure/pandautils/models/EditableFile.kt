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
package com.instructure.pandautils.models

import android.os.Parcelable
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.License
import kotlinx.parcelize.Parcelize

@Parcelize
data class EditableFile(
        var file : FileFolder,
        var usageRights : Boolean = false,
        var licenses : List<License> = arrayListOf(),
        var courseColor : Int = 0,
        var canvasContext : CanvasContext? = null,
        var iconRes : Int = 0
) : Parcelable
