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

package com.instructure.pandautils.features.file.upload.preferences

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.instructure.canvasapi2.utils.PrefManager
import com.instructure.canvasapi2.utils.StringSetPref
import java.util.*

object FileUploadPreferences : PrefManager("fileUploadPrefs") {

    private var runningWorkerIds by StringSetPref()
    private var runningWorkersLiveData: MutableLiveData<List<UUID>>? = null

    fun addWorkerId(id: UUID) {
        runningWorkerIds = runningWorkerIds + id.toString()
        runningWorkersLiveData?.postValue(getRunningWorkerIds())
    }

    fun removeWorkerId(id: UUID) {
        val idString = id.toString()
        if (runningWorkerIds.contains(idString)) {
            runningWorkerIds = runningWorkerIds - id.toString()
            runningWorkersLiveData?.postValue(getRunningWorkerIds())
        }
    }

    fun getRunningWorkerIds(): List<UUID> {
        return runningWorkerIds.map { UUID.fromString(it) }
    }

    fun getRunningWorkersLiveData(): LiveData<List<UUID>> {
        if (runningWorkersLiveData == null) runningWorkersLiveData = MutableLiveData(getRunningWorkerIds())
        return runningWorkersLiveData!!
    }
}