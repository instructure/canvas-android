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
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.compose.runtime.MutableState
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Covers the pieces of the in-app video recorder that are practical to unit test without a real
 * camera: the empty/failed-recording guard (`isVideoRecordingUsable`, including the
 * `finalizeSucceeded` flag that forces an unusable result when CameraX's `VideoRecordEvent.Finalize`
 * itself reported an error), the `handleFinalize` timer/state guards and auto-attach path, and the
 * discard-releases-and-deletes bookkeeping. `formatElapsed` is shared with, and already covered
 * by, [AudioRecorderTest]. The actual camera bind round trip (`performBind`/`awaitCameraProvider`),
 * start/stop/finalize against a real `Recorder`/`VideoCapture`, the permission flow, the live
 * preview, and the orientation lock need real camera hardware/a Compose harness and are left to
 * manual/instrumented testing.
 */
class VideoRecorderTest {

    @Test
    fun `isVideoRecordingUsable rejects a zero-length file`() {
        assertFalse(isVideoRecordingUsable(fileLength = 0L, elapsedMs = 5_000L, finalizeSucceeded = true))
    }

    @Test
    fun `isVideoRecordingUsable rejects a recording stopped immediately after starting`() {
        assertFalse(isVideoRecordingUsable(fileLength = 1_024L, elapsedMs = 50L, finalizeSucceeded = true))
    }

    @Test
    fun `isVideoRecordingUsable accepts a non-empty file recorded past the minimum duration`() {
        assertTrue(isVideoRecordingUsable(fileLength = 1_024L, elapsedMs = 1_000L, finalizeSucceeded = true))
    }

    @Test
    fun `isVideoRecordingUsable rejects an empty file even if elapsed time is long enough`() {
        assertFalse(isVideoRecordingUsable(fileLength = 0L, elapsedMs = 5_000L, finalizeSucceeded = true))
    }

    @Test
    fun `isVideoRecordingUsable rejects a failed finalize even with a non-empty file past the minimum duration`() {
        assertFalse(isVideoRecordingUsable(fileLength = 1_024L, elapsedMs = 5_000L, finalizeSucceeded = false))
    }

    @Test
    fun `isVideoRecordingUsable rejects a failed finalize regardless of file length or elapsed time`() {
        assertFalse(isVideoRecordingUsable(fileLength = 0L, elapsedMs = 0L, finalizeSucceeded = false))
    }

    // VideoRecorderHolder.state has a private setter, and every transition away from Idle
    // (bindCamera/startRecording/stopRecording) drives a real ProcessCameraProvider/Recorder,
    // which isn't available in a plain JVM unit test. Only the Idle case is reachable here without
    // real camera hardware; Recording/Processing coverage for hasUnconfirmedTake is left to
    // manual/instrumented testing (except handleFinalize below, reached via reflection).
    @Test
    fun `hasUnconfirmedTake is false for the initial Idle state`() {
        val holder = VideoRecorderHolder(mockk<Context>(relaxed = true), CoroutineScope(Dispatchers.Unconfined))
        assertFalse(holder.hasUnconfirmedTake)
    }

    // `handleFinalize` is private and every other path into it (`startRecording`/`stopRecording`)
    // drives a real CameraX `Recorder`, so this reaches it via reflection rather than the public
    // API — the one part of the fix (cancelling the timer + clearing `activeRecording` for a
    // *spontaneous* Finalize, e.g. CameraX unbinding `VideoCapture` on `ON_STOP` while
    // backgrounded) that isn't exercisable through `isVideoRecordingUsable` alone. CameraX's
    // `VideoRecordEvent.Finalize` is a final class but mockk (1.14.x, JVM inline mocking) can still
    // mock it here.
    @Test
    fun `an errored finalize during recording clears the timer and active recording instead of stranding Recording`() {
        val holder = VideoRecorderHolder(mockk<Context>(relaxed = true), CoroutineScope(Dispatchers.Unconfined))
        val timerJob = Job()
        setPrivateField(holder, "timerJob", timerJob)
        setPrivateField(holder, "activeRecording", mockk<Recording>(relaxed = true))
        setPrivateField(holder, "recordingStartMs", System.currentTimeMillis() - 5_000L)

        val file = File.createTempFile("vid_test", ".mp4").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val erroredFinalize = mockk<VideoRecordEvent.Finalize>(relaxed = true) {
            every { hasError() } returns true
        }

        invokeHandleFinalize(holder, erroredFinalize, file)

        assertEquals(VideoRecorderState.Idle, holder.state)
        assertEquals(VideoRecorderError.EMPTY_RECORDING, holder.error)
        assertFalse(holder.hasUnconfirmedTake)
        assertTrue(timerJob.isCancelled)
        assertNull(getPrivateField(holder, "timerJob"))
        assertNull(getPrivateField(holder, "activeRecording"))
        assertFalse(file.exists())
    }

    // Guards the other half of the fix: a Finalize that arrives asynchronously after the user has
    // already left the recorder (`release()` already ran) must not resurrect/overwrite `state` on
    // the torn-down holder — but the temp file it was writing to is now orphaned and must still be
    // deleted (the fix-#3 leak this test guards: `if (isReleased) return` alone left it behind).
    @Test
    fun `a finalize that arrives after release does not overwrite state but still deletes the orphaned file`() {
        val holder = VideoRecorderHolder(mockk<Context>(relaxed = true), CoroutineScope(Dispatchers.Unconfined))
        setPrivateField(holder, "recordingStartMs", System.currentTimeMillis() - 5_000L)
        setState(holder, VideoRecorderState.Recording(5_000L))

        holder.release()

        val file = File.createTempFile("vid_test", ".mp4").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val successfulFinalize = mockk<VideoRecordEvent.Finalize>(relaxed = true) {
            every { hasError() } returns false
        }

        invokeHandleFinalize(holder, successfulFinalize, file)

        assertEquals(VideoRecorderState.Recording(5_000L), holder.state)
        assertFalse(file.exists())
    }

    // Covers the auto-attach path: a successful Finalize renames the file, invokes onClipReady
    // with the attach-ready clip, and returns to Idle — no separate confirm step.
    @Test
    fun `a successful finalize renames the file, invokes onClipReady, and returns to Idle`() {
        val holder = VideoRecorderHolder(mockk<Context>(relaxed = true), CoroutineScope(Dispatchers.Unconfined))
        var attachedClip: FileSubmitObject? = null
        holder.onClipReady = { attachedClip = it }
        setPrivateField(holder, "recordingStartMs", System.currentTimeMillis() - 5_000L)
        setState(holder, VideoRecorderState.Processing(elapsedMs = 5_000L))

        val original = File.createTempFile("vid_test", ".mp4").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val originalPath = original.absolutePath
        val successfulFinalize = mockk<VideoRecordEvent.Finalize>(relaxed = true) {
            every { hasError() } returns false
        }

        invokeHandleFinalize(holder, successfulFinalize, original)

        assertEquals(VideoRecorderState.Idle, holder.state)
        val clip = attachedClip
        assertTrue(clip != null)
        assertNotEquals(originalPath, clip!!.fullPath)
        assertTrue(File(clip.fullPath).exists())
        assertFalse(File(originalPath).exists())
        assertEquals("video/mp4", clip.contentType)
        File(clip.fullPath).delete()
    }

    @Test
    fun `renameToConfirmedVideoClipFile renames to a distinct path in the same directory`() {
        val original = File.createTempFile("vid_test", ".mp4").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        val renamed = renameToConfirmedVideoClipFile(original)

        assertNotEquals(original.absolutePath, renamed.absolutePath)
        assertEquals(original.parentFile, renamed.parentFile)
        assertTrue(renamed.exists())
        assertFalse(original.exists())
        renamed.delete()
    }

    @Test
    fun `renameToConfirmedVideoClipFile falls back to the original file when the rename fails`() {
        val original = File.createTempFile("vid_test", ".mp4").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        val result = renameToConfirmedVideoClipFile(original, rename = { _, _ -> false })

        assertEquals(original.absolutePath, result.absolutePath)
        assertTrue(result.exists())
        original.delete()
    }

    @Test
    fun `buildConfirmedVideoClip renames the recorded file and returns a FileSubmitObject pointing at the new path`() {
        val original = File.createTempFile("vid_test", ".mp4").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val originalPath = original.absolutePath

        val clip = buildConfirmedVideoClip(original)

        assertNotEquals(originalPath, clip.fullPath)
        assertTrue(File(clip.fullPath).exists())
        assertFalse(File(originalPath).exists())
        File(clip.fullPath).delete()
    }

    // Guards the temp-file leak fix (and the camera-lifecycle half of it): discard() must fully
    // release the camera/recorder (unlike the audio recorder, a video take owns a camera binding)
    // *and* delete any pending (never-attached) temp file, so rememberVideoRecorderHolder's
    // onDispose (which now calls discard() instead of release()) doesn't leave the camera bound or
    // orphan a cache file.
    @Test
    fun `discard releases the camera and deletes the pending temp file`() {
        val holder = VideoRecorderHolder(mockk<Context>(relaxed = true), CoroutineScope(Dispatchers.Unconfined))
        val file = File.createTempFile("vid_test", ".mp4").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        setPrivateField(holder, "outputFile", file)
        setPrivateField(holder, "cameraProvider", mockk<ProcessCameraProvider>(relaxed = true))
        setState(holder, VideoRecorderState.Recording(1_000L))

        holder.discard()

        assertEquals(VideoRecorderState.Idle, holder.state)
        assertFalse(file.exists())
        assertNull(getPrivateField(holder, "cameraProvider"))
        assertTrue(getPrivateField(holder, "isReleased") as Boolean)
    }

    private fun setPrivateField(holder: VideoRecorderHolder, name: String, value: Any?) {
        val field = VideoRecorderHolder::class.java.getDeclaredField(name)
        field.isAccessible = true
        field.set(holder, value)
    }

    private fun getPrivateField(holder: VideoRecorderHolder, name: String): Any? {
        val field = VideoRecorderHolder::class.java.getDeclaredField(name)
        field.isAccessible = true
        return field.get(holder)
    }

    @Suppress("UNCHECKED_CAST")
    private fun setState(holder: VideoRecorderHolder, newState: VideoRecorderState) {
        val field = VideoRecorderHolder::class.java.getDeclaredField("state\$delegate")
        field.isAccessible = true
        (field.get(holder) as MutableState<VideoRecorderState>).value = newState
    }

    private fun invokeHandleFinalize(holder: VideoRecorderHolder, event: VideoRecordEvent.Finalize, file: File) {
        val method = VideoRecorderHolder::class.java.getDeclaredMethod(
            "handleFinalize",
            VideoRecordEvent.Finalize::class.java,
            File::class.java,
        )
        method.isAccessible = true
        method.invoke(holder, event, file)
    }
}
