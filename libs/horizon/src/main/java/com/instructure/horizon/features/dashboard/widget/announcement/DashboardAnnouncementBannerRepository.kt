/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.dashboard.widget.announcement

import com.instructure.horizon.domain.usecase.GetUnreadAnnouncementsUseCase
import com.instructure.horizon.features.announcement.navigation.AnnouncementRoute
import com.instructure.horizon.model.HorizonAnnouncement
import com.instructure.horizon.model.HorizonAnnouncementKind
import javax.inject.Inject

class DashboardAnnouncementBannerRepository @Inject constructor(
    private val getUnreadAnnouncementsUseCase: GetUnreadAnnouncementsUseCase,
) {
    suspend fun getUnreadAnnouncements(forceRefresh: Boolean): List<AnnouncementBannerItem> {
        return getUnreadAnnouncementsUseCase(GetUnreadAnnouncementsUseCase.Params(forceRefresh))
            .map { it.toBannerItem() }
    }

    private fun HorizonAnnouncement.toBannerItem(): AnnouncementBannerItem {
        return AnnouncementBannerItem(
            title = title,
            source = courseName,
            date = postedAt,
            type = when (kind) {
                HorizonAnnouncementKind.COURSE -> AnnouncementType.COURSE
                HorizonAnnouncementKind.ACCOUNT -> AnnouncementType.GLOBAL
            },
            route = AnnouncementRoute.Details.route(kind, id, courseId),
        )
    }
}
