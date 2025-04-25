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
package com.instructure.horizon.features.account.advanced

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
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class AccountAdvancedViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AccountAdvancedRepository,
    private val apiPrefs: ApiPrefs,
): ViewModel() {
    private val _uiState = MutableStateFlow(
        AccountAdvancedUiState(
            screenState = LoadingState(
                isPullToRefreshEnabled = false,
                onErrorSnackbarDismiss = ::dismissSnackbar
            ),
            updateTimeZone = ::updateTimeZone,
            saveSelectedTimeZone = ::saveSelectedTimeZone,
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(screenState = it.screenState.copy(isLoading = true))
            }

            val user = repository.getUser(apiPrefs.user!!.id
            )
            _uiState.update {
                it.copy(
                    timeZoneOptions = TimeZone.getAvailableIDs().map { TimeZone.getTimeZone(it) },
                    selectedTimeZone = TimeZone.getTimeZone(user.timeZone) ?: TimeZone.getDefault()
                )
            }

            _uiState.update {
                it.copy(screenState = it.screenState.copy(isLoading = false))
            }
        } catch {
            _uiState.update {
                it.copy(screenState = it.screenState.copy(isLoading = false, isError = true, errorSnackbar = context.getString(
                    R.string.accountAdvancedFailedToLoadMessage
                )))
            }
        }
    }

    private fun saveSelectedTimeZone() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(isButtonEnabled = false)
            }

            val selectedTimeZone = _uiState.value.selectedTimeZone
            repository.updateUserTimeZone(selectedTimeZone.id)

            _uiState.update {
                it.copy(isButtonEnabled = true)
            }
        } catch {
            _uiState.update {
                it.copy(
                    screenState = it.screenState.copy(
                        errorSnackbar = context.getString(R.string.accountAdvancedFailedToUpdateMessage)
                    ),
                    isButtonEnabled = true
                )
            }
        }
    }

    private fun updateTimeZone(newTimeZone: TimeZone) {
        _uiState.update {
            it.copy(
                selectedTimeZone = newTimeZone,
            )
        }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(screenState = it.screenState.copy(errorSnackbar = null))
        }
    }
}