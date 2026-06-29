/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.canvas.espresso.mockcanvas.fakes

import com.instructure.canvasapi2.managers.graphql.horizon.HorizonManageOfflineContentManager
import com.instructure.canvasapi2.managers.graphql.horizon.OfflineCourseWithFiles
import com.instructure.canvasapi2.utils.DataResult

class FakeHorizonManageOfflineContentManager : HorizonManageOfflineContentManager {
    override suspend fun getCoursesWithFiles(
        userId: Long,
        forceNetwork: Boolean,
    ): DataResult<List<OfflineCourseWithFiles>> {
        return DataResult.Success(emptyList())
    }
}
