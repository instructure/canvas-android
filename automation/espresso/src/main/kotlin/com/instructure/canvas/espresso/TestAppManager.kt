/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */package com.instructure.canvas.espresso

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import com.instructure.canvasapi2.AppManager
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import java.util.concurrent.Executors

open class TestAppManager: AppManager() {

    override fun onCreate() {
        super.onCreate()
        RemoteConfigUtils.initialize()
        initWorkManager(this)
    }

    var workerFactory: WorkerFactory? = null
    override fun getWorkManagerFactory(): WorkerFactory {
        return workerFactory ?: WorkerFactory.getDefaultWorkerFactory()
    }

    override fun performLogoutOnAuthError() = Unit

    fun initWorkManager(context: Context) {
        try {
            val config = Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .setExecutor(Executors.newSingleThreadExecutor())
                .setWorkerFactory(getWorkManagerFactory())
                .build()
            WorkManager.initialize(context, config)
        } catch (e: IllegalStateException) {
            Log.w("TestAppManager", "WorkManager.initialize() failed, likely already initialized: ${e.message}")
        }
    }
}
