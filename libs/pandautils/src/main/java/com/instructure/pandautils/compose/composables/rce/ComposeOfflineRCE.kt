/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.utils.MediaUploadUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getFragmentActivity
import kotlinx.coroutines.launch

@Composable
fun ComposeOfflineRCE(
    html: String,
    onStageImage: suspend (uri: Uri) -> String?,
    modifier: Modifier = Modifier,
    hint: String = "",
    fixedHeightInDp: Int? = null,
    canvasContext: CanvasContext = CanvasContext.defaultCanvasContext(),
    onTextChangeListener: (String) -> Unit,
    onRceFocused: () -> Unit = {},
    onCursorYCoordinateChanged: ((Float) -> Unit)? = null,
    rceControlsPosition: RceControlsPosition = RceControlsPosition.TOP,
    rceDialogThemeColor: Int = ThemePrefs.brandColor,
    rceDialogButtonColor: Int = ThemePrefs.textButtonColor,
    fileUploadRestParams: RestParams = RestParams(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    ComposeRCE(
        html = html,
        modifier = modifier,
        hint = hint,
        fixedHeightInDp = fixedHeightInDp,
        canvasContext = canvasContext,
        onTextChangeListener = onTextChangeListener,
        onRceFocused = onRceFocused,
        onCursorYCoordinateChanged = onCursorYCoordinateChanged,
        rceControlsPosition = rceControlsPosition,
        rceDialogThemeColor = rceDialogThemeColor,
        rceDialogButtonColor = rceDialogButtonColor,
        fileUploadRestParams = fileUploadRestParams,
        enableLocalImagePreview = true,
        resolveImageUrl = { uri, onResolved ->
            coroutineScope.launch {
                val localSrc = onStageImage(uri)
                if (localSrc != null) {
                    onResolved(localSrc)
                } else {
                    MediaUploadUtils.uploadRceImageJob(
                        uri,
                        canvasContext,
                        context.getFragmentActivity(),
                        rceDialogButtonColor,
                        fileUploadRestParams
                    ) { imageUrl -> onResolved(imageUrl) }
                }
            }
        }
    )
}
