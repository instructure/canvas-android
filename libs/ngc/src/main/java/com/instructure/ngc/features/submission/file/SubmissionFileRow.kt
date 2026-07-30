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

package com.instructure.ngc.features.submission.file

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.file
import com.instructure.instui.token.icon.fileAudio
import com.instructure.instui.token.icon.fileImage
import com.instructure.instui.token.icon.fileText
import com.instructure.instui.token.icon.fileVideo
import com.instructure.instui.token.icon.refreshCw
import com.instructure.instui.token.icon.trash2
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R
import com.instructure.ngc.features.submission.SubmissionFileState
import com.instructure.ngc.features.submission.SubmissionFileUiState
import java.io.File

/**
 * A single row representing an attached file in the file-upload submission.
 *
 * Renders a leading MIME-type icon in a tinted square, the file name and
 * human-readable size, and a trailing action that depends on [SubmissionFileState]:
 *   - [SubmissionFileState.Ready]      → destructive trash icon button
 *   - [SubmissionFileState.InProgress] → indeterminate spinner (or determinate if progress is set)
 *                                        with an optional linear progress track; no cancel affordance
 *   - [SubmissionFileState.Error]      → error-tinted row border + delete (trash) icon button
 *
 * When [enabled] is `false` delete and retry actions are suppressed (rows in
 * [SubmissionFileState.InProgress] already show no action, so the effect is mainly
 * on [SubmissionFileState.Ready] rows queued but not yet started during an upload).
 */
@Composable
fun SubmissionFileRow(
    file: SubmissionFileUiState,
    onDelete: (id: Long) -> Unit,
    onRetry: (id: Long) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val context = LocalContext.current
    val isError = file.state is SubmissionFileState.Error
    val borderColor = if (isError) {
        InstUISemanticColors.Stroke.error()
    } else {
        InstUISemanticColors.Stroke.Container.base()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(InstUILayoutSizes.BorderRadius.Container.sm))
            .border(
                width = InstUILayoutSizes.BorderWidth.Sm.sm,
                color = borderColor,
                shape = RoundedCornerShape(InstUILayoutSizes.BorderRadius.Container.sm),
            )
            .background(InstUISemanticColors.Background.container())
            .padding(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
        verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
        ) {
            FileTypeIcon(contentType = file.contentType, fullPath = file.fullPath)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = InstUIText.contentSmall,
                    color = InstUISemanticColors.Text.base(),
                )
                Text(
                    text = NumberHelper.readableFileSize(context, file.size),
                    style = InstUIText.legend,
                    color = InstUISemanticColors.Text.muted(),
                )
                if (isError) {
                    Text(
                        text = (file.state as SubmissionFileState.Error).message,
                        style = InstUIText.legend,
                        color = InstUISemanticColors.Text.error(),
                    )
                }
            }

            when (val state = file.state) {
                is SubmissionFileState.Ready -> {
                    Icon(
                        imageVector = InstUIIcons.trash2,
                        contentDescription = stringResource(R.string.a11y_submission_fileDelete),
                        color = if (enabled) IconColor.Error else IconColor.Muted,
                        size = IconSize.Medium,
                        modifier = if (enabled) {
                            Modifier
                                .clip(RoundedCornerShape(InstUILayoutSizes.BorderRadius.Interactive.base))
                                .clickable(role = Role.Button) { onDelete(file.id) }
                                .padding(InstUILayoutSizes.Spacing.SpaceXs.spaceXs)
                        } else {
                            Modifier.padding(InstUILayoutSizes.Spacing.SpaceXs.spaceXs)
                        },
                    )
                }
                is SubmissionFileState.InProgress -> {
                    if (state.progress != null) {
                        Spacer(Modifier.width(4.dp))
                    } else {
                        CircularProgressIndicator(
                            color = InstUISemanticColors.Text.Interactive.Action.Tertiary.base(),
                            modifier = Modifier.size(IconSize.Medium.dp),
                            strokeWidth = InstUILayoutSizes.BorderWidth.Interactive.focus,
                        )
                    }
                }
                is SubmissionFileState.Error -> {
                    // Failed rows offer both retry (re-run the upload) and delete (drop this file
                    // and submit the rest).
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
                    ) {
                        Icon(
                            imageVector = InstUIIcons.refreshCw,
                            contentDescription = stringResource(R.string.a11y_submission_fileRetry),
                            color = if (enabled) IconColor.Info else IconColor.Muted,
                            size = IconSize.Medium,
                            modifier = if (enabled) {
                                Modifier
                                    .clip(RoundedCornerShape(InstUILayoutSizes.BorderRadius.Interactive.base))
                                    .clickable(role = Role.Button) { onRetry(file.id) }
                                    .padding(InstUILayoutSizes.Spacing.SpaceXs.spaceXs)
                            } else {
                                Modifier.padding(InstUILayoutSizes.Spacing.SpaceXs.spaceXs)
                            },
                        )
                        Icon(
                            imageVector = InstUIIcons.trash2,
                            contentDescription = stringResource(R.string.a11y_submission_fileDelete),
                            color = if (enabled) IconColor.Error else IconColor.Muted,
                            size = IconSize.Medium,
                            modifier = if (enabled) {
                                Modifier
                                    .clip(RoundedCornerShape(InstUILayoutSizes.BorderRadius.Interactive.base))
                                    .clickable(role = Role.Button) { onDelete(file.id) }
                                    .padding(InstUILayoutSizes.Spacing.SpaceXs.spaceXs)
                            } else {
                                Modifier.padding(InstUILayoutSizes.Spacing.SpaceXs.spaceXs)
                            },
                        )
                    }
                }
            }
        }

        // Determinate upload progress bar — only when progress is available
        if (file.state is SubmissionFileState.InProgress && file.state.progress != null) {
            LinearProgressIndicator(
                progress = { file.state.progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = InstUISemanticColors.Text.Interactive.Action.Tertiary.base(),
                trackColor = InstUISemanticColors.Background.muted(),
            )
        }
    }
}

/**
 * Maps a MIME type to an [InstUIIcons] glyph inside a tinted square chip.
 *
 * For image MIME types with a non-null [fullPath], renders a [GlideImage] thumbnail
 * crop-scaled to fill the 40dp box. The type icon sits in the background so it is
 * visible while Glide loads and acts as an implicit placeholder/fallback. For all
 * other MIME types only the icon is shown.
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun FileTypeIcon(contentType: String?, fullPath: String?, modifier: Modifier = Modifier) {
    val icon = when {
        contentType == null -> InstUIIcons.file
        contentType.startsWith("image/") -> InstUIIcons.fileImage
        contentType.startsWith("video/") -> InstUIIcons.fileVideo
        contentType.startsWith("audio/") -> InstUIIcons.fileAudio
        contentType.startsWith("text/") -> InstUIIcons.fileText
        else -> InstUIIcons.file
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(FILE_ICON_BOX_SIZE)
            .clip(RoundedCornerShape(InstUILayoutSizes.BorderRadius.Container.sm))
            .background(InstUISemanticColors.Background.muted()),
    ) {
        // Icon is always present; for image files it serves as the loading/error placeholder
        // beneath the GlideImage layer that renders once the file is decoded.
        Icon(
            imageVector = icon,
            contentDescription = null,
            size = IconSize.Medium,
            color = IconColor.Muted,
        )

        if (contentType?.startsWith("image/") == true && fullPath != null) {
            val file = remember(fullPath) { File(fullPath) }
            GlideImage(
                model = file,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private val FILE_ICON_BOX_SIZE = 40.dp

// region Previews

@Preview(name = "FileRow — Ready", showBackground = true)
@Composable
private fun FileRowReadyPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SubmissionFileRow(
                file = SubmissionFileUiState(
                    id = 1,
                    name = "biology_essay.pdf",
                    size = 2_340_000,
                    contentType = "application/pdf",
                    fullPath = null,
                    state = SubmissionFileState.Ready,
                ),
                onDelete = {},
                onRetry = {},
            )
            SubmissionFileRow(
                file = SubmissionFileUiState(
                    id = 2,
                    name = "diagram.png",
                    size = 512_000,
                    contentType = "image/png",
                    fullPath = null,
                    state = SubmissionFileState.Ready,
                ),
                onDelete = {},
                onRetry = {},
            )
        }
    }
}

@Preview(name = "FileRow — InProgress (indeterminate)", showBackground = true)
@Composable
private fun FileRowInProgressPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            SubmissionFileRow(
                file = SubmissionFileUiState(
                    id = 1,
                    name = "lecture_recording.mp4",
                    size = 84_000_000,
                    contentType = "video/mp4",
                    fullPath = null,
                    state = SubmissionFileState.InProgress(progress = null),
                ),
                onDelete = {},
                onRetry = {},
            )
        }
    }
}

@Preview(name = "FileRow — InProgress (determinate 42%)", showBackground = true)
@Composable
private fun FileRowInProgressDetermPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            SubmissionFileRow(
                file = SubmissionFileUiState(
                    id = 1,
                    name = "lecture_recording.mp4",
                    size = 84_000_000,
                    contentType = "video/mp4",
                    fullPath = null,
                    state = SubmissionFileState.InProgress(progress = 0.42f),
                ),
                onDelete = {},
                onRetry = {},
            )
        }
    }
}

@Preview(name = "FileRow — Error (retry + delete)", showBackground = true)
@Composable
private fun FileRowErrorPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            SubmissionFileRow(
                file = SubmissionFileUiState(
                    id = 1,
                    name = "notes.txt",
                    size = 4_096,
                    contentType = "text/plain",
                    fullPath = null,
                    state = SubmissionFileState.Error("Upload failed. Tap to retry."),
                ),
                onDelete = {},
                onRetry = {},
            )
        }
    }
}

@Preview(name = "FileRow — Ready (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FileRowReadyDarkPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SubmissionFileRow(
                file = SubmissionFileUiState(
                    id = 1,
                    name = "biology_essay.pdf",
                    size = 2_340_000,
                    contentType = "application/pdf",
                    fullPath = null,
                    state = SubmissionFileState.Ready,
                ),
                onDelete = {},
                onRetry = {},
            )
        }
    }
}

// endregion
