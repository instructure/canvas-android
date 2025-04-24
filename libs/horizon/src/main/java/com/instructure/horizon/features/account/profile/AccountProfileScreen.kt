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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.instructure.horizon.R
import com.instructure.horizon.features.account.AccountScaffold
import com.instructure.horizon.features.account.AccountViewModel
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.organisms.inputs.textfield.TextField
import com.instructure.horizon.horizonui.organisms.inputs.textfield.TextFieldInputSize
import com.instructure.horizon.horizonui.organisms.inputs.textfield.TextFieldState
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountProfileScreen(
    state: AccountProfileUiState,
    navController: NavController,
    mainNavController: NavController,
) {
    LoadingStateWrapper(state.screenState) {
        AccountProfileContent(state, navController, mainNavController)
    }
}

@Composable
private fun AccountProfileContent(
    state: AccountProfileUiState,
    navController: NavController,
    mainNavController: NavController
) {
    val fullNameErrorMessage = stringResource(R.string.accountFullNameErrorMessage)
    val fullNameState = TextFieldState(
        value = state.fullNameTextValue,
        label = stringResource(R.string.accountFullNameLabel),
        size = TextFieldInputSize.Medium,
        onFocusChanged = { state.updateFullNameIsFocused(it) },
        onValueChange = {
            state.updateFullName(it)
            if (it.text.isEmpty()) {
                state.updateFullNameErrorMessage(fullNameErrorMessage)
            } else {
                state.updateFullNameErrorMessage(null)
            }
        },
        errorText = state.fullNameErrorMessage,
    )
    val displayNameErrorMessage = stringResource(R.string.accountDisplayNameErrorMessage)
    val displayNameState = TextFieldState(
        value = state.displayNameTextValue,
        label = stringResource(R.string.accountDisplayNameLabel),
        size = TextFieldInputSize.Medium,
        onFocusChanged = { state.updateDisplayNameIsFocused(it) },
        onValueChange = {
            state.updateDisplayName(it)
            if (it.text.isEmpty()) {
                state.updateDisplayNameErrorMessage(displayNameErrorMessage)
            } else {
                state.updateDisplayNameErrorMessage(null)
            }
        },
        errorText = state.displayNameErrorMessage,
    )
    val emailErrorMessage = stringResource(R.string.accountEmailErrorMessage)
    val emailState = TextFieldState(
        value = state.emailTextValue,
        label = stringResource(R.string.accountEmailLabel),
        size = TextFieldInputSize.Medium,
        onFocusChanged = { state.updateEmailIsFocused(it) },
        onValueChange = {
            state.updateEmail(it)
            if (it.text.isEmpty()) {
                state.updateEmailErrorMessage(emailErrorMessage)
            } else {
                state.updateEmailErrorMessage(null)
            }
        },
        errorText = state.emailErrorMessage,
        helperText = stringResource(R.string.accountEmailHelperText),
        enabled = false
    )
    AccountScaffold(
        title = stringResource(R.string.accountProfileLabel),
        onBackPressed = { navController.popBackStack() }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                vertical = 48.dp,
                horizontal = 32.dp
            ),
        ) {
            item {
                Column {
                    TextField(fullNameState)

                    HorizonSpace(SpaceSize.SPACE_24)


                    TextField(displayNameState)

                    HorizonSpace(SpaceSize.SPACE_24)

                    TextField(emailState)

                    HorizonSpace(SpaceSize.SPACE_24)

                    Button(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        label = stringResource(R.string.accountProfileSaveChangesLabel),
                        onClick = {
                            state.saveChanges {
                                mainNavController.currentBackStackEntry?.savedStateHandle?.set(AccountViewModel.CHANGE_USER_NAME,
                                    it
                                )
                            }
                        }
                    )
                }
            }

        }
    }
}