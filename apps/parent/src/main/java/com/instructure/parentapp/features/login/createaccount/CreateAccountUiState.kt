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

import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.models.TermsOfService

data class CreateAccountUiState(
    val isLoading: Boolean = false,
    val showErrorSnack: Boolean = false,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val termsOfService: TermsOfService? = null
)

sealed class CreateAccountAction {
    data class UpdateName(val name: String) : CreateAccountAction()
    data class UpdateEmail(val email: String) : CreateAccountAction()
    data class UpdatePassword(val password: String) : CreateAccountAction()
    data object CreateAccountTapped : CreateAccountAction()
    data object SnackbarDismissed : CreateAccountAction()
    data object SignInTapped : CreateAccountAction()
    data object PrivacyTapped : CreateAccountAction()
    data object TosTapped : CreateAccountAction()
}

sealed class CreateAccountViewModelAction {
    data class NavigateToSignIn(val accountDomain: AccountDomain, val closeActivity: Boolean) :
        CreateAccountViewModelAction()
}
