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

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.instui.compose.input.Button
import com.instructure.instui.compose.input.ButtonVariant
import com.instructure.instui.compose.input.TextButton
import com.instructure.instui.compose.text.Heading
import com.instructure.instui.compose.text.HeadingLevel
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.circleStop
import com.instructure.instui.token.icon.mic
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

// The legacy floating recorder (FloatingRecordingView) captured to AMR_NB. AMR_NB is an audio-only,
// low-bitrate container from the 3GPP telephony world; MPEG_4/AAC is the modern equivalent InstUI
// media players and most external tooling expect, so the in-app recorder targets that instead.
private const val AUDIO_CONTENT_TYPE = "audio/mp4"
private const val AUDIO_FILE_EXTENSION = "m4a"

// A recorder stopped within this window almost never has usable audio (MediaRecorder.stop() can
// even throw "stop failed" if called immediately after start()) — treat it as an empty recording
// rather than handing a near-silent/corrupt clip to the caller.
private const val MIN_RECORDING_DURATION_MS = 300L

private const val TIMER_TICK_MS = 200L

/**
 * Recording state machine for [AudioRecorderHolder]. There is no `Recorded`/review state — once a
 * take is stopped, [AudioRecorderHolder.stopRecording] resolves the whole stop-then-attach flow in
 * one call (see its kdoc) and the holder goes straight back to [Idle]. Replaying an attached clip
 * is handled by the attached-clip [com.instructure.pandautils.compose.composables.filedetails.MediaFileContent]
 * player in [MediaSubmissionContent], not by anything this holder owns.
 */
internal sealed interface AudioRecorderState {
    data object Idle : AudioRecorderState
    data class Recording(val elapsedMs: Long) : AudioRecorderState
}

/** Reasons a recording attempt did not end up producing a usable clip. */
internal enum class AudioRecorderError {
    START_FAILED,
    EMPTY_RECORDING,
}

/**
 * Formats an elapsed/position duration as `mm:ss`. Pure function — the only piece of the
 * recorder's UI-facing logic that's practical to unit test without a full MediaRecorder/
 * MediaPlayer harness.
 */
internal fun formatElapsed(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

/**
 * Guards against attaching an empty/near-empty/corrupt recording (e.g. the user taps stop
 * immediately after tapping record, or `MediaRecorder.stop()` itself failed). Pure function so
 * the guard is unit-testable independent of the MediaRecorder/File plumbing that calls it.
 *
 * [stopSucceeded] must be `false` whenever `MediaRecorder.stop()` threw — a thrown stop() means
 * the output container was never finalized, so the file is unusable even if it's non-empty and
 * the capture ran past [MIN_RECORDING_DURATION_MS].
 */
internal fun isRecordingUsable(fileLength: Long, elapsedMs: Long, stopSucceeded: Boolean): Boolean =
    stopSucceeded && fileLength > 0L && elapsedMs >= MIN_RECORDING_DURATION_MS

// MediaRecorder(Context) is only available on API 31+ (Build.VERSION_CODES.S) and is the
// platform-recommended constructor there (it lets the OS attribute the mic capture to this app
// for the privacy indicator); the no-arg MediaRecorder() is deprecated starting on that same API
// level. minSdk here is 28, so both branches are reachable on real devices and must be kept.
private fun newMediaRecorder(context: Context): MediaRecorder =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
    } else {
        @Suppress("DEPRECATION")
        MediaRecorder()
    }

private fun createOutputFile(context: Context): File? {
    val cacheDir = context.externalCacheDir ?: context.cacheDir ?: return null
    return File(cacheDir, "audio_${System.currentTimeMillis()}.$AUDIO_FILE_EXTENSION")
}

/**
 * Renames a just-stopped recording to a distinct filename in the same directory (an instant,
 * same-filesystem operation — no byte copy) so the attached clip's filename is no longer the same
 * "audio_*" pattern used for an in-progress capture — e.g. useful for any future temp-file
 * bookkeeping that wants to tell an attached, submission-owned file apart from a stray
 * capture-in-progress one by name alone.
 *
 * [rename] is a seam for unit testing the (rare) failure path — production callers use the
 * default, which is the real [File.renameTo]. Falls back to returning [file] unchanged if the
 * rename fails, rather than losing the clip.
 */
internal fun renameToConfirmedClipFile(
    file: File,
    rename: (File, File) -> Boolean = File::renameTo,
): File {
    val target = File(file.parentFile, "clip_${System.currentTimeMillis()}.$AUDIO_FILE_EXTENSION")
    return if (rename(file, target)) target else file
}

/**
 * Builds the attached-clip [FileSubmitObject] for an already-usable recording: renames [file] to
 * its final filename (see [renameToConfirmedClipFile]) and wraps it. Pure aside from the rename's
 * filesystem side effect, so it's unit-testable without a live [MediaRecorder].
 */
internal fun buildConfirmedClip(
    file: File,
    rename: (File, File) -> Boolean = File::renameTo,
): FileSubmitObject {
    val finalFile = renameToConfirmedClipFile(file, rename)
    return FileSubmitObject(
        name = finalFile.name,
        size = finalFile.length(),
        contentType = AUDIO_CONTENT_TYPE,
        fullPath = finalFile.absolutePath,
    )
}

/**
 * State holder wrapping [MediaRecorder] (capture) for the in-app audio recorder. Reuses the
 * capture-configuration approach from the legacy `FloatingRecordingView` (mic source, output file
 * under the app's cache dir) but targets a modern MPEG_4/AAC container instead of the legacy
 * AMR_NB one. A take auto-attaches on [stopRecording] — there is no separate confirm step (see
 * [AudioRecorderState]).
 *
 * Not a Composable itself — obtain one via [rememberAudioRecorderHolder], which ties [discard] to
 * the host Composable's lifecycle so the native objects (and any orphaned temp file) are never
 * leaked.
 */
internal class AudioRecorderHolder(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    var state by mutableStateOf<AudioRecorderState>(AudioRecorderState.Idle)
        private set

    var error by mutableStateOf<AudioRecorderError?>(null)
        private set

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var recordingStartMs: Long = 0L
    private var timerJob: Job? = null

    fun startRecording() {
        if (state !is AudioRecorderState.Idle) return
        error = null
        val file = createOutputFile(context)
        if (file == null) {
            error = AudioRecorderError.START_FAILED
            return
        }
        val recorder = try {
            newMediaRecorder(context).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            file.delete()
            error = AudioRecorderError.START_FAILED
            return
        }
        mediaRecorder = recorder
        outputFile = file
        recordingStartMs = System.currentTimeMillis()
        state = AudioRecorderState.Recording(0L)
        timerJob = scope.launch {
            while (isActive) {
                delay(TIMER_TICK_MS)
                state = AudioRecorderState.Recording(System.currentTimeMillis() - recordingStartMs)
            }
        }
    }

    /**
     * Stops the in-flight recording and, if it produced a usable clip, returns it as an
     * attach-ready [FileSubmitObject] — [MediaRecorder.stop] is synchronous, so (unlike the video
     * recorder, whose outcome is only known once CameraX's async `Finalize` event arrives, see
     * [VideoRecorderHolder.stopRecording]) this resolves the whole stop-then-attach decision in one
     * call. Always returns to [AudioRecorderState.Idle]. Returns null — with
     * [AudioRecorderError.EMPTY_RECORDING] set — if there's nothing to stop or the take turned out
     * to be unusable (see [isRecordingUsable]); callers must not attach a null result.
     */
    fun stopRecording(): FileSubmitObject? {
        val recorder = mediaRecorder ?: return null
        timerJob?.cancel()
        timerJob = null
        val elapsedMs = System.currentTimeMillis() - recordingStartMs
        val file = outputFile
        val stopSucceeded = try {
            recorder.stop()
            true
        } catch (e: Exception) {
            // stop() can throw even on a >300ms take that produced bytes, if the container was
            // never finalized (e.g. a mic hiccup mid-capture) — the file can be non-empty and
            // long enough by isRecordingUsable()'s other checks, so this flag must be threaded
            // through explicitly rather than relying on file length/elapsed time to catch it.
            false
        }
        recorder.release()
        mediaRecorder = null
        outputFile = null
        state = AudioRecorderState.Idle

        if (file == null || !isRecordingUsable(file.length(), elapsedMs, stopSucceeded)) {
            file?.delete()
            error = AudioRecorderError.EMPTY_RECORDING
            return null
        }

        return buildConfirmedClip(file)
    }

    /** Stops any in-flight recording/playback and deletes the temp file. Call when the user cancels
     * mid-take, or when this leaves composition (see [rememberAudioRecorderHolder]) — either way,
     * anything under [outputFile] at that point is an orphaned capture that was never attached. */
    fun discard() {
        if (state is AudioRecorderState.Recording) {
            try {
                mediaRecorder?.stop()
            } catch (e: Exception) {
                // Discarding regardless of whether stop() produced a valid file.
            }
        }
        release()
        outputFile?.delete()
        outputFile = null
        state = AudioRecorderState.Idle
        error = null
    }

    /** Releases the native recorder without touching the output file. Call on dispose. */
    fun release() {
        timerJob?.cancel()
        timerJob = null
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            // Already released/invalid state — nothing further to clean up.
        }
        mediaRecorder = null
    }

    /**
     * True while a take is in progress — i.e. there's something a submission-type switch would
     * silently discard, and the window during which orientation should be locked (see
     * [LockOrientationWhileActive]). Drives the type-selector lock in
     * [com.instructure.ngc.features.submission.NGCSubmissionScreen].
     */
    val hasUnconfirmedTake: Boolean
        get() = state is AudioRecorderState.Recording
}

/** Remembers an [AudioRecorderHolder] and discards it (releasing native resources and deleting any
 * orphaned temp file) when this leaves composition. */
@Composable
internal fun rememberAudioRecorderHolder(): AudioRecorderHolder {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val holder = remember { AudioRecorderHolder(context, scope) }
    DisposableEffect(holder) {
        onDispose { holder.discard() }
    }
    return holder
}

/**
 * Pure state+lambdas presentation of the in-app audio recorder: Record at [AudioRecorderState.Idle],
 * or a running `mm:ss` timer with Stop/Cancel at [AudioRecorderState.Recording]. Hosted inline in the
 * media main-pane body (see [MediaSubmissionContent]) rather than in a modal sheet, so the non-modal
 * "additional info" supporting pane stays visible and usable over it while recording.
 *
 * Stopping auto-attaches the take (see [AudioRecorderHolder.stopRecording]) — there is no separate
 * confirm/review step. Callers must gate [onStart] on the `RECORD_AUDIO` permission already being
 * granted.
 */
@Composable
internal fun AudioRecorderContent(
    state: AudioRecorderState,
    error: AudioRecorderError?,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
                vertical = InstUILayoutSizes.Spacing.SpaceMd.spaceMd,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Heading(
            text = stringResource(R.string.ngc_submission_audioRecorderTitle),
            level = HeadingLevel.H3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
        )

        val elapsedMs = when (state) {
            is AudioRecorderState.Recording -> state.elapsedMs
            AudioRecorderState.Idle -> 0L
        }
        val timerText = formatElapsed(elapsedMs)
        val timerContentDescription = stringResource(R.string.a11y_submission_audioRecorderTimer, timerText)
        Text(
            text = timerText,
            style = InstUIText.contentImportant.copy(fontSize = InstUIText.fontSizeXLarge),
            color = InstUISemanticColors.Text.base(),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = InstUILayoutSizes.Spacing.SpaceMd.spaceMd)
                .semantics {
                    contentDescription = timerContentDescription
                },
        )

        val errorText = when (error) {
            AudioRecorderError.EMPTY_RECORDING -> stringResource(R.string.ngc_submission_audioRecorderEmptyRecording)
            AudioRecorderError.START_FAILED -> stringResource(R.string.ngc_submission_audioRecorderStartFailed)
            null -> null
        }
        if (errorText != null) {
            Text(
                text = errorText,
                style = InstUIText.content,
                color = InstUISemanticColors.Text.error(),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
            )
        }

        when (state) {
            AudioRecorderState.Idle -> {
                Button(
                    text = stringResource(R.string.ngc_submission_audioRecorderRecord),
                    onClick = onStart,
                    icon = InstUIIcons.mic,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            is AudioRecorderState.Recording -> {
                Button(
                    text = stringResource(R.string.ngc_submission_audioRecorderStop),
                    onClick = onStop,
                    icon = InstUIIcons.circleStop,
                    variant = ButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(InstUILayoutSizes.Spacing.SpaceSm.spaceSm))
                TextButton(
                    text = stringResource(R.string.ngc_submission_audioRecorderCancel),
                    onClick = onCancel,
                )
            }
        }
    }
}
