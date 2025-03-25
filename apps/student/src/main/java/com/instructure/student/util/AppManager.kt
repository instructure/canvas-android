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
import com.instructure.canvasapi2.utils.MasqueradeHelper
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.analytics.pageview.PageViewUploadWorker
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.pandautils.room.offline.DatabaseProvider
import com.instructure.pandautils.typeface.TypefaceBehavior
import com.instructure.student.BuildConfig
import com.instructure.student.tasks.StudentLogoutTask
import dagger.hilt.android.HiltAndroidApp
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
    }

    private fun initPendo() {
        val options = Pendo.PendoOptions.Builder().setJetpackComposeBeta(true).build()
        Pendo.setup(this, BuildConfig.PENDO_TOKEN, options, null)
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
