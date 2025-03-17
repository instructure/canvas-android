/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.util

import androidx.hilt.work.HiltWorkerFactory
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.parentapp.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import sdk.pendo.io.Pendo
import javax.inject.Inject


@HiltAndroidApp
class AppManager : BaseAppManager() {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var flutterAppMigration: FlutterAppMigration

    override fun onCreate() {
        super.onCreate()
        initPendo()
    }

    override fun performLogoutOnAuthError() {
        ParentLogoutTask(LogoutTask.Type.LOGOUT, null, getScheduler()).execute()
    }

    override fun getWorkManagerFactory() = workerFactory

    override fun getScheduler(): AlarmScheduler {
        return alarmScheduler
    }

    override fun performFlutterAppMigration() {
        flutterAppMigration.migrateIfNecessary()
    }

    private fun initPendo() {
        val options = Pendo.PendoOptions.Builder().setJetpackComposeBeta(true).build()
        Pendo.setup(this, BuildConfig.PENDO_TOKEN, options, null)
    }
}