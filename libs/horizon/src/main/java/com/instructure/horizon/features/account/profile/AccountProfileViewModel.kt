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
import com.instructure.horizon.horizonui.organisms.inputs.textfield.TextFieldInputSize
import com.instructure.horizon.horizonui.organisms.inputs.textfield.TextFieldState
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
        fullNameInputState = TextFieldState(
            label = context.getString(R.string.accountFullNameLabel),
            size = TextFieldInputSize.Medium,
            value = TextFieldValue(""),
            isFocused = false,
            errorText = null,
            onValueChange = ::updateFullName,
            onFocusChanged = ::updateFullNameIsFocused,
        ),
        displayNameInputState = TextFieldState(
            label = context.getString(R.string.accountDisplayNameLabel),
            size = TextFieldInputSize.Medium,
            value = TextFieldValue(""),
            isFocused = false,
            errorText = null,
            onValueChange = ::updateDisplayName,
            onFocusChanged = ::updateDisplayNameIsFocused,
        ),
        emailInputState = TextFieldState(
            label = context.getString(R.string.accountEmailLabel),
            enabled = false,
            size = TextFieldInputSize.Medium,
            value = TextFieldValue(""),
            isFocused = false,
            errorText = null,
            onValueChange = ::updateEmail,
            onFocusChanged = ::updateEmailIsFocused,
        ),
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
                    fullNameInputState = it.fullNameInputState.copy(
                        value = TextFieldValue(user.name),
                        errorText = if (user.name.isEmpty()) {
                            context.getString(R.string.accountFullNameErrorMessage)
                        } else {
                            null
                        }
                    ),
                    displayNameInputState = it.displayNameInputState.copy(
                        value = TextFieldValue(user.shortName.orEmpty()),
                        errorText = if (user.shortName.isNullOrEmpty()) {
                            context.getString(R.string.accountDisplayNameErrorMessage)
                        } else {
                            null
                        }
                    ),
                    emailInputState = it.emailInputState.copy(
                        value = TextFieldValue(user.primaryEmail.orEmpty()),
                        errorText = if (user.primaryEmail.isNullOrEmpty()) {
                            context.getString(R.string.accountEmailErrorMessage)
                        } else {
                            null
                        },
                        helperText = context.getString(R.string.accountEmailHelperText),
                    ),
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
                fullNameInputState = it.fullNameInputState.copy(
                    value = value,
                    errorText = if (value.text.isEmpty()) {
                        context.getString(R.string.accountFullNameErrorMessage)
                    } else {
                        null
                    },
                ),
            )
        }
    }

    private fun updateFullNameIsFocused(value: Boolean) {
        _uiState.update {
            it.copy(
                fullNameInputState = it.fullNameInputState.copy(isFocused = value),
            )
        }
    }

    private fun updateDisplayName(value: TextFieldValue) {
        _uiState.update {
            it.copy(
                displayNameInputState = it.displayNameInputState.copy(
                    value = value,
                    errorText = if (value.text.isEmpty()) {
                        context.getString(R.string.accountDisplayNameErrorMessage)
                    } else {
                        null
                    },
                ),
            )
        }
    }

    private fun updateDisplayNameIsFocused(value: Boolean) {
        _uiState.update {
            it.copy(
                displayNameInputState = it.displayNameInputState.copy(isFocused = value),
            )
        }
    }

    private fun updateEmail(value: TextFieldValue) {
        _uiState.update {
            it.copy(
                emailInputState = it.emailInputState.copy(
                    value = value,
                    errorText = if (value.text.isEmpty()) {
                        context.getString(R.string.accountEmailErrorMessage)
                    } else {
                        null
                    },
                )
            )
        }
    }

    private fun updateEmailIsFocused(value: Boolean) {
        _uiState.update {
            it.copy(
                emailInputState = it.emailInputState.copy(isFocused = value),
            )
        }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(screenState = it.screenState.copy(errorSnackbar = null))
        }
    }

    private fun saveChanges(notifyParent: (String) -> Unit) {
        viewModelScope.tryLaunch {
            repository.updateUser(
                uiState.value.fullNameInputState.value.text,
                uiState.value.displayNameInputState.value.text,
            )
            _uiState.update {
                it.copy(
                    screenState = it.screenState.copy(errorSnackbar = context.getString(R.string.accountProfileUpdated)),
                )
            }
            notifyParent(uiState.value.fullNameInputState.value.text)
        } catch {
            _uiState.update {
                it.copy(
                    screenState = it.screenState.copy(errorSnackbar = context.getString(R.string.accountProfileFailedToUpdate)),
                )
            }
        }
    }
}