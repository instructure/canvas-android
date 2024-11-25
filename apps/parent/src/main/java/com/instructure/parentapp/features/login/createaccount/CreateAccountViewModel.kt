/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */package com.instructure.parentapp.features.login.createaccount

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandares.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateAccountViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val domain: String = savedStateHandle.get<String>(CreateAccountFragment.DOMAIN) ?: ""
    private val accountId: String =
        savedStateHandle.get<String>(CreateAccountFragment.ACCOUNT_ID) ?: ""
    private val pairingCode: String =
        savedStateHandle.get<String>(CreateAccountFragment.PAIRING_CODE) ?: ""

    private val _uiState = MutableStateFlow(CreateAccountUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<CreateAccountViewModelAction>()
    val events = _events.receiveAsFlow()

    init {
        fetchTermsOfService()
    }

    fun handleAction(action: CreateAccountAction) {
        when (action) {
            CreateAccountAction.CreateAccountTapped -> {
                createAccountTapped()
            }

            is CreateAccountAction.UpdateName -> _uiState.update {
                it.copy(
                    name = action.name,
                    nameError = ""
                )
            }

            is CreateAccountAction.UpdateEmail -> _uiState.update {
                it.copy(
                    email = action.email,
                    emailError = ""
                )
            }

            is CreateAccountAction.UpdatePassword -> _uiState.update {
                it.copy(
                    password = action.password,
                    passwordError = ""
                )
            }

            CreateAccountAction.SnackbarDismissed -> _uiState.update {
                it.copy(showErrorSnack = false)
            }

            CreateAccountAction.SignInTapped -> {
                viewModelScope.launch {
                    _events.send(
                        CreateAccountViewModelAction.NavigateToSignIn(
                            AccountDomain(domain = domain),
                            false
                        )
                    )
                }
            }

            CreateAccountAction.PrivacyTapped -> {
                // TODO needs MBL-17672 implemented
            }

            is CreateAccountAction.TosTapped -> {
                // TODO needs MBL-17672 implemented
            }
        }
    }

    private fun createAccountTapped() {
        if (validate()) {
            createAccount()
        }
    }

    private fun validate(): Boolean {
        var valid = true
        with(uiState.value) {
            if (name.isBlank()) {
                valid = false
                _uiState.update { it.copy(nameError = context.getString(R.string.createAccEnterFullName)) }
            }
            if (email.isBlank()) {
                valid = false
                _uiState.update { it.copy(emailError = context.getString(R.string.createAccEnterEmail)) }
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                valid = false
                _uiState.update { it.copy(emailError = context.getString(R.string.createAccEnterValidEmail)) }
            }
            if (password.isEmpty()) {
                valid = false
                _uiState.update { it.copy(passwordError = context.getString(R.string.createAccEnterPassword)) }
            } else if (password.length < 8) {
                valid = false
                _uiState.update { it.copy(passwordError = context.getString(R.string.createAccPasswordTooShort)) }
            }
        }
        return valid
    }

    private fun createAccount() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(isLoading = true) }
            val response = UserAPI.createObserverUser(
                domain,
                accountId,
                pairingCode,
                uiState.value.name,
                uiState.value.email,
                uiState.value.password
            )

            if (response.isSuccess) {
                _uiState.update { it.copy(isLoading = false) }
                ApiPrefs.domain = domain
                _events.send(
                    CreateAccountViewModelAction.NavigateToSignIn(
                        AccountDomain(domain = domain),
                        true
                    )
                )
            } else if (response.isFail) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        showErrorSnack = true
                    )
                }
            }
        } catch {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    showErrorSnack = true
                )
            }
        }
    }

    private fun fetchTermsOfService() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(isLoading = true) }
            val response = UserAPI.getTermsOfService(
                domain,
                accountId
            )
            if (response.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        termsOfService = response.dataOrNull
                    )
                }

            } else if (response.isFail) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                    )
                }
            }
        } catch {
            _uiState.update {
                it.copy(
                    isLoading = false,
                )
            }
        }
    }
}
