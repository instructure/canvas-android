/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.features.settings.inboxsignature

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.LabelSwitchRow
import com.instructure.pandautils.compose.composables.TextFieldWithHeader
import com.instructure.pandautils.utils.ThemePrefs

@Composable
fun InboxSignatureScreen(uiState: InboxSignatureUiState, actionHandler: (InboxSignatureAction) -> Unit, navigationActionClick: () -> Unit) {
    CanvasTheme {
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                CanvasThemedAppBar(
                    title = stringResource(id = R.string.inboxSignatureScreenTitle),
                    navigationActionClick = {
                        navigationActionClick()
                    },
                    actions = {
                        if (uiState.saving) {
                            CircularProgressIndicator(
                                color = Color(ThemePrefs.primaryTextColor),
                                strokeWidth = 3.dp,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("savingProgressIndicator")
                            )
                        } else {
                            SaveAction(
                                uiState = uiState,
                                actionHandler = actionHandler
                            )
                        }
                    }
                )
            }) { padding ->
            InboxSignatureContent(uiState, actionHandler, Modifier.padding(padding))
        }
    }
}

@Composable
fun InboxSignatureContent(uiState: InboxSignatureUiState, actionHandler: (InboxSignatureAction) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = stringResource(id = R.string.inboxSignatureScreenSubtitle),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            fontSize = 14.sp,
            color = colorResource(R.color.textDarkest),
            fontWeight = FontWeight.SemiBold
        )
        LabelSwitchRow(
            label = stringResource(com.instructure.pandares.R.string.inboxSignatureSwitchLabel),
            checked = uiState.signatureEnabled,
            onCheckedChange = {
                actionHandler(InboxSignatureAction.UpdateSignatureEnabled(it))
            },
            fontWeight = FontWeight.SemiBold
        )
        TextFieldWithHeader(label = stringResource(R.string.inboxSignatureTextfieldHeader), value = uiState.signatureText, onValueChange = {
            actionHandler(InboxSignatureAction.UpdateSignature(it))
        }, enabled = uiState.signatureEnabled, headerEnabled = true)
    }
}

@Composable
private fun SaveAction(
    uiState: InboxSignatureUiState,
    actionHandler: (InboxSignatureAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val saveEnabled = uiState.saveEnabled
    val focusManager = LocalFocusManager.current
    TextButton(
        onClick = {
            focusManager.clearFocus()
            actionHandler(InboxSignatureAction.Save)
        },
        enabled = saveEnabled,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.save),
            color = Color(ThemePrefs.primaryTextColor),
            fontSize = 14.sp,
            modifier = Modifier.alpha(if (saveEnabled) 1f else .4f)
        )
    }
}
