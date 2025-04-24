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
package com.instructure.horizon.features.account.notifications

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AccountNotificationsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AccountNotificationsRepository,
    private val apiPrefs: ApiPrefs
): ViewModel() {

    private val _uiState = MutableStateFlow(AccountNotificationsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(screenState = it.screenState.copy(isLoading = true)) }

            val settings = repository.getNotificationPreferences(apiPrefs.user!!.id)
            val items = mapAccountNotificationPreferencesToItems(settings)
            _uiState.update { it.copy(notificationItems = items) }
            _uiState.update { it.copy(screenState = it.screenState.copy(isLoading = false)) }
        } catch {
            _uiState.update { it.copy(screenState = it.screenState.copy(isLoading = false, isError = true, errorMessage = context.getString(
                R.string.accountNoticifationsFailedToLoadMessage
            ))) }
        }
    }

    private fun mapAccountNotificationPreferencesToItems(preferences: List<AccountNotificationPreference>): List<AccountNotificationItem> {
        val items = mutableListOf<AccountNotificationItem>()

        val announcementsAndMessages = preferences.filter {
            it.category == AccountNotificationCategory.ANNOUNCEMENT
                    || it.category == AccountNotificationCategory.ACCOUNT_NOTIFICATION
                    || it.category == AccountNotificationCategory.CONVERSATION_MESSAGE
        }
        val dueDates = preferences.filter { it.category == AccountNotificationCategory.DUE_DATE }

        val scores = preferences.filter { it.category == AccountNotificationCategory.GRADING }

        items.add(
            AccountNotificationItem(
                title = context.getString(R.string.accountNotificationsAnnouncementsAndMessagesHeader),
                description = context.getString(R.string.accountNotificationsAnnouncementsAndMessagesDescription),
                isEmailEnabled = announcementsAndMessages.all { it.type == AccountNotificationType.EMAIL },
                isPushEnabled = announcementsAndMessages.all { it.type == AccountNotificationType.PUSH }
            )
        )

        items.add(
            AccountNotificationItem(
                title = context.getString(R.string.accountNotificationsDueDatesHeader),
                description = context.getString(R.string.accountNotificationsDueDatesDescription),
                isEmailEnabled = dueDates.all { it.type == AccountNotificationType.EMAIL },
                isPushEnabled = dueDates.all { it.type == AccountNotificationType.PUSH }
            )
        )

        items.add(
            AccountNotificationItem(
                title = context.getString(R.string.accountNotificationsScoresHeader),
                description = context.getString(R.string.accountNotificationsScoresDescription),
                isEmailEnabled = scores.all { it.type == AccountNotificationType.EMAIL },
                isPushEnabled = scores.all { it.type == AccountNotificationType.PUSH }
            )
        )

        return items
    }
}