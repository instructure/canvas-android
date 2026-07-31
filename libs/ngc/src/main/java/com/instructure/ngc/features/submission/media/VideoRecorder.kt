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
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview as CameraXPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.instructure.instui.token.icon.video
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

private const val VIDEO_CONTENT_TYPE = "video/mp4"
private const val VIDEO_FILE_EXTENSION = "mp4"

// Mirrors AudioRecorder.kt's MIN_RECORDING_DURATION_MS guard: a clip finalized within this
// window (or whose Recorder.Finalize event reports an error) isn't a usable submission.
private const val MIN_RECORDING_DURATION_MS = 300L

private const val TIMER_TICK_MS = 200L

/** Fixed height for the inline live camera preview, so it reads well inside the scrolling media body. */
private val CAMERA_PREVIEW_HEIGHT = 240.dp

/**
 * Recording state machine for [VideoRecorderHolder]. There is no `Recorded`/review state — once
 * [stopRecording] is called the holder moves to [Processing] until CameraX's async `Finalize` event
 * arrives (see [VideoRecorderHolder.handleFinalize]), at which point a usable take auto-attaches
 * (via the holder's `onClipReady` callback) and the holder goes straight back to [Idle]. Replaying
 * an attached clip is handled by the attached-clip
 * [com.instructure.pandautils.compose.composables.filedetails.MediaFileContent] player in
 * [MediaSubmissionContent], not by anything this holder owns.
 */
internal sealed interface VideoRecorderState {
    data object Idle : VideoRecorderState
    data class Recording(val elapsedMs: Long) : VideoRecorderState
    data class Processing(val elapsedMs: Long) : VideoRecorderState
}

/** Reasons a recording attempt did not end up producing a usable clip. */
internal enum class VideoRecorderError {
    START_FAILED,
    EMPTY_RECORDING,
}

/**
 * Guards against attaching an empty/near-empty/corrupt recording — mirrors [isRecordingUsable]'s
 * role for the audio recorder. [finalizeSucceeded] must be `false` whenever the CameraX
 * `VideoRecordEvent.Finalize` callback reported an error (e.g. the camera was unbound mid-capture)
 * — a failed finalize means the container was never completed, so the file is unusable even if
 * it's non-empty and the capture ran past [MIN_RECORDING_DURATION_MS].
 */
internal fun isVideoRecordingUsable(fileLength: Long, elapsedMs: Long, finalizeSucceeded: Boolean): Boolean =
    finalizeSucceeded && fileLength > 0L && elapsedMs >= MIN_RECORDING_DURATION_MS

private fun createVideoOutputFile(context: Context): File? {
    val cacheDir = context.externalCacheDir ?: context.cacheDir ?: return null
    return File(cacheDir, "vid_${System.currentTimeMillis()}.$VIDEO_FILE_EXTENSION")
}

/**
 * Renames a just-finalized recording to a distinct filename in the same directory (an instant,
 * same-filesystem operation — no byte copy) so the attached clip's filename is no longer the same
 * "vid_*" pattern used for an in-progress capture. Mirrors [renameToConfirmedClipFile] in
 * `AudioRecorder.kt`.
 *
 * [rename] is a seam for unit testing the (rare) failure path — production callers use the
 * default, which is the real [File.renameTo]. Falls back to returning [file] unchanged if the
 * rename fails, rather than losing the clip.
 */
internal fun renameToConfirmedVideoClipFile(
    file: File,
    rename: (File, File) -> Boolean = File::renameTo,
): File {
    val target = File(file.parentFile, "clip_${System.currentTimeMillis()}.$VIDEO_FILE_EXTENSION")
    return if (rename(file, target)) target else file
}

/**
 * Builds the attached-clip [FileSubmitObject] for an already-usable recording: renames [file] to
 * its final filename (see [renameToConfirmedVideoClipFile]) and wraps it. Pure aside from the
 * rename's filesystem side effect, so it's unit-testable without a live CameraX `Recorder`.
 */
internal fun buildConfirmedVideoClip(
    file: File,
    rename: (File, File) -> Boolean = File::renameTo,
): FileSubmitObject {
    val finalFile = renameToConfirmedVideoClipFile(file, rename)
    return FileSubmitObject(
        name = finalFile.name,
        size = finalFile.length(),
        contentType = VIDEO_CONTENT_TYPE,
        fullPath = finalFile.absolutePath,
    )
}

/** Awaits [ProcessCameraProvider.getInstance] without pulling in a Guava `ListenableFuture` await extension. */
private suspend fun awaitCameraProvider(context: Context): ProcessCameraProvider =
    suspendCancellableCoroutine { continuation ->
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            {
                try {
                    continuation.resume(future.get())
                } catch (e: Exception) {
                    continuation.cancel(e)
                }
            },
            ContextCompat.getMainExecutor(context),
        )
    }

/**
 * State holder wrapping CameraX (`ProcessCameraProvider` + `Preview` + `VideoCapture<Recorder>`)
 * for the in-app video recorder. Structured to mirror [AudioRecorderHolder] as closely as
 * CameraX's async bind/record APIs allow: a `mutableStateOf` [state] + [error], a coroutine-driven
 * elapsed timer (no busy loop), and [discard] tied to the host Composable's lifecycle via
 * [rememberVideoRecorderHolder].
 *
 * A take auto-attaches once CameraX's `Finalize` event confirms it's usable — see [handleFinalize]
 * and [onClipReady] — rather than through a separate confirm step.
 *
 * The camera is bound (and its live preview starts) as soon as this holder is created — callers
 * must gate entering video-record mode on the `CAMERA` + `RECORD_AUDIO` permissions already being
 * granted (see [com.instructure.ngc.features.submission.media.MediaPickerController.requestVideoPermission]).
 */
internal class VideoRecorderHolder(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    var state by mutableStateOf<VideoRecorderState>(VideoRecorderState.Idle)
        private set

    var error by mutableStateOf<VideoRecorderError?>(null)
        private set

    /** The live-preview use case, exposed once the camera is bound, for [VideoRecorderContent] to surface. */
    var previewUseCase by mutableStateOf<CameraXPreview?>(null)
        private set

    /**
     * Invoked from [handleFinalize] once a stopped take is confirmed usable, with the attach-ready
     * clip. Kept as a plain settable property (rather than a constructor parameter) so
     * [rememberVideoRecorderHolder] can keep it current across recompositions via `SideEffect`
     * without needing to key the remembered holder on it.
     */
    var onClipReady: (FileSubmitObject) -> Unit = {}

    private var cameraProvider: ProcessCameraProvider? = null
    private var recorder: Recorder? = null
    private var activeRecording: Recording? = null
    private var outputFile: File? = null
    private var recordingStartMs: Long = 0L
    private var timerJob: Job? = null

    /** Set once [release] has run, so a `Finalize` event that arrives after the user already left
     * the recorder can't resurrect [state]/[error] on a torn-down holder. */
    private var isReleased: Boolean = false

    /** Binds the camera + starts the live preview. Call once, from [rememberVideoRecorderHolder]. */
    suspend fun bindCamera(lifecycleOwner: LifecycleOwner) {
        if (cameraProvider != null) return
        performBind(lifecycleOwner)
    }

    private suspend fun performBind(lifecycleOwner: LifecycleOwner) {
        val provider = try {
            awaitCameraProvider(context)
        } catch (e: Exception) {
            error = VideoRecorderError.START_FAILED
            return
        }

        val preview = CameraXPreview.Builder().build()
        val newRecorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD, FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)))
            .build()
        val videoCapture = VideoCapture.withOutput(newRecorder)

        val cameraSelector = if (provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        try {
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
        } catch (e: Exception) {
            error = VideoRecorderError.START_FAILED
            return
        }

        cameraProvider = provider
        recorder = newRecorder
        previewUseCase = preview
    }

    fun startRecording() {
        if (state !is VideoRecorderState.Idle) return
        val currentRecorder = recorder
        if (currentRecorder == null) {
            error = VideoRecorderError.START_FAILED
            return
        }
        error = null
        val file = createVideoOutputFile(context)
        if (file == null) {
            error = VideoRecorderError.START_FAILED
            return
        }

        val recording = try {
            currentRecorder.prepareRecording(context, FileOutputOptions.Builder(file).build())
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(context)) { event ->
                    if (event is VideoRecordEvent.Finalize) {
                        handleFinalize(event, file)
                    }
                }
        } catch (e: Exception) {
            file.delete()
            error = VideoRecorderError.START_FAILED
            return
        }

        activeRecording = recording
        outputFile = file
        recordingStartMs = System.currentTimeMillis()
        state = VideoRecorderState.Recording(0L)
        timerJob = scope.launch {
            while (isActive) {
                delay(TIMER_TICK_MS)
                state = VideoRecorderState.Recording(System.currentTimeMillis() - recordingStartMs)
            }
        }
    }

    /**
     * Stops the in-flight recording. The state moves to [VideoRecorderState.Processing] immediately
     * (a brief, transitional "waiting on CameraX" window with no cancel affordance) and then to
     * [VideoRecorderState.Idle] — attaching the clip via [onClipReady], or surfacing an error —
     * once CameraX's `Finalize` event arrives via [handleFinalize].
     */
    fun stopRecording() {
        val recording = activeRecording ?: return
        timerJob?.cancel()
        timerJob = null
        val elapsedMs = System.currentTimeMillis() - recordingStartMs
        activeRecording = null
        state = VideoRecorderState.Processing(elapsedMs)
        recording.stop()
    }

    /**
     * Handles CameraX's `Finalize` event — the *only* place a recording's outcome is known. This
     * can arrive for reasons other than an explicit [stopRecording] call: CameraX unbinds
     * [VideoCapture] (and force-finalizes any in-flight recording, with an error) when the host
     * lifecycle hits `ON_STOP` — e.g. the user backgrounds the app mid-record — and it can also
     * finalize with an error on disk-full/source-error conditions. Whatever the trigger, the timer
     * and the dangling [activeRecording] reference must always be torn down here; otherwise the
     * still-running [timerJob] keeps overwriting [state] back to `Recording` on its next tick,
     * stranding the user in a take that can never be stopped/confirmed again.
     *
     * A `Finalize` that arrives after [release] already ran (the user left the recorder while this
     * was still in flight) must not resurrect [state]/[error] on the torn-down holder — but [file]
     * is still an orphaned temp file at that point and must be deleted regardless.
     */
    private fun handleFinalize(event: VideoRecordEvent.Finalize, file: File) {
        timerJob?.cancel()
        timerJob = null
        activeRecording = null
        if (isReleased) {
            file.delete()
            return
        }

        val elapsedMs = System.currentTimeMillis() - recordingStartMs
        if (isVideoRecordingUsable(file.length(), elapsedMs, finalizeSucceeded = !event.hasError())) {
            outputFile = null
            state = VideoRecorderState.Idle
            onClipReady(buildConfirmedVideoClip(file))
        } else {
            file.delete()
            outputFile = null
            error = VideoRecorderError.EMPTY_RECORDING
            state = VideoRecorderState.Idle
        }
    }

    /** Stops/unbinds everything (via [release]) and deletes the pending temp file, if any. Call
     * when the user cancels an active take, or when this leaves composition (see
     * [rememberVideoRecorderHolder]) — either way, anything under [outputFile] at that point is an
     * orphaned capture that was never attached. Unlike the audio recorder, a video take also owns
     * the camera binding, so full teardown runs first. */
    fun discard() {
        val fileToDelete = outputFile
        release()
        fileToDelete?.delete()
        outputFile = null
        state = VideoRecorderState.Idle
        error = null
    }

    /** Unbinds the camera and finalizes any active recording without keeping the output file. Call on dispose. */
    fun release() {
        isReleased = true
        timerJob?.cancel()
        timerJob = null
        try {
            activeRecording?.stop()
        } catch (e: Exception) {
            // Already stopped/invalid state — nothing further to clean up.
        }
        activeRecording = null
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            // Already unbound/invalid state — nothing further to clean up.
        }
        cameraProvider = null
        recorder = null
        previewUseCase = null
    }

    /**
     * True while a take is in progress or being finalized — i.e. there's something a submission-type
     * switch would silently discard, and the window during which orientation should be locked (see
     * [LockOrientationWhileActive]). Mirrors [AudioRecorderHolder.hasUnconfirmedTake]; drives the
     * type-selector lock in [MediaSubmissionContent] via [InlineVideoRecorder]'s
     * `onUnconfirmedTakeChanged` reporting. At idle camera preview this is `false` — switching type
     * away just tears down the camera, there's no captured data to lose.
     */
    val hasUnconfirmedTake: Boolean
        get() = state is VideoRecorderState.Recording || state is VideoRecorderState.Processing
}

/**
 * Remembers a [VideoRecorderHolder], binds the camera, keeps [VideoRecorderHolder.onClipReady]
 * current every recomposition (so a freshly-recomposed [onClipReady] closure is always the one
 * invoked, even though the holder itself is created once), and discards it (unbinding the camera
 * and deleting any orphaned temp file) when this leaves composition.
 */
@Composable
internal fun rememberVideoRecorderHolder(onClipReady: (FileSubmitObject) -> Unit): VideoRecorderHolder {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val holder = remember { VideoRecorderHolder(context, scope) }
    SideEffect { holder.onClipReady = onClipReady }
    LaunchedEffect(holder) {
        holder.bindCamera(lifecycleOwner)
    }
    DisposableEffect(holder) {
        onDispose { holder.discard() }
    }
    return holder
}

/**
 * Pure-ish state+lambdas presentation of the in-app video recorder: a live camera preview,
 * Record at [VideoRecorderState.Idle], a running `mm:ss` timer with Stop/Cancel at
 * [VideoRecorderState.Recording], and a brief "processing" indicator at
 * [VideoRecorderState.Processing] while CameraX finalizes the file. [previewUseCase] is threaded
 * through separately from [state]/[error] because CameraX's `Preview` use case owns a native
 * surface that [VideoRecorderHolder] can't express as plain state; when null (camera still
 * binding, or binding failed) a placeholder is shown instead of the live feed.
 *
 * Hosted inline in the media main-pane body (see [MediaSubmissionContent]) exactly like
 * [AudioRecorderContent], so the non-modal "additional info" supporting pane stays visible and
 * usable while recording. Stopping auto-attaches the take once CameraX's `Finalize` event confirms
 * it's usable (see [VideoRecorderHolder.handleFinalize]) — there is no separate confirm/review step.
 */
@Composable
internal fun VideoRecorderContent(
    state: VideoRecorderState,
    error: VideoRecorderError?,
    previewUseCase: CameraXPreview?,
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
            text = stringResource(R.string.ngc_submission_videoRecorderTitle),
            level = HeadingLevel.H3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
        )

        if (previewUseCase != null) {
            AndroidView(
                // Set once here, at creation, rather than in `update` — `update` re-runs on every
                // recomposition (the elapsed-time timer recomposes this ~5x/sec while recording),
                // and re-invoking `setSurfaceProvider` resets the Preview surface each time,
                // flickering the live feed. `previewUseCase` is stable for the life of this
                // Composable (bound once in `bindCamera`), so capturing it here is safe.
                factory = { context ->
                    PreviewView(context).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        previewUseCase.setSurfaceProvider(surfaceProvider)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CAMERA_PREVIEW_HEIGHT)
                    .clip(RoundedCornerShape(InstUILayoutSizes.BorderRadius.Container.sm))
                    .padding(bottom = InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
            )
        } else {
            // Camera still binding (or binding failed, in which case `error` is also set below).
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CAMERA_PREVIEW_HEIGHT)
                    .clip(RoundedCornerShape(InstUILayoutSizes.BorderRadius.Container.sm))
                    .background(InstUISemanticColors.Background.muted())
                    .padding(bottom = InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
            )
        }

        val elapsedMs = when (state) {
            is VideoRecorderState.Recording -> state.elapsedMs
            is VideoRecorderState.Processing -> state.elapsedMs
            VideoRecorderState.Idle -> 0L
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
            VideoRecorderError.EMPTY_RECORDING -> stringResource(R.string.ngc_submission_audioRecorderEmptyRecording)
            VideoRecorderError.START_FAILED -> stringResource(R.string.ngc_submission_audioRecorderStartFailed)
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
            VideoRecorderState.Idle -> {
                Button(
                    text = stringResource(R.string.ngc_submission_audioRecorderRecord),
                    onClick = onStart,
                    icon = InstUIIcons.video,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            is VideoRecorderState.Recording -> {
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
            is VideoRecorderState.Processing -> {
                Button(
                    text = stringResource(R.string.ngc_submission_videoRecorderProcessing),
                    onClick = {},
                    enabled = false,
                    loading = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
