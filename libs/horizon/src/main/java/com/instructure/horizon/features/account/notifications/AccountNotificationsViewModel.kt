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
        screenState = LoadingState(
            isPullToRefreshEnabled = false,
            onSnackbarDismiss = ::dismissSnackbar
        )
    ))
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(screenState = it.screenState.copy(isLoading = true)) }
            val settings = repository.getNotificationPreferences(apiPrefs.user!!.id)
            val horizonSettings = getHorizonSettings(settings)
            val uiGroups = getUiGroupStates(horizonSettings)

            _uiState.update { it.copy(notificationItems = uiGroups) }
            _uiState.update { it.copy(screenState = it.screenState.copy(isLoading = false)) }
        } catch {
            _uiState.update { it.copy(screenState = it.screenState.copy(isLoading = false, isError = true, errorMessage = context.getString(
                R.string.accountNoticifationsFailedToLoadMessage
            ))) }
        }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(screenState = it.screenState.copy(snackbarMessage = null))
        }
    }

    private fun updatePreference(groupTitle: String, typeLabel: String, checked: Boolean? = null, enabled: Boolean? = null) {
        _uiState.update {
            it.copy(
                notificationItems = it.notificationItems.map { group ->
                    if (group.title == groupTitle) {
                        group.copy(
                            items = group.items.map { item ->
                                if (item.label == typeLabel) {
                                    item.copy(checked = checked ?: item.checked, enabled = enabled ?: item.enabled)
                                } else {
                                    item
                                }
                            }
                        )
                    } else {
                        group
                    }
                }
            )
        }
    }

    private fun getUiGroupStates(preferences: List<HorizonAccountNotificationPreference>): List<AccountNotificationGroup> {
        return preferences.distinctBy { it.category }.map { preference ->
            val groupTitle = preference.category.title()
            AccountNotificationGroup(
                title = groupTitle,
                description = preference.category.description(),
                items = preferences.filter { it.category == preference.category }.map {
                    val typeLabel = it.channel.type.label()
                    AccountNotificationTypeState(
                        label = typeLabel,
                        enabled = it.channel.type == AccountNotificationType.EMAIL,
                        checked = it.frequency == AccountNotificationFrequency.IMMEDIATELY,
                        onClick = { checked ->
                            viewModelScope.tryLaunch {
                                updatePreference(groupTitle, typeLabel, enabled = false)
                                preference.category.categories.forEach { category ->
                                    repository.updateNotificationPreference(
                                        category = category,
                                        channelId = it.channel.id,
                                        frequency = if (checked) {
                                            AccountNotificationFrequency.IMMEDIATELY
                                        } else {
                                            AccountNotificationFrequency.NEVER
                                        }
                                    )
                                }
                                updatePreference(groupTitle, typeLabel, enabled = true, checked = checked)
                            } catch {
                                updatePreference(groupTitle, typeLabel, enabled = true)
                                _uiState.update { it.copy(screenState = it.screenState.copy(snackbarMessage = context.getString(R.string.accountNotificationsFailedToUpdate))) }
                            }
                        }
                    )
                }
            )
        }
    }

    private fun getHorizonSettings(preferences: List<AccountNotificationPreference>): List<HorizonAccountNotificationPreference> {
        val mappedSettings = preferences.map { preference ->
            HorizonAccountNotificationPreference(
                channel = preference.channel,
                category = HorizonAccountNotificationCategory.entries.first {
                    it.categories.contains(
                        preference.category
                    )
                },
                frequency = preference.frequency,
            )
        }
        return buildList {
            HorizonAccountNotificationCategory.entries.forEach { category ->
                val categorySettings = mappedSettings.filter { it.category == category }
                val categoryChannels = categorySettings.map { it.channel }.distinct()
                val items = categoryChannels.map { channel ->
                    val prefs = categorySettings.filter { it.channel == channel }
                    HorizonAccountNotificationPreference(
                        channel = channel,
                        category = category,
                        frequency = if (prefs.all { it.frequency == AccountNotificationFrequency.IMMEDIATELY }) {
                            AccountNotificationFrequency.IMMEDIATELY
                        } else {
                            AccountNotificationFrequency.NEVER
                        }
                    )
                }
                addAll(items)
            }
        }
    }

    private fun AccountNotificationType.label(): String {
        return when (this) {
            AccountNotificationType.EMAIL -> context.getString(R.string.accountNotificationEmailToggleLabel)
            AccountNotificationType.PUSH -> context.getString(R.string.accountNotificationPushToggleLabel)
            AccountNotificationType.SMS -> context.getString(R.string.accountNotificationPushToggleLabel)
        }
    }

    private fun HorizonAccountNotificationCategory.title(): String {
        return when (this) {
            HorizonAccountNotificationCategory.ANNOUNCEMENTS_AND_MESSAGES -> context.getString(R.string.accountNotificationsAnnouncementsAndMessagesHeader)
            HorizonAccountNotificationCategory.ASSIGNMENT_DUE_DATE -> context.getString(R.string.accountNotificationsDueDatesHeader)
            HorizonAccountNotificationCategory.SCORES -> context.getString(R.string.accountNotificationsScoresHeader)
        }
    }

    private fun HorizonAccountNotificationCategory.description(): String {
        return when (this) {
            HorizonAccountNotificationCategory.ANNOUNCEMENTS_AND_MESSAGES -> context.getString(R.string.accountNotificationsAnnouncementsAndMessagesDescription)
            HorizonAccountNotificationCategory.ASSIGNMENT_DUE_DATE -> context.getString(R.string.accountNotificationsDueDatesDescription)
            HorizonAccountNotificationCategory.SCORES -> context.getString(R.string.accountNotificationsScoresDescription)
        }
    }
}

