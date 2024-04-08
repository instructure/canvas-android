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
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.webkit.URLUtil
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.utils.MediaUploadUtils
import com.instructure.pandautils.utils.getFragmentActivity
import instructure.rceditor.R
import instructure.rceditor.RCEInsertDialog
import instructure.rceditor.RCETextEditor
import jp.wasabeef.richeditor.RichEditor

@Composable
fun ComposeRCE(
    hint: String,
    html: String,
    onTextChangeListener: (String) -> Unit,
    modifier: Modifier = Modifier,
    canvasContext: CanvasContext? = null
) {
    var imageUri: Uri? by remember { mutableStateOf(null) }
    var rceState by remember { mutableStateOf(RCEState()) }
    var showControls by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var rceTextEditor = RCETextEditor(context).apply {
        setOnTextChangeListener {
            onTextChangeListener(it)
            evaluateJavascript("javascript:RE.enabledEditingItems();", null)
        }
        setOnDecorationChangeListener { text, types ->
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
            it?.let {
                MediaUploadUtils.uploadRceImageJob(
                    it,
                    canvasContext ?: CanvasContext.defaultCanvasContext(),
                    context.getFragmentActivity()
                ) {
                    rceTextEditor.insertImage(it, "")
                }
            }
        }

    val photoLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageUri?.let { imageUri ->
                    MediaUploadUtils.uploadRceImageJob(
                        imageUri,
                        canvasContext ?: CanvasContext.defaultCanvasContext(),
                        context.getFragmentActivity()
                    ) { imageUrl ->
                        MediaUploadUtils.showAltTextDialog(
                            context.getFragmentActivity(),
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
                Color.BLACK,
                Color.BLACK,
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
            context.getFragmentActivity(),
            imagePickerLauncher,
            "image/*",
            photoLauncher,
            imageUri,
        )
    }

    val postUpdateState = { block: () -> Unit ->
        block()
        rceTextEditor.evaluateJavascript("javascript:RE.enabledEditingItems();", null)
    }

    LaunchedEffect(Unit) {
        rceTextEditor.setPlaceholder(hint)
        rceTextEditor.html = html
    }

    Column(modifier = modifier) {
        if (showControls) {
            RCEControls(rceState, onActionClick = {
                when (it) {
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
            }, onColorClick = {
                rceState = rceState.copy(colorPicker = false)
                postUpdateState { rceTextEditor.setTextColor(ContextCompat.getColor(context, it)) }
            })
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