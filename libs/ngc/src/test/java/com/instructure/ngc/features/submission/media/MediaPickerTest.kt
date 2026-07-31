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

import android.Manifest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Covers the pure permission-gating decision extracted out of `rememberMediaPicker`'s
 * `requestAudioPermission`/`requestVideoPermission` closures (self-permission short-circuit vs.
 * which permissions to request). The surrounding launcher wiring, denied-flag bookkeeping, and
 * pending-grant-callback state remain in the `@Composable` and are left to manual/instrumented
 * testing since they require a real permission-launcher harness.
 */
class MediaPickerTest {

    @Test
    fun `decideAudioPermissionRequest - proceeds when audio already granted`() {
        assertEquals(MediaPermissionDecision.Proceed, decideAudioPermissionRequest(isAudioGranted = true))
    }

    @Test
    fun `decideAudioPermissionRequest - requests RECORD_AUDIO when not granted`() {
        assertEquals(
            MediaPermissionDecision.RequestPermissions(listOf(Manifest.permission.RECORD_AUDIO)),
            decideAudioPermissionRequest(isAudioGranted = false),
        )
    }

    @Test
    fun `decideVideoPermissionRequest - proceeds when camera and audio both granted`() {
        assertEquals(
            MediaPermissionDecision.Proceed,
            decideVideoPermissionRequest(isCameraGranted = true, isAudioGranted = true),
        )
    }

    @Test
    fun `decideVideoPermissionRequest - requests both permissions when only camera granted`() {
        assertEquals(
            MediaPermissionDecision.RequestPermissions(
                listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            ),
            decideVideoPermissionRequest(isCameraGranted = true, isAudioGranted = false),
        )
    }

    @Test
    fun `decideVideoPermissionRequest - requests both permissions when only audio granted`() {
        assertEquals(
            MediaPermissionDecision.RequestPermissions(
                listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            ),
            decideVideoPermissionRequest(isCameraGranted = false, isAudioGranted = true),
        )
    }

    @Test
    fun `decideVideoPermissionRequest - requests both permissions when neither granted`() {
        assertEquals(
            MediaPermissionDecision.RequestPermissions(
                listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            ),
            decideVideoPermissionRequest(isCameraGranted = false, isAudioGranted = false),
        )
    }
}
