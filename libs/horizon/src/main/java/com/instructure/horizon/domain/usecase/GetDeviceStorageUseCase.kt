/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.domain.usecase

import android.content.Context
import android.os.StatFs
import com.instructure.pandautils.domain.usecase.BaseUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class DeviceStorageData(
    val totalBytes: Long,
    val availableBytes: Long,
)

class GetDeviceStorageUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) : BaseUseCase<Unit, DeviceStorageData>() {

    suspend operator fun invoke() = invoke(Unit)

    override suspend fun execute(params: Unit): DeviceStorageData {
        val statFs = StatFs(context.filesDir.path)
        return DeviceStorageData(
            totalBytes = statFs.totalBytes,
            availableBytes = statFs.availableBytes,
        )
    }
}