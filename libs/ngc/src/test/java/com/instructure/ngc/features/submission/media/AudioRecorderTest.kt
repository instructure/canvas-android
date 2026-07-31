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
import androidx.compose.runtime.MutableState
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Covers the pieces of the in-app audio recorder that are practical to unit test without a full
 * MediaRecorder/MediaPlayer harness: the `mm:ss` timer formatter, the empty/failed-recording guard
 * (including the `stopSucceeded` flag that forces an unusable result when `MediaRecorder.stop()`
 * itself threw), and the stop-time rename/attach decision ([buildConfirmedClip]) that
 * [AudioRecorderHolder.stopRecording] uses to auto-attach a take. Everything that touches
 * MediaRecorder directly (start/stop/prepare/release, the actual permission flow, and the
 * Composable UI) is left to manual/instrumented testing.
 */
class AudioRecorderTest {

    @Test
    fun `formatElapsed formats zero as 00_00`() {
        assertEquals("00:00", formatElapsed(0L))
    }

    @Test
    fun `formatElapsed rounds down to whole seconds`() {
        assertEquals("00:01", formatElapsed(1_999L))
    }

    @Test
    fun `formatElapsed formats minutes and seconds`() {
        assertEquals("02:05", formatElapsed(125_000L))
    }

    @Test
    fun `formatElapsed pads minutes and seconds beyond a single digit`() {
        assertEquals("11:07", formatElapsed(667_000L))
    }

    @Test
    fun `formatElapsed clamps negative durations to zero`() {
        assertEquals("00:00", formatElapsed(-500L))
    }

    @Test
    fun `isRecordingUsable rejects a zero-length file`() {
        assertFalse(isRecordingUsable(fileLength = 0L, elapsedMs = 5_000L, stopSucceeded = true))
    }

    @Test
    fun `isRecordingUsable rejects a recording stopped immediately after starting`() {
        assertFalse(isRecordingUsable(fileLength = 1_024L, elapsedMs = 50L, stopSucceeded = true))
    }

    @Test
    fun `isRecordingUsable accepts a non-empty file recorded past the minimum duration`() {
        assertTrue(isRecordingUsable(fileLength = 1_024L, elapsedMs = 1_000L, stopSucceeded = true))
    }

    @Test
    fun `isRecordingUsable rejects an empty file even if elapsed time is long enough`() {
        assertFalse(isRecordingUsable(fileLength = 0L, elapsedMs = 5_000L, stopSucceeded = true))
    }

    @Test
    fun `isRecordingUsable rejects a failed stop even with a non-empty file past the minimum duration`() {
        assertFalse(isRecordingUsable(fileLength = 1_024L, elapsedMs = 5_000L, stopSucceeded = false))
    }

    @Test
    fun `isRecordingUsable rejects a failed stop regardless of file length or elapsed time`() {
        assertFalse(isRecordingUsable(fileLength = 0L, elapsedMs = 0L, stopSucceeded = false))
    }

    // AudioRecorderHolder.state has a private setter, and every transition away from Idle
    // (startRecording/stopRecording) drives a real MediaRecorder, which isn't available in a
    // plain JVM unit test. Only the Idle case is reachable here without a Robolectric/
    // instrumentation harness; Recording coverage for hasUnconfirmedTake is left to
    // manual/instrumented testing.
    @Test
    fun `hasUnconfirmedTake is false for the initial Idle state`() {
        val holder = AudioRecorderHolder(mockk<Context>(relaxed = true), CoroutineScope(Dispatchers.Unconfined))
        assertFalse(holder.hasUnconfirmedTake)
    }

    // Guards the startRecording guard alignment fix (matches VideoRecorderHolder's `state !is
    // Idle` phrasing): reachable via reflection since real recording requires a live MediaRecorder.
    @Test
    fun `startRecording no-ops while already recording`() {
        val holder = AudioRecorderHolder(mockk<Context>(relaxed = true), CoroutineScope(Dispatchers.Unconfined))
        setState(holder, AudioRecorderState.Recording(1_000L))

        holder.startRecording()

        assertEquals(AudioRecorderState.Recording(1_000L), holder.state)
    }

    @Test
    fun `renameToConfirmedClipFile renames to a distinct path in the same directory`() {
        val original = File.createTempFile("audio_test", ".m4a").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        val renamed = renameToConfirmedClipFile(original)

        assertNotEquals(original.absolutePath, renamed.absolutePath)
        assertEquals(original.parentFile, renamed.parentFile)
        assertTrue(renamed.exists())
        assertFalse(original.exists())
        renamed.delete()
    }

    @Test
    fun `renameToConfirmedClipFile falls back to the original file when the rename fails`() {
        val original = File.createTempFile("audio_test", ".m4a").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        val result = renameToConfirmedClipFile(original, rename = { _, _ -> false })

        assertEquals(original.absolutePath, result.absolutePath)
        assertTrue(result.exists())
        original.delete()
    }

    // Covers the auto-attach decision AudioRecorderHolder.stopRecording delegates to on a
    // successful stop: the file is renamed to its final path and wrapped as an attach-ready
    // FileSubmitObject, in one call, with no separate confirm step.
    @Test
    fun `buildConfirmedClip renames the recorded file and returns a FileSubmitObject pointing at the new path`() {
        val original = File.createTempFile("audio_test", ".m4a").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val originalPath = original.absolutePath

        val clip = buildConfirmedClip(original)

        assertNotEquals(originalPath, clip.fullPath)
        assertTrue(File(clip.fullPath).exists())
        assertFalse(File(originalPath).exists())
        assertEquals("audio/mp4", clip.contentType)
        File(clip.fullPath).delete()
    }

    // Guards the temp-file leak fix: discard() must both release the native recorder and delete
    // any pending (never-attached) temp file, so rememberAudioRecorderHolder's onDispose (which
    // now calls discard() instead of release()) doesn't orphan a cache file.
    @Test
    fun `discard deletes the pending temp file and returns to Idle`() {
        val holder = AudioRecorderHolder(mockk<Context>(relaxed = true), CoroutineScope(Dispatchers.Unconfined))
        val file = File.createTempFile("audio_test", ".m4a").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        setPrivateField(holder, "outputFile", file)
        setState(holder, AudioRecorderState.Recording(1_000L))

        holder.discard()

        assertEquals(AudioRecorderState.Idle, holder.state)
        assertFalse(file.exists())
    }

    private fun setPrivateField(holder: AudioRecorderHolder, name: String, value: Any?) {
        val field = AudioRecorderHolder::class.java.getDeclaredField(name)
        field.isAccessible = true
        field.set(holder, value)
    }

    @Suppress("UNCHECKED_CAST")
    private fun setState(holder: AudioRecorderHolder, newState: AudioRecorderState) {
        val field = AudioRecorderHolder::class.java.getDeclaredField("state\$delegate")
        field.isAccessible = true
        (field.get(holder) as MutableState<AudioRecorderState>).value = newState
    }
}
