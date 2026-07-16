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

package com.instructure.ngc.features.submission.url

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.LocalCourseColor
import com.instructure.instui.compose.input.TextInput
import com.instructure.instui.compose.input.TextInputValidation
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.component.InstUITextInput
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R
import com.instructure.pandautils.compose.composables.ComposeCanvasWebView
import com.instructure.pandautils.compose.composables.ComposeWebViewCallbacks

private const val PREVIEW_HEIGHT_DP = 240

/**
 * The Website URL submission body: a single-line URL entry field plus a live preview of the typed
 * URL. Mirrors [com.instructure.ngc.features.submission.file.FileSubmissionContent] for the file
 * type.
 *
 * [contentResetKey] is bumped by the ViewModel on external resets only (initial load, delete draft,
 * submit success, submission-type switch). The field is re-seeded from [url] on each key change, but
 * NOT on every keystroke, so the caret position is preserved while the user types.
 *
 * [previewUrl] is an https-normalized URL supplied (debounced) by the ViewModel; it is rendered in a
 * passive WebView below the input. It is blank while typing settles, for invalid input, and for
 * cleartext `http://` URLs (which a WebView cannot load).
 */
@Composable
fun UrlSubmissionContent(
    url: String,
    contentResetKey: Int,
    previewUrl: String,
    urlError: String?,
    onUrlChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Seed the field only when contentResetKey changes (external reset), not on every keystroke.
    var currentText by remember(contentResetKey) { mutableStateOf(url) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
    ) {
        TextInput(
            value = currentText,
            onValueChange = {
                currentText = it
                onUrlChanged(it)
            },
            modifier = Modifier.fillMaxWidth(),
            validation = if (urlError != null) TextInputValidation.Error(urlError) else TextInputValidation.None,
            placeholder = stringResource(R.string.enterAUrlHereForYourSubmission),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        )

        if (urlError != null) {
            Text(
                text = urlError,
                style = InstUIText.contentSmall,
                color = InstUISemanticColors.Text.error(),
            )
        }

        // Always reserve the fixed-height preview area so the form layout never shifts when the
        // preview appears or finishes loading.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(PREVIEW_HEIGHT_DP.dp)
                .border(
                    width = InstUITextInput.borderWidth,
                    color = InstUITextInput.borderColor,
                    shape = RoundedCornerShape(InstUITextInput.borderRadius),
                ),
        ) {
            if (previewUrl.isNotBlank()) {
                UrlPreview(
                    previewUrl = previewUrl,
                    backgroundColor = InstUISemanticColors.Background.base().toArgb(),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

/**
 * A passive WebView preview of [previewUrl], built on the shared [ComposeCanvasWebView].
 *
 * Setup mirrors the other in-app WebView previews (external tool / Skillspace / file preview):
 * MATCH_PARENT layout params + `setInitialScale(100)` so the page fills the box. On top of that it
 * is made passive for a preview — touches swallowed, no focus (so loading never scrolls the parent
 * form), and a software layer so a hardware-accelerated WebView does not clear the shared window
 * surface (which would blank the whole Compose UI while loading). A course-colored spinner is shown
 * until the page finishes loading.
 */
@SuppressLint("ClickableViewAccessibility")
@Composable
private fun UrlPreview(
    previewUrl: String,
    backgroundColor: Int,
    modifier: Modifier = Modifier,
) {
    val courseColor = LocalCourseColor.current
    // Backed by a State object (not a delegate) so the spinner overlay can read it via a deferred
    // lambda — that keeps the WebView subtree from recomposing (and reloading) when loading toggles.
    val loading = remember { mutableStateOf(true) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Key the WebView state by the previewed URL so each URL keeps its own state: a live
        // preview whose URL changes (or the submission-type tab being switched away and back) must
        // load the current [previewUrl] rather than restore a previously-shown page.
        ComposeCanvasWebView(
            url = previewUrl,
            modifier = Modifier.fillMaxSize(),
            webViewCallbacks = ComposeWebViewCallbacks(
                onPageStarted = { _, _ -> loading.value = true },
                onPageFinished = { _, _ -> loading.value = false },
                onReceivedError = { _, _, _, _ -> loading.value = false },
            ),
            webViewStateKey = previewUrl,
            applyOnWebView = {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                setInitialScale(100)
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                setBackgroundColor(backgroundColor)
                // JavaScript is left disabled (the default): this is a passive preview of an
                // arbitrary, user-entered URL, so keeping JS off reduces the attack surface.
                setOnTouchListener { _, _ -> true }
                isVerticalScrollBarEnabled = false
                isFocusable = false
                isFocusableInTouchMode = false
            },
        )

        PreviewLoadingIndicator(color = courseColor, loadingProvider = { loading.value })
    }
}

@Composable
private fun PreviewLoadingIndicator(color: Color, loadingProvider: () -> Boolean) {
    if (loadingProvider()) {
        CircularProgressIndicator(color = color)
    }
}

// region Previews
// The preview area renders as the empty reserved box in @Preview (a live WebView cannot load in the
// preview renderer); these focus on the input + error + reserved-box layout.

@Preview(name = "UrlSubmissionContent — empty", showBackground = true)
@Composable
private fun UrlSubmissionContentEmptyPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            UrlSubmissionContent(
                url = "",
                contentResetKey = 0,
                previewUrl = "",
                urlError = null,
                onUrlChanged = {},
            )
        }
    }
}

@Preview(name = "UrlSubmissionContent — with url", showBackground = true)
@Composable
private fun UrlSubmissionContentWithUrlPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            UrlSubmissionContent(
                url = "https://www.instructure.com",
                contentResetKey = 0,
                previewUrl = "",
                urlError = null,
                onUrlChanged = {},
            )
        }
    }
}

@Preview(name = "UrlSubmissionContent — invalid url", showBackground = true)
@Composable
private fun UrlSubmissionContentErrorPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            UrlSubmissionContent(
                url = "not a url",
                contentResetKey = 0,
                previewUrl = "",
                urlError = stringResource(R.string.submissionUrlNotAUrlError),
                onUrlChanged = {},
            )
        }
    }
}

@Preview(
    name = "UrlSubmissionContent — with url (Dark)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun UrlSubmissionContentDarkPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            UrlSubmissionContent(
                url = "https://www.instructure.com",
                contentResetKey = 0,
                previewUrl = "",
                urlError = null,
                onUrlChanged = {},
            )
        }
    }
}

// endregion
