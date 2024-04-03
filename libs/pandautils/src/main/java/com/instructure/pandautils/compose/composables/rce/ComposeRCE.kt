/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.compose.composables.rce

import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.instructure.pandautils.utils.ThemePrefs
import instructure.rceditor.RCETextEditor
import instructure.rceditor.RCETextEditorView

@Composable
fun ComposeRCE(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val rceTextEditor = RCETextEditor(context)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusRequester = remember {
        FocusRequester()
    }
    LaunchedEffect(Unit) {
        rceTextEditor.html = "<p>Compose RCE</p>"
    }
    Column(modifier = modifier) {
        var rceState by remember { mutableStateOf(RCEState()) }
        RCEControls(rceState) {
            when (it) {
                RCEAction.BOLD -> rceState = rceState.copy(bold = !rceState.bold)
                RCEAction.ITALIC -> rceState = rceState.copy(italic = !rceState.italic)
                RCEAction.UNDERLINE -> rceState = rceState.copy(underline = !rceState.underline)
                RCEAction.NUMBERED_LIST -> rceState = rceState.copy(numberedList = !rceState.numberedList)
                RCEAction.BULLETED_LIST -> rceState = rceState.copy(bulletedList = !rceState.bulletedList)
                RCEAction.COLOR_PICKER -> rceState = rceState.copy(colorPicker = !rceState.colorPicker)
                else -> {
                }
            }
        }

        AndroidView(
            modifier = Modifier
                .height(280.dp)
                .fillMaxWidth(),
            factory = {
                rceTextEditor
            }
        )
    }

}