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
package com.instructure.student.ui.utils

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.instructure.pandautils.features.file.upload.scanner.DocumentScanResult
import com.instructure.pandautils.features.file.upload.scanner.DocumentScannerManager
import com.instructure.pandautils.utils.ActivityResult
import com.instructure.pandautils.utils.OnActivityResults
import com.instructure.pandautils.utils.postSticky

/**
 * A fake [DocumentScannerManager] for use in instrumentation tests.
 *
 * By default [isDeviceSupported] returns `true` so the scanner button is shown. Set
 * [scannerSupported] to `false` before navigating to the screen under test to verify the scanner
 * button is hidden for unsupported devices.
 *
 * [scanResultUri] must be set to a valid [Uri] before the scanner button is tapped. The fake
 * intercepts the scanner launch and immediately posts an [OnActivityResults] EventBus event so the
 * effect handler treats the scan as complete without invoking the real ML Kit scanner.
 *
 * The [requestCode] must match the one used by the effect handler that processes the result
 * (e.g. [PickerSubmissionUploadEffectHandler.REQUEST_DOCUMENT_SCANNING] = 5103).
 */
class FakeDocumentScannerManager(
    private val requestCode: Int = REQUEST_DOCUMENT_SCANNING
) : DocumentScannerManager {

    var scannerSupported: Boolean = true
    var scanResultUri: Uri? = null

    override fun isDeviceSupported(): Boolean = scannerSupported

    override fun getStartScanIntent(activity: Activity, pageLimit: Int): Task<IntentSender> {
        // Post a fake activity result so the effect handler treats the scan as complete.
        // Using a non-completing TaskCompletionSource ensures startIntentSenderForResult is never
        // called, avoiding any interaction with the real ML Kit scanner.
        val fakeResultIntent = Intent()
        OnActivityResults(ActivityResult(requestCode, Activity.RESULT_OK, fakeResultIntent)).postSticky()
        return TaskCompletionSource<IntentSender>().task
    }

    override fun handleScanResultFromIntent(intent: Intent?): DocumentScanResult {
        return DocumentScanResult(scanResultUri, emptyList())
    }

    override fun generateFileName(): String = "Scanned_Document_test.pdf"

    companion object {
        // Matches PickerSubmissionUploadEffectHandler.REQUEST_DOCUMENT_SCANNING
        const val REQUEST_DOCUMENT_SCANNING = 5103
    }
}