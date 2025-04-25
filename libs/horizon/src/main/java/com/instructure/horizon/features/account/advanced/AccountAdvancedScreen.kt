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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.instructure.horizon.R
import com.instructure.horizon.features.account.AccountScaffold
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelect
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectInputSize
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectState
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountAdvancedScreen(
    state: AccountAdvancedUiState,
    navController: NavController
) {
    LoadingStateWrapper(state.screenState) {
        AccountAdvancedContent(
            state = state,
            navController = navController
        )
    }
}

@Composable
private fun AccountAdvancedContent(
    state: AccountAdvancedUiState,
    navController: NavController
) {
    AccountScaffold(
        title = stringResource(R.string.accountAdvancedTitle),
        onBackPressed = { navController.popBackStack() },
    ) {
        var isFocused by remember { mutableStateOf(false) }
        var isOpen by remember { mutableStateOf(false) }
        val singleSelectState = SingleSelectState(
            label = stringResource(R.string.accountAdvancedtimeZoneSelectLabel),
            size = SingleSelectInputSize.Medium,
            placeHolderText = stringResource(R.string.accountAdvancedTimeZoneSelectPlaceHolder),
            isFocused = isFocused,
            isMenuOpen = isOpen,
            onFocusChanged = { isFocused = it },
            onMenuOpenChanged = { isOpen = it },
            onOptionSelected = { option ->
                val selectedTimeZone = state.timeZoneOptions.first { it.id == option }
                state.updateTimeZone(selectedTimeZone)
            },
            options = state.timeZoneOptions.map { it.id },
            selectedOption = state.selectedTimeZone.id,
        )
        LazyColumn(
            contentPadding = PaddingValues(vertical = 32.dp, horizontal = 60.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            item {
                SingleSelect(singleSelectState)
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        label = stringResource(R.string.accountAdvancedSaveChangesLabel),
                        enabled = state.isButtonEnabled,
                        onClick = { state.saveSelectedTimeZone() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}