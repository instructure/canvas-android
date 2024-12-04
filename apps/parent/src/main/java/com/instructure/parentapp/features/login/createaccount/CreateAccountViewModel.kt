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
import androidx.core.util.PatternsCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParser
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandares.R
import com.instructure.pandautils.utils.getArrayOrNull
import com.instructure.pandautils.utils.getObjectOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import javax.inject.Inject

@HiltViewModel
class CreateAccountViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val repository: CreateAccountRepository
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
                    nameError = null
                )
            }

            is CreateAccountAction.UpdateEmail -> _uiState.update {
                it.copy(
                    email = action.email,
                    emailError = null
                )
            }

            is CreateAccountAction.UpdatePassword -> _uiState.update {
                it.copy(
                    password = action.password,
                    passwordError = null
                )
            }

            CreateAccountAction.SnackbarDismissed -> _uiState.update {
                it.copy(
                    showErrorSnack = false,
                    errorSnackMessage = ""
                )
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
                openPrivacyPolicy()
            }

            is CreateAccountAction.TosTapped -> {
                openTermsOfService()
            }
        }
    }

    private fun openPrivacyPolicy() {
        viewModelScope.launch {
            _events.send(
                CreateAccountViewModelAction.NavigateToPrivacyPolicy
            )
        }
    }

    private fun openTermsOfService() {
        viewModelScope.launch {
            _uiState.value.termsOfService?.content?.let {
                _events.send(
                    CreateAccountViewModelAction.NavigateToHtmlContent(
                        html = it,
                        context.getString(R.string.termsOfUse)
                    )
                )
            } ?: run {
                _events.send(
                    CreateAccountViewModelAction.NavigateToTermsOfService
                )
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
            } else if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
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

            val response = repository.createObserverUser(
                domain,
                accountId,
                pairingCode,
                uiState.value.name,
                uiState.value.email,
                uiState.value.password
            )

            _uiState.update { it.copy(isLoading = false) }
            if (response.isSuccess) {
                ApiPrefs.domain = domain
                _events.send(
                    CreateAccountViewModelAction.NavigateToSignIn(
                        AccountDomain(domain = domain),
                        true
                    )
                )
            } else {
                val responseBody = (response as? DataResult.Fail)?.errorBody
                handleError(responseBody)
            }
        } catch {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    showErrorSnack = true,
                    errorSnackMessage = context.getString(R.string.createAccErrorCreatingAccount)
                )
            }
        }
    }

    private fun handleError(responseBody: ResponseBody?) {
        responseBody?.let {
            val jsonObj = JsonParser.parseString(it.string()).asJsonObject
            val uniqueIdError = jsonObj
                .getObjectOrNull("errors")
                ?.getObjectOrNull("pseudonym")
                ?.getArrayOrNull("unique_id")
                ?.firstOrNull()
                ?.asJsonObject
                ?.get("message")

            uniqueIdError?.let {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        emailError = context.getString(R.string.createAccErrorEmailAlreadyInUse)
                    )
                }
                return
            }

            val pairingError = jsonObj
                .getObjectOrNull("errors")
                ?.getObjectOrNull("pairing_code")
                ?.getArrayOrNull("code")
                ?.firstOrNull()
                ?.asJsonObject
                ?.get("message")

            pairingError?.let {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        showErrorSnack = true,
                        errorSnackMessage = context.getString(R.string.createAccErrorPairingCode)
                    )
                }
                return
            }

            val emailError = jsonObj
                .getObjectOrNull("errors")
                ?.getObjectOrNull("user")
                ?.getArrayOrNull("pseudonyms")
                ?.firstOrNull()
                ?.asJsonObject
                ?.get("message")

            emailError?.let {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        emailError = context.getString(R.string.createAccEnterValidEmail)
                    )
                }
                return
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    showErrorSnack = true,
                    errorSnackMessage = context.getString(R.string.createAccErrorCreatingAccount)
                )
            }
        } ?: run {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    showErrorSnack = true,
                    errorSnackMessage = context.getString(R.string.createAccErrorCreatingAccount)
                )
            }
        }
    }

    private fun fetchTermsOfService() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(isLoading = true) }
            val response = repository.getTermsOfService(
                domain,
                accountId
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    termsOfService = response
                )
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
