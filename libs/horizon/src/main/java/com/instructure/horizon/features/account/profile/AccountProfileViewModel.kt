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
package com.instructure.horizon.features.account.profile

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
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
class AccountProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiPrefs: ApiPrefs,
    private val repository: AccountProfileRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(AccountProfileUiState(
        screenState = LoadingState(isPullToRefreshEnabled = false, onErrorSnackbarDismiss = ::dismissSnackbar),
        updateFullName = ::updateFullName,
        updateFullNameIsFocused = ::updateFullNameIsFocused,
        updateFullNameErrorMessage = ::updateFullNameErrorMessage,
        updateDisplayName = ::updateDisplayName,
        updateDisplayNameIsFocused = ::updateDisplayNameIsFocused,
        updateDisplayNameErrorMessage = ::updateDisplayNameErrorMessage,
        updateEmail = ::updateEmail,
        updateEmailIsFocused = ::updateEmailIsFocused,
        updateEmailErrorMessage = ::updateEmailErrorMessage,
        saveChanges = ::saveChanges
    ))
    val uiState = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        _uiState.update {
            it.copy(
                screenState = it.screenState.copy(isLoading = true)
            )
        }
        viewModelScope.tryLaunch {
            val user = repository.getUser(apiPrefs.user!!.id)
            _uiState.update {
                it.copy(
                    screenState = it.screenState.copy(isLoading = false),
                    fullNameTextValue = TextFieldValue(user.name),
                    fullNameErrorMessage = if (user.name.isEmpty()) {
                        context.getString(R.string.accountFullNameErrorMessage)
                    } else {
                        null
                    },
                    displayNameTextValue = TextFieldValue(user.shortName ?: ""),
                    displayNameErrorMessage = if (user.shortName.isNullOrEmpty()) {
                        context.getString(R.string.accountDisplayNameErrorMessage)
                    } else {
                        null
                    },
                    emailTextValue = TextFieldValue(user.email ?: ""),
                )
            }
        } catch {
            _uiState.update {
                it.copy(
                    screenState = it.screenState.copy(isLoading = false, isError = true, errorMessage = context.getString(
                        R.string.accountProfileErrorMessage
                    )),
                )
            }
        }
    }

    private fun updateFullName(value: TextFieldValue) {
        _uiState.update {
            it.copy(
                fullNameTextValue = value
            )
        }
    }

    private fun updateFullNameIsFocused(value: Boolean) {
        _uiState.update {
            it.copy(
                fullNameIsFocused = value
            )
        }
    }

    private fun updateFullNameErrorMessage(value: String?) {
        _uiState.update {
            it.copy(
                fullNameErrorMessage = value
            )
        }
    }

    private fun updateDisplayName(value: TextFieldValue) {
        _uiState.update {
            it.copy(
                displayNameTextValue = value
            )
        }
    }

    private fun updateDisplayNameIsFocused(value: Boolean) {
        _uiState.update {
            it.copy(
                displayNameIsFocused = value
            )
        }
    }

    private fun updateDisplayNameErrorMessage(value: String?) {
        _uiState.update {
            it.copy(
                displayNameErrorMessage = value
            )
        }
    }

    private fun updateEmail(value: TextFieldValue) {
        _uiState.update {
            it.copy(
                emailTextValue = value
            )
        }
    }

    private fun updateEmailIsFocused(value: Boolean) {
        _uiState.update {
            it.copy(
                emailIsFocused = value
            )
        }
    }

    private fun updateEmailErrorMessage(value: String?) {
        _uiState.update {
            it.copy(
                emailErrorMessage = value
            )
        }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(screenState = it.screenState.copy(errorSnackbar = null))
        }
    }

    private fun saveChanges() {
        viewModelScope.tryLaunch {
            repository.updateUser(
                uiState.value.fullNameTextValue.text,
                uiState.value.displayNameTextValue.text,
            )
            _uiState.update {
                it.copy(
                    screenState = it.screenState.copy(errorSnackbar = context.getString(R.string.accountProfileUpdated)),
                )
            }
        } catch {
            _uiState.update {
                it.copy(
                    screenState = it.screenState.copy(errorSnackbar = context.getString(R.string.accountProfileFailedToUpdate)),
                )
            }
        }
    }
}