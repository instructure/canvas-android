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

import com.instructure.horizon.data.repository.NotificationPreferencesRepository
import com.instructure.horizon.model.AccountNotificationPreference
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class GetNotificationPreferencesUseCase @Inject constructor(
    private val repository: NotificationPreferencesRepository,
) : BaseUseCase<Long, List<AccountNotificationPreference>>() {

    override suspend fun execute(params: Long): List<AccountNotificationPreference> {
        return repository.getNotificationPreferences(params)
    }
}
