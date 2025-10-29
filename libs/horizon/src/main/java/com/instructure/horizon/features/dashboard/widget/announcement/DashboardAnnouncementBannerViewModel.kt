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
import com.instructure.horizon.features.dashboard.DashboardEvent
import com.instructure.horizon.features.dashboard.DashboardEventHandler
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardButtonRoute
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardButtonState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardChipState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardState
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardAnnouncementBannerViewModel @Inject constructor(
    private val repository: DashboardAnnouncementBannerRepository,
    private val dashboardEventHandler: DashboardEventHandler,
    @ApplicationContext private val context: Context
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
    }

    private suspend fun loadAnnouncementData(forceNetwork: Boolean = false) {
        _uiState.update { it.copy(state = DashboardItemState.LOADING) }
        val announcements = repository.getUnreadAnnouncements(forceNetwork)

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
        chipState = DashboardPaginatedWidgetCardChipState(
            label = context.getString(R.string.notificationsAnnouncementCategoryLabel),
            color = StatusChipColor.Sky
        ),
        source = source,
        date = date,
        title = title,
        buttonState =
            DashboardPaginatedWidgetCardButtonState(
                label = context.getString(R.string.dashboardAnnouncementBannerGoToAnnouncement),
                color = ButtonColor.WhiteWithOutline,
                route = DashboardPaginatedWidgetCardButtonRoute.MainRoute(route)
            )
    )
}