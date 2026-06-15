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

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.domain.usecase.GetUnreadAnnouncementsUseCase
import com.instructure.horizon.features.announcement.navigation.AnnouncementRoute
import com.instructure.horizon.features.dashboard.DashboardEvent
import com.instructure.horizon.model.HorizonAnnouncement
import com.instructure.horizon.model.HorizonAnnouncementKind
import com.instructure.horizon.features.dashboard.DashboardEventHandler
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardHeaderState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardState
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.offline.HorizonOfflineState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardAnnouncementBannerViewModel @Inject constructor(
    private val getUnreadAnnouncementsUseCase: GetUnreadAnnouncementsUseCase,
    private val dashboardEventHandler: DashboardEventHandler,
    @ApplicationContext private val context: Context,
    offlineState: HorizonOfflineState,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DashboardAnnouncementBannerUiState(
            onRefresh = ::refresh
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.tryLaunch {
            loadAnnouncementData()
        } catch {
            _uiState.update { it.copy(state = DashboardItemState.ERROR) }
        }

        viewModelScope.launch {
            dashboardEventHandler.events.collect { event ->
                when (event) {
                    is DashboardEvent.AnnouncementRefresh -> {
                        refresh()
                    }
                    else -> { /* No-op */ }
                }
            }
        }

        offlineState.isOffline
            .drop(1)
            .onEach { refresh() }
            .launchIn(viewModelScope)
    }

    private suspend fun loadAnnouncementData(forceNetwork: Boolean = false) {
        _uiState.update { it.copy(state = DashboardItemState.LOADING) }
        val announcements = getUnreadAnnouncementsUseCase(GetUnreadAnnouncementsUseCase.Params(forceNetwork))
            .map { it.toBannerItem() }

        _uiState.update {
            it.copy(
                state = DashboardItemState.SUCCESS,
                cardState = DashboardPaginatedWidgetCardState(
                    items = announcements.map { announcement ->
                        announcement.toPaginatedWidgetCardItemState(context)
                    }
                )
            )
        }
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

    private fun refresh(onComplete: () -> Unit = {}) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(state = DashboardItemState.LOADING) }
            loadAnnouncementData(forceNetwork = true)
            _uiState.update { it.copy(state = DashboardItemState.SUCCESS) }
            onComplete()
        } catch {
            _uiState.update { it.copy(state = DashboardItemState.ERROR) }
            onComplete()
        }
    }
}

private fun AnnouncementBannerItem.toPaginatedWidgetCardItemState(context: Context): DashboardPaginatedWidgetCardItemState {
    return DashboardPaginatedWidgetCardItemState(
        headerState = DashboardPaginatedWidgetCardHeaderState(
            label = context.getString(R.string.notificationsAnnouncementCategoryLabel),
            color = HorizonColors.PrimitivesSky.sky12,
            iconRes = R.drawable.campaign
        ),
        source = source,
        date = date,
        title = title,
        route = route
    )
}