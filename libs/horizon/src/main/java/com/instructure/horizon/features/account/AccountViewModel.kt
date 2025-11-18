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
package com.instructure.horizon.features.account

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.ExperienceSummary
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.account.navigation.AccountRoute
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.pandautils.room.offline.DatabaseProvider
import com.instructure.pandautils.utils.LogoutHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AccountRepository,
    private val logoutHelper: LogoutHelper,
    private val databaseProvider: DatabaseProvider,
    private val alarmScheduler: AlarmScheduler,
    private val apiPrefs: ApiPrefs
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AccountUiState(
            screenState = LoadingState(
                isPullToRefreshEnabled = false,
                onSnackbarDismiss = ::dismissSnackbar,
            ),
            updateUserName = ::updateUserName,
            performLogout = ::performLogout,
            switchExperience = ::switchExperience
        )
    )
    val uiState = _uiState.asStateFlow()

    private var showExperienceSwitcher = false

    init {
        initData()
    }

    private fun initOptions() {
        _uiState.update {
            it.copy(
                accountGroups = buildList {
                    if (showExperienceSwitcher) add(getExperienceGroup())
                    add(getSettingsGroup())
                    add(getSupportGroup())
                    add(getLogOutGroup())
                }
            )
        }
    }

    private fun getExperienceGroup() = AccountGroupState(
        title = context.getString(R.string.accountExperienceHeading),
        items = listOf(
            AccountItemState(
                title = context.getString(R.string.accountSwitchToAcademicLabel),
                type = AccountItemType.SwitchExperience,
            ),
        )
    )

    private fun getSettingsGroup() = AccountGroupState(
        title = context.getString(R.string.accountSettingsHeading),
        items = listOf(
            AccountItemState(
                title = context.getString(R.string.accountProfileLabel),
                type = AccountItemType.Open(AccountRoute.Profile),
            ),
            AccountItemState(
                title = context.getString(R.string.accountPasswordLabel),
                type = AccountItemType.Open(AccountRoute.Password),
                visible = false,
            ),
            AccountItemState(
                title = context.getString(R.string.accountNotificationsLabel),
                type = AccountItemType.Open(AccountRoute.Notifications),
            ),
            AccountItemState(
                title = context.getString(R.string.accountCalendarFeedLabel),
                type = AccountItemType.Open(AccountRoute.CalendarFeed),
            ),
            AccountItemState(
                title = context.getString(R.string.accountAdvancedLabel),
                type = AccountItemType.Open(AccountRoute.Advanced),
            )
        )
    )

    private fun getSupportGroup() = AccountGroupState(
        title = context.getString(R.string.accountSupportHeading),
        items = listOf(
            AccountItemState(
                title = context.getString(R.string.accountReportABug),
                type = AccountItemType.OpenExternal("https://community.canvaslms.com/t5/Canvas-Career/Report-an-issue/td-p/662564")
            )
        )
    )

    private fun getLogOutGroup() = AccountGroupState(
        title = null,
        items = listOf(
            AccountItemState(
                title = context.getString(R.string.accountLogOutLabel),
                type = AccountItemType.LogOut,
            )
        )
    )

    private fun initData(forceRefresh: Boolean = false) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(screenState = it.screenState.copy(isLoading = true)) }
            loadData(forceRefresh)
            initOptions()
            _uiState.update { it.copy(screenState = it.screenState.copy(isLoading = false)) }
        } catch {
            _uiState.update {
                it.copy(
                    screenState = it.screenState.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = context.getString(R.string.failedToLoadAccount)
                    )
                )
            }
        }
    }

    private suspend fun loadData(forceRefresh: Boolean = false) {
        val user = repository.getUserDetails(forceRefresh = forceRefresh)

        val experiences = repository.getExperiences(forceRefresh = forceRefresh)
        showExperienceSwitcher = experiences.contains(ExperienceSummary.ACADEMIC_EXPERIENCE)

        _uiState.update {
            it.copy(
                userName = user.shortName ?: user.name,
            )
        }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(screenState = it.screenState.copy(snackbarMessage = null))
        }
    }

    private fun updateUserName(value: String) {
        _uiState.update {
            it.copy(
                userName = value
            )
        }
    }

    private fun performLogout() {
        logoutHelper.logout(databaseProvider, alarmScheduler)
    }

    private fun switchExperience() {
        apiPrefs.canvasCareerView = false
        _uiState.update {
            it.copy(restartApp = true)
        }
    }

    companion object {
        const val CHANGE_USER_NAME = "changeUserName"
    }
}