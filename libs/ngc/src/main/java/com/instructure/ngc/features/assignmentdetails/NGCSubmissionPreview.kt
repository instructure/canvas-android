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
package com.instructure.ngc.features.assignmentdetails

import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.instructure.annotations.compose.PdfPreview
import com.instructure.instui.compose.LocalCourseColor
import com.instructure.instui.compose.container.Card
import com.instructure.instui.compose.container.Elevation
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.input.Button
import com.instructure.instui.compose.input.ButtonVariant
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.eyeOff
import com.instructure.instui.token.icon.file
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R
import com.instructure.pandautils.compose.composables.ComposeCanvasWebView
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.filedetails.ImageFileContent
import com.instructure.pandautils.compose.composables.filedetails.MediaFileContent
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.launchCustomTab

/** Fixed height for the inline preview body; longer content (text/URL) is clipped to this. */
private val PREVIEW_HEIGHT = 320.dp

/**
 * Inline preview of the selected submission attempt: a single white card containing the preview
 * body (image / PDF / media / text / URL, or a static "no preview" placeholder), the "Show more"
 * action, and a [footer] slot for the grade + rubric/comments rows that belong in the same card.
 */
@Composable
fun NGCSubmissionPreview(
    state: NgcSubmissionPreviewUiState,
    onShowMore: () -> Unit,
    onFullScreenMedia: (url: String, contentType: String, displayName: String, thumbnailUrl: String) -> Unit,
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit = {},
) {
    // contentPadding = 0 so the preview image and the row separators run edge-to-edge in the card;
    // each inner element supplies its own padding.
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ngcAssignmentDetails_submissionPreview"),
        elevation = Elevation.Level1,
        contentPadding = 0.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            PreviewBody(
                state = state,
                onFullScreenMedia = onFullScreenMedia,
            )
            Box(modifier = Modifier.padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg)) {
                ShowMoreButton(onClick = onShowMore)
            }
            footer()
        }
    }
}

@Composable
private fun PreviewBody(
    state: NgcSubmissionPreviewUiState,
    onFullScreenMedia: (url: String, contentType: String, displayName: String, thumbnailUrl: String) -> Unit,
) {
    val activity = LocalContext.current.getActivityOrNull()
    val fixedHeight = Modifier
        .fillMaxWidth()
        .height(PREVIEW_HEIGHT)
        .clipToBounds()
    when (state) {
        NgcSubmissionPreviewUiState.Loading -> Loading(
            color = LocalCourseColor.current,
            modifier = Modifier
                .fillMaxWidth()
                .height(PREVIEW_HEIGHT)
                .testTag("ngcAssignmentDetails_submissionPreviewLoading"),
        )

        NgcSubmissionPreviewUiState.NoPreview -> NoPreviewPlaceholder()

        // A file upload whose files can't be rendered: the file list itself is the preview, so show
        // file items (icon + name) instead of the "no preview" image.
        is NgcSubmissionPreviewUiState.NoFilePreview -> FileListPreview(fileNames = state.fileNames)

        is NgcSubmissionPreviewUiState.Image -> PreviewWithAdditionalFiles(state.additionalFileCount) { previewModifier ->
            ImageFileContent(
                uri = state.uri,
                contentDescription = state.displayName,
                loadingIndicator = { Loading(color = LocalCourseColor.current, modifier = Modifier.fillMaxSize()) },
                modifier = previewModifier,
            )
        }

        is NgcSubmissionPreviewUiState.Pdf -> PreviewWithAdditionalFiles(state.additionalFileCount) { previewModifier ->
            Box(modifier = previewModifier) {
                PdfPreview(documentUri = state.uri, modifier = Modifier.fillMaxSize())
            }
        }

        is NgcSubmissionPreviewUiState.AnnotatedPdf -> Box(modifier = fixedHeight) {
            PdfPreview(documentUri = state.uri, annotations = state.annotations, modifier = Modifier.fillMaxSize())
        }

        is NgcSubmissionPreviewUiState.Media -> PreviewWithAdditionalFiles(state.additionalFileCount) { previewModifier ->
            // key(uri) so switching attempts recreates the player: MediaFileContent wires its
            // fullscreen click listener in the AndroidView factory (runs once), so without a fresh
            // identity the button keeps opening the previously played file.
            key(state.uri) {
                MediaFileContent(
                    uri = state.uri,
                    contentType = state.contentType,
                    onFullScreenClicked = { uri, contentType ->
                        onFullScreenMedia(uri.toString(), contentType, state.displayName, state.thumbnailUrl)
                    },
                    modifier = previewModifier,
                )
            }
        }

        // Submitted text: render the HTML in place. Links open in a custom tab rather than routing
        // the app (mirrors horizon's file preview).
        is NgcSubmissionPreviewUiState.Text -> PreviewWithAdditionalFiles(state.additionalFileCount) { previewModifier ->
            ComposeCanvasWebViewWrapper(
                content = state.content,
                contentType = state.contentType,
                useInAppFormatting = state.useInAppFormatting,
                baseUrl = state.baseUrl,
                applyOnWebView = {
                    activity?.let { addVideoClient(it) }
                    setZoomSettings(false)
                },
                embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks(
                    shouldLaunchInternalWebViewFragment = { true },
                    launchInternalWebViewFragment = { url -> activity?.launchCustomTab(url, ThemePrefs.brandColor) },
                ),
                modifier = previewModifier,
            )
        }

        // Submitted URL: load the page in place. No routing callbacks — otherwise the initial page
        // load is intercepted and pushed to a separate WebView screen.
        is NgcSubmissionPreviewUiState.Url -> ComposeCanvasWebView(
            url = state.url,
            applyOnWebView = {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                settings.loadWithOverviewMode = true
                settings.displayZoomControls = false
                settings.setSupportZoom(true)
                activity?.let { addVideoClient(it) }
                setInitialScale(100)
            },
            modifier = fixedHeight,
        )
    }
}

@Composable
private fun NoPreviewPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg)
            .testTag("ngcAssignmentDetails_submissionPreviewNoPreview"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
    ) {
        Icon(imageVector = InstUIIcons.eyeOff, color = IconColor.Muted, size = IconSize.Large)
        Text(
            text = stringResource(R.string.ngc_submissionNoPreview),
            style = InstUIText.content,
            color = InstUISemanticColors.Text.muted(),
            textAlign = TextAlign.Center,
        )
    }
}

/** Height of a single file row; used to compute how many rows fit in the preview window. */
private val FILE_ITEM_HEIGHT = 44.dp

/**
 * Renders a single-file [preview] and, when [additionalFileCount] > 0, reserves a row beneath it for
 * the "N additional files" that aren't shown. The preview and the row together fill the preview
 * window; with no additional files the preview takes the whole window.
 */
@Composable
private fun PreviewWithAdditionalFiles(
    additionalFileCount: Int,
    preview: @Composable (previewModifier: Modifier) -> Unit,
) {
    if (additionalFileCount > 0) {
        Column(modifier = Modifier.fillMaxWidth().height(PREVIEW_HEIGHT)) {
            preview(Modifier.fillMaxWidth().weight(1f).clipToBounds())
            AdditionalFilesRow(additionalFileCount)
        }
    } else {
        preview(Modifier.fillMaxWidth().height(PREVIEW_HEIGHT).clipToBounds())
    }
}

/**
 * Preview for a file upload whose first file can't be rendered inline: a list of file items (icon +
 * name). Shows as many rows as fit in the preview window; if there are more, the last row becomes a
 * "N additional files" overflow item (its slot is always reserved when the files don't all fit).
 */
@Composable
private fun FileListPreview(fileNames: List<String>) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = PREVIEW_HEIGHT)
            .padding(vertical = InstUILayoutSizes.Spacing.SpaceSm.spaceSm)
            .testTag("ngcAssignmentDetails_submissionPreviewFiles"),
    ) {
        val maxRows = (maxHeight.value / FILE_ITEM_HEIGHT.value).toInt().coerceAtLeast(1)
        val overflow = fileNames.size > maxRows
        val visibleCount = if (overflow) maxRows - 1 else fileNames.size
        Column(modifier = Modifier.fillMaxWidth()) {
            fileNames.take(visibleCount).forEach { fileName ->
                FilePreviewItem(fileName)
            }
            if (overflow) {
                AdditionalFilesRow(count = fileNames.size - visibleCount)
            }
        }
    }
}

@Composable
private fun FilePreviewItem(fileName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(FILE_ITEM_HEIGHT)
            .padding(horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
    ) {
        Icon(imageVector = InstUIIcons.file, color = IconColor.Muted, size = IconSize.Small)
        Text(
            text = fileName,
            style = InstUIText.content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AdditionalFilesRow(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(FILE_ITEM_HEIGHT)
            .padding(horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg)
            .testTag("ngcAssignmentDetails_submissionPreviewMoreFiles"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = pluralStringResource(R.plurals.ngc_additionalFiles, count, count),
            style = InstUIText.contentImportant,
            color = InstUISemanticColors.Text.muted(),
        )
    }
}

/** "Show more" opens the full submission. Per design it's the InstUI Secondary button (tonal). */
@Composable
private fun ShowMoreButton(onClick: () -> Unit) {
    Button(
        text = stringResource(R.string.ngc_showMore),
        onClick = onClick,
        variant = ButtonVariant.Secondary,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ngcAssignmentDetails_showMore"),
    )
}
