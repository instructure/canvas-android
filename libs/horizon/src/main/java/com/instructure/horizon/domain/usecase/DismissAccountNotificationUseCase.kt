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

import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.horizon.data.repository.AnnouncementRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

class DismissAccountNotificationUseCase @Inject constructor(
    private val repository: AnnouncementRepository,
    private val networkStateProvider: NetworkStateProvider,
) : BaseUseCase<Long, DataResult<AccountNotification>>() {

    override suspend fun execute(params: Long): DataResult<AccountNotification> {
        if (!networkStateProvider.isOnline()) return DataResult.Fail()
        return repository.dismissAccountNotification(params)
    }
}
