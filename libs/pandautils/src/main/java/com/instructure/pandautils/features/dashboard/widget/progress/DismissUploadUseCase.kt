/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.pandautils.features.dashboard.widget.progress

import androidx.room.withTransaction
import com.instructure.pandautils.features.file.upload.FileUploadUtilsHelper
import com.instructure.pandautils.room.appdatabase.AppDatabase
import com.instructure.pandautils.room.appdatabase.daos.DashboardFileUploadDao
import com.instructure.pandautils.room.appdatabase.daos.FileUploadInputDao
import java.util.UUID
import javax.inject.Inject

class DismissUploadUseCase @Inject constructor(
    private val appDatabase: AppDatabase,
    private val dashboardFileUploadDao: DashboardFileUploadDao,
    private val fileUploadInputDao: FileUploadInputDao,
    private val fileUploadUtilsHelper: FileUploadUtilsHelper
) {
    suspend operator fun invoke(workerId: UUID) {
        appDatabase.withTransaction {
            dashboardFileUploadDao.findByWorkerId(workerId.toString())?.let {
                dashboardFileUploadDao.delete(it)
            }
            fileUploadInputDao.findByWorkerId(workerId.toString())?.let { input ->
                fileUploadUtilsHelper.deleteCachedFiles(input.filePaths)
                fileUploadInputDao.delete(input)
            }
        }
    }
}