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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import instructure.rceditor.RCETextEditor
import jp.wasabeef.richeditor.RichEditor

@Composable
fun ComposeRCE(modifier: Modifier = Modifier) {
    var rceState by remember { mutableStateOf(RCEState()) }

    val context = LocalContext.current
    var rceTextEditor = RCETextEditor(context).apply {
        setOnTextChangeListener { evaluateJavascript("javascript:RE.enabledEditingItems();", null) }
        setOnDecorationChangeListener { text, types ->
            val typeSet = text.split(",").toSet()
            rceState = rceState.copy(
                bold = typeSet.contains(RichEditor.Type.BOLD.name),
                italic = typeSet.contains(RichEditor.Type.ITALIC.name),
                underline = typeSet.contains(RichEditor.Type.UNDERLINE.name),
                numberedList = typeSet.contains(RichEditor.Type.ORDEREDLIST.name),
                bulletedList = typeSet.contains(RichEditor.Type.UNORDEREDLIST.name)
            )
        }
    }
    val postUpdateState = { block: () -> Unit ->
        block()
        rceTextEditor.evaluateJavascript("javascript:RE.enabledEditingItems();", null)
    }
    LaunchedEffect(Unit) {
        rceTextEditor.html = "<p>Compose RCE</p>"
    }
    Column(modifier = modifier) {
        RCEControls(rceState) {
            when (it) {
                RCEAction.BOLD -> {
                    postUpdateState { rceTextEditor.setBold() }
                }

                RCEAction.ITALIC -> {
                    postUpdateState { rceTextEditor.setItalic() }
                }

                RCEAction.UNDERLINE -> {
                    postUpdateState { rceTextEditor.setUnderline() }
                }

                RCEAction.NUMBERED_LIST -> {
                    postUpdateState { rceTextEditor.setNumbers() }
                }

                RCEAction.BULLETED_LIST -> {
                    postUpdateState { rceTextEditor.setBullets() }
                }

                RCEAction.COLOR_PICKER -> {
                    rceState = rceState.copy(colorPicker = !rceState.colorPicker)
                }

                RCEAction.UNDO -> {
                    postUpdateState { rceTextEditor.undo() }
                }

                RCEAction.REDO -> {
                    postUpdateState { rceTextEditor.redo() }
                }

                else -> {
                }
            }
        }

        AndroidView(
            modifier = Modifier
                .fillMaxSize(),
            factory = {
                rceTextEditor
            },
            update = {
                rceTextEditor = it
            }
        )
    }

}