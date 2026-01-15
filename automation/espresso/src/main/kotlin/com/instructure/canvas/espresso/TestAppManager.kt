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
 */
package com.instructure.canvas.espresso

import android.annotation.SuppressLint
import android.util.Log
import androidx.work.Configuration
import androidx.work.DefaultWorkerFactory
import androidx.work.WorkerFactory
import androidx.work.testing.TestDriver
import androidx.work.testing.WorkManagerTestInitHelper
import com.instructure.canvasapi2.AppManager
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import java.util.concurrent.Executors

open class TestAppManager : AppManager() {

    var workerFactory: WorkerFactory? = null
    var workManagerInitialized = false
    var testDriver: TestDriver? = null

    @SuppressLint("RestrictedApi")
    override fun onCreate() {
        super.onCreate()
        RemoteConfigUtils.initialize()
    }

    override fun getWorkManagerFactory(): WorkerFactory {
        return workerFactory ?: DefaultWorkerFactory
    }

    override fun performLogoutOnAuthError() = Unit

    @SuppressLint("RestrictedApi")
    fun initializeTestWorkManager() {
        if (workManagerInitialized) return

        try {
            val config = Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .setExecutor(Executors.newSingleThreadExecutor())
                .setWorkerFactory(getWorkManagerFactory())
                .build()

            WorkManagerTestInitHelper.initializeTestWorkManager(this, config)
            testDriver = WorkManagerTestInitHelper.getTestDriver(this)
            workManagerInitialized = true
        } catch (e: IllegalStateException) {
            Log.w("TestAppManager", e)
        }
    }
}
