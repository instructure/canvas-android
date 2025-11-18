/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 *
 */
package com.instructure.pandautils.domain.usecase.accountnotification

import com.instructure.pandautils.data.repository.accountnotification.AccountNotificationRepository
import com.instructure.pandautils.domain.models.accountnotification.InstitutionalAnnouncement
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

data class LoadInstitutionalAnnouncementsParams(
    val forceRefresh: Boolean = false
)

class LoadInstitutionalAnnouncementsUseCase @Inject constructor(
    private val accountNotificationRepository: AccountNotificationRepository
) : BaseUseCase<LoadInstitutionalAnnouncementsParams, List<InstitutionalAnnouncement>>() {

    override suspend fun execute(params: LoadInstitutionalAnnouncementsParams): List<InstitutionalAnnouncement> {
        val notifications = accountNotificationRepository.getAccountNotifications(
            forceRefresh = params.forceRefresh
        ).dataOrThrow

        return notifications
            .sortedByDescending { it.startDate }
            .take(5)
            .map { notification ->
                InstitutionalAnnouncement(
                    id = notification.id,
                    subject = notification.subject,
                    message = notification.message,
                    institutionName = "",
                    startDate = notification.startDate,
                    icon = notification.icon
                )
            }
    }
}