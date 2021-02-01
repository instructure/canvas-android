/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.student.adapter.assignment

import android.content.Context
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.student.interfaces.AdapterToAssignmentsCallback

class AssignmentTypeListRecyclerAdapter(
        context: Context,
        canvasContext: CanvasContext,
        adapterToAssignmentsCallback: AdapterToAssignmentsCallback,
        isTesting: Boolean = false
) : AssignmentRecyclerAdapter(context, canvasContext, adapterToAssignmentsCallback, isTesting) {

    override fun populateData() {
        isAllPagesLoaded = true
    }

}
