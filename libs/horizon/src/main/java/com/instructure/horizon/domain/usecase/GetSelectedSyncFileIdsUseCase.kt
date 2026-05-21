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

import com.instructure.horizon.database.dao.HorizonFileSyncPlanDao
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class GetSelectedSyncFileIdsUseCase @Inject constructor(
    private val fileSyncPlanDao: HorizonFileSyncPlanDao,
) : BaseUseCase<Unit, Set<Long>>() {

    suspend operator fun invoke() = invoke(Unit)

    override suspend fun execute(params: Unit): Set<Long> =
        fileSyncPlanDao.findAllOnce().map { it.fileId }.toSet()
}
