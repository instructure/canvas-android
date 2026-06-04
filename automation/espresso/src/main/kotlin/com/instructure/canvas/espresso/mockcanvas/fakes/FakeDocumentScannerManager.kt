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
package com.instructure.canvas.espresso.mockcanvas.fakes

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


class FakeDocumentScannerManager(
    private val requestCode: Int = REQUEST_DOCUMENT_SCANNING
) : DocumentScannerManager {

    var scannerSupported: Boolean = true
    var scanResultUri: Uri? = null

    override fun isDeviceSupported(): Boolean = scannerSupported

    override fun getStartScanIntent(activity: Activity, pageLimit: Int): Task<IntentSender> {
        val fakeResultIntent = Intent()
        OnActivityResults(ActivityResult(requestCode, Activity.RESULT_OK, fakeResultIntent)).postSticky()
        return TaskCompletionSource<IntentSender>().task
    }

    override fun handleScanResultFromIntent(intent: Intent?): DocumentScanResult {
        return DocumentScanResult(scanResultUri, emptyList())
    }

    override fun generateFileName(): String = "Scanned_Document_test.pdf"

    companion object {
        const val REQUEST_DOCUMENT_SCANNING = 5103 //Matches PickerSubmissionUploadEffectHandler.REQUEST_DOCUMENT_SCANNING
    }
}