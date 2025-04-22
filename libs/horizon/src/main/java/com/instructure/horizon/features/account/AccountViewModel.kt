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
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.platform.LoadingState
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
): ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState(
        screenState = LoadingState(
            onErrorSnackbarDismiss = ::dismissSnackbar,
        )
    ))
    val uiState = _uiState.asStateFlow()

    init {
        initData()
        initOptions()
    }

    private fun initOptions() {
        _uiState.update {
            it.copy(
                accountGroups = listOf(
                    getSettingsGroup(),
                    getSupportGroup(),
                    getLogOutGroup()
                )
            )
        }
    }

    private fun getSettingsGroup() = AccountGroupState(
        title = context.getString(R.string.accountSettingsHeading),
        items = listOf(
            AccountItemState(
                title = context.getString(R.string.accountProfileLabel),
                type = AccountItemType.Open,
                onClick = { /* TODO: Open settings */ }
            ),
            AccountItemState(
                title = context.getString(R.string.accountPasswordLabel),
                type = AccountItemType.Open,
                onClick = { /* TODO: Log out */ }
            ),
            AccountItemState(
                title = context.getString(R.string.accountNotificationsLabel),
                type = AccountItemType.Open,
                onClick = { /* TODO: Log out */ }
            ),
            AccountItemState(
                title = context.getString(R.string.accountCalendarFeedLabel),
                type = AccountItemType.Open,
                onClick = { /* TODO: Log out */ }
            ),
            AccountItemState(
                title = context.getString(R.string.accountAdvancedLabel),
                type = AccountItemType.Open,
                onClick = { /* TODO: Log out */ }
            )
        )
    )

    private fun getSupportGroup() = AccountGroupState(
        title = context.getString(R.string.accountSupportHeading),
        items = listOf(
            AccountItemState(
                title = context.getString(R.string.accountBetaCommunityLabel),
                type = AccountItemType.OpenInNew,
                onClick = { /* TODO: Open beta community */ }
            ),
            AccountItemState(
                title = context.getString(R.string.accountGiveFeedbackLabel),
                type = AccountItemType.OpenInNew,
                onClick = { /* TODO: Open feedback */ }
            )
        )
    )

    private fun getLogOutGroup() = AccountGroupState(
        title = null,
        items = listOf(
            AccountItemState(
                title = "Log Out",
                type = AccountItemType.LogOut,
                onClick = { /* TODO: Log out */ }
            )
        )
    )

    private fun initData(forceRefresh: Boolean = false) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(screenState = it.screenState.copy(isLoading = true)) }
            loadData(forceRefresh)
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
        val institutionName = repository.getInstitutionName(forceRefresh = forceRefresh)

        _uiState.update {
            it.copy(
                userName = user.name,
                accountName = institutionName,
            )
        }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(screenState = it.screenState.copy(errorSnackbar = null))
        }
    }
}