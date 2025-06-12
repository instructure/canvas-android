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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.ApiPrefs
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
class AccountNotificationsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AccountNotificationsRepository,
    private val apiPrefs: ApiPrefs
): ViewModel() {

    private val _uiState = MutableStateFlow(AccountNotificationsUiState(
        updateNotificationItem = ::updateNotificationItem,
        screenState = LoadingState(
            isPullToRefreshEnabled = false,
            onErrorSnackbarDismiss = ::dismissSnackbar
        )
    ))
    val uiState = _uiState.asStateFlow()

    private val isPermissionGranted: Boolean
        get() {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
    // This is a temporary flag to disable push notifications on beta
    private val isPushEnabledOnBeta: Boolean = false

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

    private fun mapAccountNotificationPreferencesToItems(preferences: List<AccountNotificationPreference>): List<AccountNotificationGroup> {
        val items = mutableListOf<AccountNotificationGroup>()

        val announcementsAndMessages = preferences.filter {
            it.category == AccountNotificationCategory.ANNOUNCEMENT
                    || it.category == AccountNotificationCategory.ACCOUNT_NOTIFICATION
                    || it.category == AccountNotificationCategory.CONVERSATION_MESSAGE
        }
        val dueDates = preferences.filter { it.category == AccountNotificationCategory.DUE_DATE }

        val scores = preferences.filter { it.category == AccountNotificationCategory.GRADING }

        items.add(
            AccountNotificationGroup(
                title = context.getString(R.string.accountNotificationsAnnouncementsAndMessagesHeader),
                description = context.getString(R.string.accountNotificationsAnnouncementsAndMessagesDescription),
                items = listOf(
                    getNotificationItemByType(announcementsAndMessages, AccountNotificationType.EMAIL),
                    getNotificationItemByType(announcementsAndMessages, AccountNotificationType.PUSH),
                )
            )
        )

        items.add(
            AccountNotificationGroup(
                title = context.getString(R.string.accountNotificationsDueDatesHeader),
                description = context.getString(R.string.accountNotificationsDueDatesDescription),
                items = listOf(
                    getNotificationItemByType(dueDates, AccountNotificationType.EMAIL),
                    getNotificationItemByType(dueDates, AccountNotificationType.PUSH),
                )
            )
        )

        items.add(
            AccountNotificationGroup(
                title = context.getString(R.string.accountNotificationsScoresHeader),
                description = context.getString(R.string.accountNotificationsScoresDescription),
                items = listOf(
                    getNotificationItemByType(scores, AccountNotificationType.EMAIL),
                    getNotificationItemByType(scores, AccountNotificationType.PUSH),
                )
            )
        )

        return items
    }

    private fun updateNotificationItem(item: AccountNotificationItem, checked: Boolean) {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(
                    notificationItems = it.notificationItems.map { group ->
                        group.copy(
                            items = group.items.map { notificationItem ->
                                if (notificationItem == item) {
                                    notificationItem.copy(enabled = false, checked = checked)
                                } else {
                                    notificationItem
                                }
                            }
                        )
                    }
                )
            }

            item.onClick(checked)

            _uiState.update {
                it.copy(
                    notificationItems = it.notificationItems.map { groups ->
                        groups.copy(
                            items = groups.items.map { notificationItem ->
                                if (notificationItem.title == item.title) {
                                    notificationItem.copy(enabled = true)
                                } else {
                                    notificationItem
                                }
                            }
                        )
                    }
                )
            }
        } catch {
            _uiState.update {
                it.copy(
                    screenState = it.screenState.copy(
                        errorSnackbar = context.getString(R.string.accountNotificationsFailedToUpdate)
                    )
                )
            }

            _uiState.update {
                it.copy(
                    notificationItems = it.notificationItems.map { groups ->
                        groups.copy(
                            items = groups.items.map { notificationItem ->
                                if (notificationItem.title == item.title) {
                                    notificationItem.copy(enabled = true, checked = !checked)
                                } else {
                                    notificationItem
                                }
                            }
                        )
                    }
                )
            }
        }
    }

    private fun getNotificationItemByType(preferences: List<AccountNotificationPreference>, type: AccountNotificationType): AccountNotificationItem {
        val filteredPreferences = preferences.filter { it.type == type }
        return AccountNotificationItem(
            title = type.label(context),
            checked = filteredPreferences.isNotEmpty() && filteredPreferences.any { it.frequency == AccountNotificationFrequency.IMMEDIATELY },
            enabled = if (type == AccountNotificationType.PUSH) isPermissionGranted && isPushEnabledOnBeta else true,
            onClick = { checked ->
                if (filteredPreferences.isEmpty() && type == AccountNotificationType.PUSH && checked) {
                    repository.registerPushNotification()
                } else {
                    filteredPreferences.forEach { preference ->
                        repository.updateNotificationPreference(
                            preference.category,
                            preference.channelId,
                            checked.frequency()
                        )
                    }
                }
            }
        )
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(screenState = it.screenState.copy(errorSnackbar = null))
        }
    }
}

private fun Boolean.frequency(): AccountNotificationFrequency {
    return if (this) {
        AccountNotificationFrequency.IMMEDIATELY
    } else {
        AccountNotificationFrequency.NEVER
    }
}

private fun AccountNotificationType.label(context: Context): String {
    return when (this) {
        AccountNotificationType.EMAIL -> context.getString(R.string.accountNotificationEmailToggleLabel)
        AccountNotificationType.PUSH -> context.getString(R.string.accountNotificationPushToggleLabel)
    }
}