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
package com.instructure.pandautils.features.file.upload.scanner

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class DocumentScanResult(
    val pdfUri: Uri?,
    val pageUris: List<Uri>
)

@Singleton
class DocumentScannerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun isDeviceSupported(): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalMemoryGB = memoryInfo.totalMem.toDouble() / (1024 * 1024 * 1024)
        return totalMemoryGB >= 1.7
    }

    fun getStartScanIntent(activity: Activity, pageLimit: Int = 50): Task<IntentSender> {
        val options = GmsDocumentScannerOptions.Builder()
            .setScannerMode(SCANNER_MODE_FULL)
            .setGalleryImportAllowed(true)
            .setPageLimit(pageLimit)
            .setResultFormats(RESULT_FORMAT_PDF, RESULT_FORMAT_JPEG)
            .build()
        return GmsDocumentScanning.getClient(options).getStartScanIntent(activity)
    }

    fun handleScanResultFromIntent(intent: Intent?): DocumentScanResult {
        val result = GmsDocumentScanningResult.fromActivityResultIntent(intent)
            ?: return DocumentScanResult(null, emptyList())
        val pdfUri = result.pdf?.uri
        val pageUris = result.pages?.map { it.imageUri } ?: emptyList()
        return DocumentScanResult(pdfUri, pageUris)
    }

    fun generateFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val timestamp = dateFormat.format(Date())
        return "Scanned_Document_$timestamp.pdf"
    }
}