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
package com.instructure.horizon.features.account.calendarfeed

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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonIconPosition
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.organisms.inputs.singleselectimage.SingleSelectImage
import com.instructure.horizon.horizonui.organisms.inputs.singleselectimage.SingleSelectImageInputSize
import com.instructure.horizon.horizonui.organisms.inputs.singleselectimage.SingleSelectImageState
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.horizon.horizonui.organisms.scaffolds.HorizonScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountCalendarFeedScreen(
    state: AccountCalendarUiState,
    navController: NavController
) {
    HorizonScaffold(
        title = stringResource(R.string.accountCalendarFeedTitle),
        onBackPressed = { navController.popBackStack() },
    ) { modifier ->
        LoadingStateWrapper(state.screenState) {
            AccountCalendarFeedContent(state, modifier)
        }
    }
}

@Composable
private fun AccountCalendarFeedContent(state: AccountCalendarUiState, modifier: Modifier) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var isFocused by remember { mutableStateOf(false) }
    var isOpen by remember { mutableStateOf(false) }
    val singleSelectState = SingleSelectImageState(
        label = stringResource(R.string.accountCalendarFeedSelectLabel),
        size = SingleSelectImageInputSize.Medium,
        placeHolderText = stringResource(R.string.accountCalendarFeedSeelctPlaceholder),
        isFocused = isFocused,
        isMenuOpen = isOpen,
        onFocusChanged = { isFocused = it },
        onMenuOpenChanged = { isOpen = it },
        onOptionSelected = { option ->
            startActivity(context, state.calendarOptions.first { it.icon == option.first && it.name == option.second }.intent, null)
        },
        options = state.calendarOptions.map { Pair( it.icon, it.name) },
        selectedOption = null
    )

    LazyColumn(
        contentPadding = PaddingValues(32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = modifier
    ) {
        item {
            SingleSelectImage(singleSelectState)
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ){
                Button(
                    label = stringResource(R.string.accountCalendarFeedCopyLinkButtonLabel),
                    width = ButtonWidth.FILL,
                    iconPosition = ButtonIconPosition.End(R.drawable.link),
                    onClick = {
                        clipboardManager.setText(AnnotatedString(state.calendarUrl))
                        state.showSnackBar(context.getString(R.string.accountCalendarFeedCopiedLinkMessage))
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}