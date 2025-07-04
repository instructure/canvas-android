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

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import android.webkit.JavascriptInterface
import android.webkit.URLUtil
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.compose.modifiers.conditional
import com.instructure.pandautils.utils.MediaUploadUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getFragmentActivity
import com.instructure.pandautils.utils.toPx
import instructure.rceditor.R
import instructure.rceditor.RCEInsertDialog
import instructure.rceditor.RCETextEditor
import jp.wasabeef.richeditor.RichEditor

enum class RceControlsPosition {
    TOP,
    BOTTOM
}

@Composable
fun ComposeRCE(
    html: String,
    modifier: Modifier = Modifier,
    hint: String = "",
    fixedHeightInDp: Int? = null,
    canvasContext: CanvasContext = CanvasContext.defaultCanvasContext(),
    onTextChangeListener: (String) -> Unit,
    onRceFocused: () -> Unit = {},
    onCursorYCoordinateChanged: (Float) -> Unit = {},
    rceControlsPosition: RceControlsPosition = RceControlsPosition.TOP,
    rceDialogThemeColor: Int = ThemePrefs.brandColor,
    rceDialogButtonColor: Int = ThemePrefs.textButtonColor,
    fileUploadRestParams: RestParams = RestParams(),
) {
    var imageUri: Uri? by remember { mutableStateOf(null) }
    var rceState by remember { mutableStateOf(RCEState()) }
    var showControls by remember { mutableStateOf(false) }

    var focused by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var rceTextEditor = RCETextEditor(context).apply {
        if (fixedHeightInDp != null) {
            setEditorHeight(fixedHeightInDp.dp.value.toInt().toPx)
        } else {
            disallowInterceptTouchEvents = false
        }
        isNestedScrollingEnabled = true
        setOnTextChangeListener {
            onTextChangeListener(it)
            evaluateJavascript("javascript:RE.enabledEditingItems();", null)
        }
        addJavascriptInterface(
            RCECursorPositionInterface { y ->
                onCursorYCoordinateChanged(y)
            }, RCECursorPositionInterface.NAME
        )
        setOnInitialLoadListener {
            evaluateJavascript("""
            document.addEventListener("selectionchange", () => {
                    const selection = window.getSelection();
                if (!selection || selection.rangeCount === 0) {
                    return null;
                }

                const range = selection.getRangeAt(0);
                let y = null;

                // Try getting rects directly first
                const rects = range.getClientRects();
                if (rects.length > 0) {
                    y = rects[0].y;
                } else {
                    const container = range.endContainer;
                    const offset = range.endOffset;
                    const editorDiv = document.getElementById('editor'); // Your contenteditable div
            
                    // Create a temporary span to get the position
                    const tempSpan = document.createElement('span');
                    tempSpan.textContent = '\u200b'; // Zero-width space character
                    tempSpan.style.whiteSpace = 'pre'; // Ensure space is preserved
                    tempSpan.style.lineHeight = '0'; // Don't affect line height by this span
                    tempSpan.style.fontSize = '0'; // Make it invisible visually
            
                    // Insert the temporary span at the caret position
                    range.insertNode(tempSpan);
            
                    // Get its position
                    const spanRect = tempSpan.getBoundingClientRect();
                    y = spanRect.y;
            
                    // Clean up: remove the temporary span and restore the selection
                    tempSpan.parentNode.removeChild(tempSpan);
            
                    // Restore the original selection
                    selection.removeAllRanges();
                    selection.addRange(range);
                }
                ${RCECursorPositionInterface.NAME}.onCursorPositionChanged(y);
            })
        """.trimIndent(), null)
        }
        setOnDecorationChangeListener { text, _ ->
            if (!focused) {
                focusEditor()
                focused = true
                onRceFocused()
            }
            showControls = true
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

    val imagePickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
            it?.let { imageUri ->
                MediaUploadUtils.uploadRceImageJob(
                    imageUri,
                    canvasContext,
                    context.getFragmentActivity(),
                    rceDialogButtonColor,
                    fileUploadRestParams
                ) { imageUrl ->
                    MediaUploadUtils.showAltTextDialog(
                        context.getFragmentActivity(),
                        buttonColor = rceDialogButtonColor,
                        onPositiveClick = { altText ->
                            rceTextEditor.insertImage(imageUrl, altText)
                        },
                        onNegativeClick = {
                            rceTextEditor.insertImage(imageUrl, "")
                        })
                }
            }
        }

    val photoLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageUri?.let { imageUri ->
                    MediaUploadUtils.uploadRceImageJob(
                        imageUri,
                        canvasContext,
                        context.getFragmentActivity(),
                        rceDialogButtonColor,
                        fileUploadRestParams
                    ) { imageUrl ->
                        MediaUploadUtils.showAltTextDialog(
                            context.getFragmentActivity(),
                            buttonColor = rceDialogButtonColor,
                            onPositiveClick = { altText ->
                                rceTextEditor.insertImage(imageUrl, altText)
                            },
                            onNegativeClick = {
                                rceTextEditor.insertImage(imageUrl, "")
                            })
                    }
                }
            }
        }

    val insertLink = {
        rceTextEditor.getSelectedText {
            RCEInsertDialog.newInstance(
                context.getString(R.string.rce_insertLink),
                rceDialogThemeColor,
                rceDialogButtonColor,
                true,
                it
            )
                .setListener { url, alt ->
                    if (URLUtil.isValidUrl(url)) {
                        rceTextEditor.insertLink(url, alt)
                    } else {
                        rceTextEditor.insertLink("https://$url", alt)
                    }
                }
                .show(context.getFragmentActivity().supportFragmentManager, "RCEInsertDialog")
        }
    }

    val insertPhoto = {
        val fileName = "rce_${System.currentTimeMillis()}.jpg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, fileName)

        imageUri =
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        MediaUploadUtils.showPickImageDialog(
            activity = context.getFragmentActivity(),
            onNewPhotoClick = {
                imageUri?.let {
                    photoLauncher.launch(it)
                }
            },
            onChooseFromGalleryClick = {
                imagePickerLauncher.launch("image/*")
            }
        )
    }

    val postUpdateState = { block: () -> Unit ->
        block()
        rceTextEditor.evaluateJavascript("javascript:RE.enabledEditingItems();", null)
    }

    LaunchedEffect(Unit) {
        rceTextEditor.setPlaceholder(hint)
    }

    LaunchedEffect(html) {
        if (html != rceTextEditor.getHtml()) {
            rceTextEditor.applyHtml(html)
        }
    }

    Column(modifier = modifier) {
        val onActionClick = { action: RCEAction ->
            when (action) {
                RCEAction.BOLD -> postUpdateState { rceTextEditor.setBold() }
                RCEAction.ITALIC -> postUpdateState { rceTextEditor.setItalic() }
                RCEAction.UNDERLINE -> postUpdateState { rceTextEditor.setUnderline() }
                RCEAction.NUMBERED_LIST -> postUpdateState { rceTextEditor.setNumbers() }
                RCEAction.BULLETED_LIST -> postUpdateState { rceTextEditor.setBullets() }
                RCEAction.COLOR_PICKER -> rceState =
                    rceState.copy(colorPicker = !rceState.colorPicker)

                RCEAction.UNDO -> postUpdateState { rceTextEditor.undo() }
                RCEAction.REDO -> postUpdateState { rceTextEditor.redo() }
                RCEAction.INSERT_LINK -> insertLink()
                RCEAction.INSERT_IMAGE -> insertPhoto()
            }
        }
        val onColorClick = { color: Int ->
            rceState = rceState.copy(colorPicker = false)
            postUpdateState { rceTextEditor.setTextColor(ContextCompat.getColor(context, color)) }
        }
        if (showControls && rceControlsPosition == RceControlsPosition.TOP) {
            RCEControls(rceState, onActionClick = onActionClick, onColorClick = onColorClick)
            Divider()
        }

        AndroidView(
            modifier = Modifier
                .nestedScroll(rememberNestedScrollInteropConnection())
                .conditional(fixedHeightInDp != null) {
                    height(fixedHeightInDp!!.dp)
                }
                .padding(top = 8.dp),
            factory = {
                rceTextEditor
            },
            update = {
                rceTextEditor = it
            }
        )

        if (showControls && rceControlsPosition == RceControlsPosition.BOTTOM) {
            Spacer(Modifier.size(8.dp))
            Divider()
            RCEControls(rceState, onActionClick = onActionClick, onColorClick = onColorClick)
        }
    }
}

private class RCECursorPositionInterface(
    val onCursorPositionChangeCallback: (Float) -> Unit
) {
    @JavascriptInterface
    fun onCursorPositionChanged(y: Float) {
        this.onCursorPositionChangeCallback(y)
    }
    companion object {
        const val NAME = "RCECursorPositionInterface"
    }
}