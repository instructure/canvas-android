/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.student.util

import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import android.util.Log
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.instructure.canvasapi2.utils.MasqueradeHelper
import com.instructure.canvasapi2.utils.PendoInitCallbackHandler
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.analytics.pageview.PageViewUploadWorker
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.pandautils.room.offline.DatabaseProvider
import com.instructure.pandautils.typeface.TypefaceBehavior
import com.instructure.student.BuildConfig
import com.instructure.student.tasks.StudentLogoutTask
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import sdk.pendo.io.Pendo
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class AppManager : BaseAppManager() {

    @Inject
    lateinit var typefaceBehavior: TypefaceBehavior

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var databaseProvider: DatabaseProvider

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var workManager: WorkManager

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        MasqueradeHelper.masqueradeLogoutTask = Runnable {
            StudentLogoutTask(
                LogoutTask.Type.LOGOUT,
                typefaceBehavior = typefaceBehavior,
                databaseProvider = databaseProvider,
                alarmScheduler = alarmScheduler
            ).execute()
        }

        schedulePandataUpload()
        initPendo()
        warmUpGeminiNano()
    }

    private fun initPendo() {
        val options = Pendo.PendoOptions.Builder().setJetpackComposeBeta(true).build()
        Pendo.setup(this, BuildConfig.PENDO_TOKEN, options, PendoInitCallbackHandler)
    }

    private fun warmUpGeminiNano() {
        appScope.launch(Dispatchers.IO) {
            try {
                val model = Generation.getClient()
                val initialStatus = model.checkStatus()
                Log.d(NANO_TAG, "Initial status=$initialStatus (AVAILABLE=${FeatureStatus.AVAILABLE}, DOWNLOADABLE=${FeatureStatus.DOWNLOADABLE}, DOWNLOADING=${FeatureStatus.DOWNLOADING})")

                if (initialStatus == FeatureStatus.DOWNLOADABLE) {
                    Log.d(NANO_TAG, "Triggering download...")
                    model.download().collect { downloadStatus ->
                        when (downloadStatus) {
                            is com.google.mlkit.genai.common.DownloadStatus.DownloadStarted ->
                                Log.d(NANO_TAG, "Download started, total bytes: ${downloadStatus.bytesToDownload}")
                            is com.google.mlkit.genai.common.DownloadStatus.DownloadProgress ->
                                Log.d(NANO_TAG, "Download progress: ${downloadStatus.totalBytesDownloaded} bytes")
                            is com.google.mlkit.genai.common.DownloadStatus.DownloadCompleted ->
                                Log.d(NANO_TAG, "Download completed")
                            is com.google.mlkit.genai.common.DownloadStatus.DownloadFailed ->
                                Log.e(NANO_TAG, "Download failed: ${downloadStatus.e.message}")
                        }
                    }
                }

                // Poll status until AVAILABLE or timeout (5 min)
                val pollStart = System.currentTimeMillis()
                val timeoutMs = 5 * 60 * 1000L
                var current = model.checkStatus()
                var pollCount = 0
                while (current != FeatureStatus.AVAILABLE && (System.currentTimeMillis() - pollStart) < timeoutMs) {
                    pollCount++
                    if (pollCount % 10 == 1) {
                        Log.d(NANO_TAG, "Waiting for AVAILABLE... status=$current, elapsed=${(System.currentTimeMillis() - pollStart) / 1000}s")
                    }
                    kotlinx.coroutines.delay(3000)
                    current = model.checkStatus()
                }

                if (current == FeatureStatus.AVAILABLE) {
                    Log.d(NANO_TAG, "Model AVAILABLE after ${(System.currentTimeMillis() - pollStart) / 1000}s, calling warmup...")
                    model.warmup()
                    Log.d(NANO_TAG, "Warmup complete")
                } else {
                    Log.w(NANO_TAG, "Timed out waiting for AVAILABLE, final status=$current")
                }
            } catch (e: Exception) {
                Log.e(NANO_TAG, "Failed: ${e.message}", e)
            }
        }
    }

    companion object {
        private const val NANO_TAG = "GeminiNanoWarmup"
    }

    override fun performLogoutOnAuthError() {
        StudentLogoutTask(
            LogoutTask.Type.LOGOUT,
            typefaceBehavior = typefaceBehavior,
            databaseProvider = databaseProvider,
            alarmScheduler = alarmScheduler
        ).execute()
    }

    override fun getWorkManagerFactory(): WorkerFactory = workerFactory

    private fun schedulePandataUpload() {
        val workRequest = PeriodicWorkRequestBuilder<PageViewUploadWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        workManager.enqueueUniquePeriodicWork("pageView-student", ExistingPeriodicWorkPolicy.KEEP, workRequest)
    }
}