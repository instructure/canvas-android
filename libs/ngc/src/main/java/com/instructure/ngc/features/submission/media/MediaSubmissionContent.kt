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
package com.instructure.ngc.features.submission.media

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.input.Button
import com.instructure.instui.compose.input.ButtonVariant
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.folder
import com.instructure.instui.token.icon.mic
import com.instructure.instui.token.icon.video
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R
import com.instructure.pandautils.compose.composables.filedetails.MediaFileContent
import com.instructure.pandautils.utils.getFragmentActivity
import java.io.File

/**
 * Main-pane body for the MEDIA_RECORDING submission type.
 *
 * Media is a single clip per submission:
 * - When [clipName] is null, defaults to hosting the in-app audio recorder
 *   ([AudioRecorderContent]) inline — idle timer + Record by default — with "Record video" and
 *   "Choose a file" as secondary affordances beneath it. The secondary affordances are hidden
 *   while a take is in progress, so a mid-take tap can't switch sources and orphan the take.
 *   Choosing "Record video" swaps the body to the in-app CameraX recorder ([VideoRecorderContent])
 *   instead — no system camera intent — which offers the mirror-image "Record audio" + "Choose a
 *   file" secondary affordances at its own idle.
 * - Stopping either recorder auto-attaches the take (see [AudioRecorderHolder.stopRecording] /
 *   [VideoRecorderHolder.handleFinalize]) — there is no separate confirm step. Once attached, the
 *   clip shows in an embedded [MediaFileContent] player (audio or video, both play back the same
 *   way) so the student can replay the take. Removing/replacing the clip goes through the shared
 *   "Delete draft" affordance on the submission form (the same one every submission type uses),
 *   not a separate per-clip delete.
 *
 * Both recorders are hosted inline (rather than in a modal sheet) so the non-modal "additional
 * info" supporting pane — peeking at the bottom on phones, a side pane on tablets — stays visible
 * and usable over them. [onUnconfirmedTakeChanged] reports whether a take is currently in progress
 * so the call-site (see [com.instructure.ngc.features.submission.NGCSubmissionScreen]) can lock
 * the submission-type selector while there's something a type switch would silently discard. The
 * same signal also drives [LockOrientationWhileActive] so a live capture isn't disrupted by a
 * rotation mid-take. Audio and video report this the same way —
 * [AudioRecorderHolder.hasUnconfirmedTake] / [VideoRecorderHolder.hasUnconfirmedTake], true only
 * once recording starts (through CameraX finalizing, for video), not merely for being in that
 * recorder's idle preview. `videoRecordMode` itself is retained only to choose which recorder
 * subtree renders.
 *
 * When [enabled] is `false` all interactive affordances are visually muted and non-interactive.
 */
@Composable
fun MediaSubmissionContent(
    clipName: String?,
    clipPath: String?,
    clipContentType: String?,
    onClipReady: (FileSubmitObject) -> Unit,
    onChooseMediaFile: () -> Unit,
    onRequestAudioPermission: (onGranted: () -> Unit) -> Unit,
    audioPermissionDenied: Boolean,
    onRequestVideoPermission: (onGranted: () -> Unit) -> Unit,
    videoPermissionDenied: Boolean,
    onUnconfirmedTakeChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    // clipName/clipPath/clipContentType are always written together (see
    // com.instructure.ngc.features.submission.NGCSubmissionViewModel) — clipUri is null only when
    // no clip is attached. A blank clipPath (e.g. a corrupt restored draft) is treated the same as
    // no clip, rather than building a bogus File("").toUri().
    val clipUri = if (clipPath.isNullOrBlank()) null else File(clipPath).toUri()
    if (clipName == null || clipUri == null) {
        // Not saveable: an in-progress video-record session ties up the camera and can't survive
        // process death anyway (mirrors AudioRecorderHolder's own non-saveable state).
        var videoRecordMode by remember { mutableStateOf(false) }
        var audioTakeInProgress by remember { mutableStateOf(false) }
        var videoTakeInProgress by remember { mutableStateOf(false) }

        // Single source of truth for the type-selector lock, rather than letting both the audio
        // and video branches below call onUnconfirmedTakeChanged directly — they're mutually
        // exclusive subtrees swapped via videoRecordMode, and relying on each branch's own
        // mount/dispose effects to race to the right answer would be order-dependent.
        // videoRecordMode itself only chooses which recorder subtree renders — the lock tracks
        // audio/video's own hasUnconfirmedTake (recording/being finalized), so idle camera preview
        // does not lock the selector, matching the audio recorder's idle behavior.
        val takeInProgress = audioTakeInProgress || videoTakeInProgress
        LaunchedEffect(takeInProgress) { onUnconfirmedTakeChanged(takeInProgress) }
        DisposableEffect(Unit) { onDispose { onUnconfirmedTakeChanged(false) } }

        if (videoRecordMode) {
            InlineVideoRecorder(
                onClipReady = { clip ->
                    videoRecordMode = false
                    onClipReady(clip)
                },
                onSwitchToAudio = { videoRecordMode = false },
                onChooseMediaFile = onChooseMediaFile,
                onUnconfirmedTakeChanged = { videoTakeInProgress = it },
                enabled = enabled,
                modifier = modifier,
            )
        } else {
            InlineAudioRecorder(
                onClipReady = onClipReady,
                onRecordVideo = { onRequestVideoPermission { videoRecordMode = true } },
                onChooseMediaFile = onChooseMediaFile,
                onRequestAudioPermission = onRequestAudioPermission,
                audioPermissionDenied = audioPermissionDenied,
                videoPermissionDenied = videoPermissionDenied,
                onUnconfirmedTakeChanged = { audioTakeInProgress = it },
                enabled = enabled,
                modifier = modifier,
            )
        }
    } else {
        MediaClipCard(
            uri = clipUri,
            name = clipName,
            contentType = clipContentType,
            modifier = modifier,
        )
    }
}

/**
 * Locks the host Activity's orientation while [active] is true (e.g. a recording/finalizing take
 * in progress), restoring free rotation when [active] goes false or this leaves composition.
 * Scoped to the recorder composables ([InlineAudioRecorder]/[InlineVideoRecorder]) rather than
 * anything screen-wide, so the rest of NGC is unaffected.
 *
 * `NGCActivity` already declares `configChanges="orientation|screenSize"`, so a live capture
 * survives a rotation regardless of this lock — this is belt-and-suspenders so an in-flight
 * `MediaRecorder`/CameraX capture is never disrupted by a rotation event mid-take.
 */
@Composable
internal fun LockOrientationWhileActive(active: Boolean) {
    val activity = LocalContext.current.getFragmentActivity()
    DisposableEffect(active) {
        if (active) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        }
        onDispose {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
}

/**
 * Hosts the [AudioRecorderHolder]/[AudioRecorderContent] pair inline and the "Record video" /
 * "Choose a file" secondary affordances beneath it. Reports [AudioRecorderHolder.hasUnconfirmedTake]
 * upward via [onUnconfirmedTakeChanged] — including a `false` on dispose, so navigating away from
 * the media type (which tears down this composable and its holder) can't leave the type selector
 * locked.
 */
@Composable
private fun InlineAudioRecorder(
    onClipReady: (FileSubmitObject) -> Unit,
    onRecordVideo: () -> Unit,
    onChooseMediaFile: () -> Unit,
    onRequestAudioPermission: (onGranted: () -> Unit) -> Unit,
    audioPermissionDenied: Boolean,
    videoPermissionDenied: Boolean,
    onUnconfirmedTakeChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val holder = rememberAudioRecorderHolder()

    LaunchedEffect(holder.state) {
        onUnconfirmedTakeChanged(holder.hasUnconfirmedTake)
    }
    DisposableEffect(Unit) {
        onDispose { onUnconfirmedTakeChanged(false) }
    }
    LockOrientationWhileActive(active = holder.hasUnconfirmedTake)

    Column(modifier = modifier.fillMaxWidth()) {
        AudioRecorderContent(
            state = holder.state,
            error = holder.error,
            onStart = { if (enabled) onRequestAudioPermission { holder.startRecording() } },
            onStop = {
                if (enabled) {
                    val clip = holder.stopRecording()
                    if (clip != null) onClipReady(clip)
                }
            },
            onCancel = { if (enabled) holder.discard() },
        )

        if (audioPermissionDenied) {
            Text(
                text = stringResource(R.string.ngc_submission_audioPermissionRequired),
                style = InstUIText.contentSmall,
                color = InstUISemanticColors.Text.error(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
                        vertical = InstUILayoutSizes.Spacing.SpaceSm.spaceSm,
                    ),
            )
        }

        // The CAMERA + RECORD_AUDIO gate for "Record video" is checked before switching into the
        // video recorder, so a denial keeps the user here (in the default audio-recorder view)
        // rather than inside a half-permissioned camera preview.
        if (videoPermissionDenied) {
            Text(
                text = stringResource(R.string.ngc_submission_videoPermissionRequired),
                style = InstUIText.contentSmall,
                color = InstUISemanticColors.Text.error(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
                        vertical = InstUILayoutSizes.Spacing.SpaceSm.spaceSm,
                    ),
            )
        }

        // Hidden mid-take so a tap can't switch sources and orphan an in-progress recording.
        if (!holder.hasUnconfirmedTake) {
            MediaSecondaryOptions(
                switchLabel = stringResource(R.string.ngc_submission_sourceRecordVideo),
                switchIcon = InstUIIcons.video,
                // Distinct from FilePicker.kt's use of the same row vocabulary: this one enters the
                // in-app inline recorder rather than opening the system camera app.
                switchContentDescription = stringResource(R.string.a11y_submission_sourceRecordVideoInline),
                onSwitchRecorder = onRecordVideo,
                onChooseMediaFile = onChooseMediaFile,
                enabled = enabled,
                modifier = Modifier.padding(horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
            )
        }
    }
}

/**
 * Hosts the [VideoRecorderHolder]/[VideoRecorderContent] pair inline and the mirror-image
 * "Record audio" / "Choose a file" secondary affordances beneath it (see [InlineAudioRecorder]).
 * Reports [VideoRecorderHolder.hasUnconfirmedTake] upward via [onUnconfirmedTakeChanged] —
 * including a `false` on dispose — mirroring [InlineAudioRecorder] exactly, so at idle camera
 * preview (before recording starts or a take is being finalized) the type selector is not locked;
 * the camera being bound doesn't by itself mean there's anything a type switch would discard.
 */
@Composable
private fun InlineVideoRecorder(
    onClipReady: (FileSubmitObject) -> Unit,
    onSwitchToAudio: () -> Unit,
    onChooseMediaFile: () -> Unit,
    onUnconfirmedTakeChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val holder = rememberVideoRecorderHolder(onClipReady = onClipReady)

    LaunchedEffect(holder.state) {
        onUnconfirmedTakeChanged(holder.hasUnconfirmedTake)
    }
    DisposableEffect(Unit) {
        onDispose { onUnconfirmedTakeChanged(false) }
    }
    LockOrientationWhileActive(active = holder.hasUnconfirmedTake)

    Column(modifier = modifier.fillMaxWidth()) {
        VideoRecorderContent(
            state = holder.state,
            error = holder.error,
            previewUseCase = holder.previewUseCase,
            onStart = { if (enabled) holder.startRecording() },
            onStop = { if (enabled) holder.stopRecording() },
            onCancel = {
                if (enabled) {
                    holder.discard()
                    onSwitchToAudio()
                }
            },
        )

        // Hidden mid-take so a tap can't switch sources and orphan an in-progress/being-finalized
        // recording.
        if (!holder.hasUnconfirmedTake) {
            MediaSecondaryOptions(
                switchLabel = stringResource(R.string.ngc_submission_sourceRecordAudio),
                switchIcon = InstUIIcons.mic,
                switchContentDescription = stringResource(R.string.a11y_submission_sourceRecordAudioInline),
                onSwitchRecorder = onSwitchToAudio,
                onChooseMediaFile = onChooseMediaFile,
                enabled = enabled,
                modifier = Modifier.padding(horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
            )
        }
    }
}

/**
 * "Record video"/"Record audio" (whichever is the *other* recorder) + "Choose a file" secondary
 * affordances shown beneath the inline recorder at idle. Rendered as InstUI [Button]s (secondary
 * variant) rather than plain clickable rows so they pick up the same rounded-ripple touch feedback
 * as every other button in the recorder (Record/Stop/Cancel) — the earlier row styling had no
 * ripple bounds of its own.
 */
@Composable
private fun MediaSecondaryOptions(
    switchLabel: String,
    switchIcon: ImageVector,
    switchContentDescription: String,
    onSwitchRecorder: () -> Unit,
    onChooseMediaFile: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val chooseMediaFileDescription = stringResource(R.string.a11y_submission_sourceChooseMediaFile)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
    ) {
        Button(
            text = switchLabel,
            onClick = onSwitchRecorder,
            icon = switchIcon,
            variant = ButtonVariant.Secondary,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) { contentDescription = switchContentDescription },
        )
        Button(
            text = stringResource(R.string.ngc_submission_sourceChooseMediaFile),
            onClick = onChooseMediaFile,
            icon = InstUIIcons.folder,
            variant = ButtonVariant.Secondary,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) { contentDescription = chooseMediaFileDescription },
        )
    }
}

/**
 * The attached clip: an embedded [MediaFileContent] player (audio and video both play back
 * through the same Media3-backed component) so the take can be replayed in place, with the clip
 * name beneath it. Removal is handled by the shared "Delete draft" affordance on the form, not a
 * per-clip delete. [key]ed on [uri] so attaching a different clip (delete-then-add) tears
 * down and recreates the player/`ExoAgent` rather than reusing a stale one — mirrors
 * [com.instructure.ngc.features.assignmentdetails.NGCSubmissionPreview]'s usage of the same
 * composable.
 *
 * For this local, not-yet-submitted draft there's nowhere useful to send a fullscreen tap (the
 * app's fullscreen player, [com.instructure.pandautils.activities.BaseViewMediaActivity] reached
 * via [com.instructure.pandautils.navigation.WebViewRouter.openMediaFullScreen], is remote-URL
 * oriented), so the fullscreen control is hidden via `showFullScreenButton = false` and
 * `onFullScreenClicked` is an unreachable no-op.
 */
@Composable
private fun MediaClipCard(
    uri: Uri,
    name: String,
    contentType: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
    ) {
        key(uri) {
            MediaFileContent(
                uri = uri,
                contentType = contentType.orEmpty(),
                onFullScreenClicked = { _, _ -> },
                showFullScreenButton = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CLIP_PLAYER_HEIGHT)
                    .clip(RoundedCornerShape(InstUILayoutSizes.BorderRadius.Container.sm))
                    .border(
                        width = InstUILayoutSizes.BorderWidth.Sm.sm,
                        color = InstUISemanticColors.Stroke.Container.base(),
                        shape = RoundedCornerShape(InstUILayoutSizes.BorderRadius.Container.sm),
                    ),
            )
        }

        Text(
            text = name,
            style = InstUIText.contentSmall,
            color = InstUISemanticColors.Text.base(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Fixed height for the embedded clip player, so it reads well inside the scrolling media body. */
internal val CLIP_PLAYER_HEIGHT = 200.dp

// region Previews

// AudioRecorderContent drives a live MediaRecorder/MediaPlayer AudioRecorderHolder, which isn't
// safe to construct in a @Preview (no real mic/codec available in the renderer). These previews
// render AudioRecorderContent directly with hand-built states instead of going through
// MediaSubmissionContent + rememberAudioRecorderHolder(), so they stay meaningful without a live
// holder.

@Preview(name = "MediaSubmissionContent — idle recorder + secondary options", showBackground = true)
@Composable
private fun MediaSubmissionContentIdlePreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(vertical = InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            AudioRecorderContent(
                state = AudioRecorderState.Idle,
                error = null,
                onStart = {},
                onStop = {},
                onCancel = {},
            )
            MediaSecondaryOptions(
                switchLabel = stringResource(R.string.ngc_submission_sourceRecordVideo),
                switchIcon = InstUIIcons.video,
                switchContentDescription = "",
                onSwitchRecorder = {},
                onChooseMediaFile = {},
                modifier = Modifier.padding(horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
            )
        }
    }
}

@Preview(name = "MediaSubmissionContent — active take (secondary options hidden)", showBackground = true)
@Composable
private fun MediaSubmissionContentActiveTakePreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(vertical = InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            AudioRecorderContent(
                state = AudioRecorderState.Recording(elapsedMs = 12_000L),
                error = null,
                onStart = {},
                onStop = {},
                onCancel = {},
            )
        }
    }
}

// VideoRecorderHolder drives a live ProcessCameraProvider/Recorder, which isn't safe to construct
// in a @Preview (no real camera available in the renderer). These previews render
// VideoRecorderContent directly with hand-built states and previewUseCase = null (which itself
// renders a static placeholder box rather than a live feed), so they stay meaningful without a
// live holder.

@Preview(name = "MediaSubmissionContent — video recorder idle (camera binding)", showBackground = true)
@Composable
private fun MediaSubmissionContentVideoIdlePreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(vertical = InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            VideoRecorderContent(
                state = VideoRecorderState.Idle,
                error = null,
                previewUseCase = null,
                onStart = {},
                onStop = {},
                onCancel = {},
            )
            MediaSecondaryOptions(
                switchLabel = stringResource(R.string.ngc_submission_sourceRecordAudio),
                switchIcon = InstUIIcons.mic,
                switchContentDescription = "",
                onSwitchRecorder = {},
                onChooseMediaFile = {},
                modifier = Modifier.padding(horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
            )
        }
    }
}

@Preview(name = "MediaSubmissionContent — video recorder processing", showBackground = true)
@Composable
private fun MediaSubmissionContentVideoProcessingPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(vertical = InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            VideoRecorderContent(
                state = VideoRecorderState.Processing(elapsedMs = 8_000L),
                error = null,
                previewUseCase = null,
                onStart = {},
                onStop = {},
                onCancel = {},
            )
        }
    }
}

// MediaClipCard renders MediaFileContent, which drives a live ExoPlayer/ExoAgent — same
// not-safe-in-@Preview concern as the AudioRecorderHolder above. These previews render a
// placeholder block standing in for the player instead of going through MediaClipCard, so they
// stay meaningful (name layout) without constructing a real player.

@Composable
private fun MediaClipCardPreviewStandIn(name: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(CLIP_PLAYER_HEIGHT)
                .clip(RoundedCornerShape(InstUILayoutSizes.BorderRadius.Container.sm))
                .background(InstUISemanticColors.Background.muted()),
        )
        Text(
            text = name,
            style = InstUIText.contentSmall,
            color = InstUISemanticColors.Text.base(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(name = "MediaSubmissionContent — with video clip", showBackground = true)
@Composable
private fun MediaSubmissionContentWithVideoPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            MediaClipCardPreviewStandIn(name = "vid_1700000000000.mp4")
        }
    }
}

@Preview(name = "MediaSubmissionContent — with audio clip", showBackground = true)
@Composable
private fun MediaSubmissionContentWithAudioPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            MediaClipCardPreviewStandIn(name = "recording.m4a")
        }
    }
}

@Preview(
    name = "MediaSubmissionContent — with video clip (Dark)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun MediaSubmissionContentDarkPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            MediaClipCardPreviewStandIn(name = "vid_1700000000000.mp4")
        }
    }
}

// endregion
