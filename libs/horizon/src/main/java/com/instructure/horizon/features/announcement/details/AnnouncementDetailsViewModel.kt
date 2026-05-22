/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.features.announcement.details

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.domain.usecase.DismissAccountNotificationUseCase
import com.instructure.horizon.domain.usecase.GetAnnouncementDetailsUseCase
import com.instructure.horizon.domain.usecase.MarkAnnouncementReadUseCase
import com.instructure.horizon.features.announcement.navigation.AnnouncementRoute
import com.instructure.horizon.features.dashboard.DashboardEvent
import com.instructure.horizon.features.dashboard.DashboardEventHandler
import com.instructure.horizon.features.inbox.InboxEvent
import com.instructure.horizon.features.inbox.InboxEventHandler
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.horizon.model.HorizonAnnouncementDetails
import com.instructure.horizon.model.HorizonAnnouncementKind
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AnnouncementDetailsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getAnnouncementDetailsUseCase: GetAnnouncementDetailsUseCase,
    private val markAnnouncementReadUseCase: MarkAnnouncementReadUseCase,
    private val dismissAccountNotificationUseCase: DismissAccountNotificationUseCase,
    private val dashboardEventHandler: DashboardEventHandler,
    private val inboxEventHandler: InboxEventHandler,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val kindString: String? = savedStateHandle[AnnouncementRoute.KIND]
    private val kind = HorizonAnnouncementKind.entries.firstOrNull { it.name.equals(kindString, ignoreCase = true) }
    private val id: Long? = savedStateHandle[AnnouncementRoute.ID]
    private val courseId: Long? = savedStateHandle.get<Long>(AnnouncementRoute.COURSE_ID)?.takeIf { it > 0L }

    private val _uiState = MutableStateFlow(
        AnnouncementDetailsUiState(
            loadingState = LoadingState(
                onRefresh = ::refresh,
                onSnackbarDismiss = ::dismissSnackbar,
            ),
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
            fetchData(forceRefresh = false)
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false)) }
        } catch {
            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(
                        isLoading = false,
                        snackbarMessage = context.getString(R.string.inboxFailedtoLoadErrorMessage),
                    ),
                )
            }
        }
    }

    private fun refresh() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = true)) }
            fetchData(forceRefresh = true)
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = false)) }
        } catch {
            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(
                        isRefreshing = false,
                        snackbarMessage = context.getString(R.string.inboxFailedToRefreshLabel),
                    ),
                )
            }
        }
    }

    private suspend fun fetchData(forceRefresh: Boolean) {
        val kind = kind
        val id = id
        if (kind == null || id == null) {
            setErrorState()
            return
        }
        val details = getAnnouncementDetailsUseCase(
            GetAnnouncementDetailsUseCase.Params(
                kind = kind,
                id = id,
                courseId = courseId,
                forceRefresh = forceRefresh,
            )
        )
        _uiState.update {
            it.copy(
                title = details.announcement.title,
                items = details.toUiItems(),
            )
        }
        applySideEffects(details)
    }

    private fun applySideEffects(details: HorizonAnnouncementDetails) {
        val courseId = courseId
        val id = id ?: return
        when (kind) {
            HorizonAnnouncementKind.COURSE -> {
                if (courseId != null && details.announcement.isUnread) {
                    viewModelScope.tryLaunch {
                        val result = markAnnouncementReadUseCase(
                            MarkAnnouncementReadUseCase.Params(
                                courseId = courseId,
                                announcementId = id,
                                entryIds = details.comments.map { it.id }.toSet(),
                            )
                        )
                        if (result.isSuccess) {
                            inboxEventHandler.postEvent(InboxEvent.AnnouncementRead)
                            dashboardEventHandler.postEvent(DashboardEvent.AnnouncementRefresh)
                        }
                    } catch { /* Mark-as-read is best-effort */ }
                }
            }
            HorizonAnnouncementKind.ACCOUNT -> {
                if (!details.announcement.isClosed) {
                    viewModelScope.tryLaunch {
                        val result = dismissAccountNotificationUseCase(id)
                        if (result.isSuccess) {
                            inboxEventHandler.postEvent(InboxEvent.AnnouncementRead)
                            dashboardEventHandler.postEvent(DashboardEvent.AnnouncementRefresh)
                        }
                    } catch { /* Dismiss is best-effort */ }
                }
            }
            null -> Unit
        }
    }

    private fun setErrorState() {
        _uiState.update {
            it.copy(
                loadingState = it.loadingState.copy(isLoading = false, isError = true),
            )
        }
    }

    private fun dismissSnackbar() {
        _uiState.update { it.copy(loadingState = it.loadingState.copy(snackbarMessage = null)) }
    }

    private fun HorizonAnnouncementDetails.toUiItems(): List<AnnouncementDetailsItem> {
        val header = AnnouncementDetailsItem(
            author = announcement.authorDisplayName
                ?: context.getString(R.string.inboxGlobalAnnouncementAuthorLabel),
            date = announcement.postedAt ?: Date(),
            isHtmlContent = true,
            content = announcement.message,
        )
        val replies = comments.map { comment ->
            AnnouncementDetailsItem(
                author = comment.authorDisplayName.orEmpty(),
                date = comment.createdAt ?: Date(),
                isHtmlContent = true,
                content = comment.message,
            )
        }
        return listOf(header) + replies
    }
}
