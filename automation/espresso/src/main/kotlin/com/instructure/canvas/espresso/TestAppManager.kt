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

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkerFactory
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestDriver
import androidx.work.testing.WorkManagerTestInitHelper
import com.instructure.canvasapi2.AppManager
import com.instructure.canvasapi2.utils.RemoteConfigUtils

open class TestAppManager: AppManager() {

    @SuppressLint("RestrictedApi")
    override fun onCreate() {
        super.onCreate()
        RemoteConfigUtils.initialize()

        if (workerFactory == null) {
            workerFactory = WorkerFactory.getDefaultWorkerFactory()
        }
    }

    var testDriver: TestDriver? = null

    var workerFactory: WorkerFactory? = null
    @SuppressLint("RestrictedApi")
    override fun getWorkManagerFactory(): WorkerFactory {
        return workerFactory ?: WorkerFactory.getDefaultWorkerFactory()
    }

    // CRITICAL: Prevent automatic WorkManager initialization via Configuration.Provider
    // We want manual control in tests
    override val workManagerConfiguration: Configuration
        get() {
            Log.w("TestAppManager", "workManagerConfiguration getter called - WorkManager trying to auto-initialize!")
            throw IllegalStateException("WorkManager should be manually initialized in tests, not auto-initialized")
        }

    override fun performLogoutOnAuthError() = Unit

    fun initWorkManager(context: Context) {
        val factoryBeforeInit = this.workerFactory
        Log.d("WorkManagerDebug", "initWorkManager called")
        Log.d("WorkManagerDebug", "  this (TestAppManager): ${this.hashCode()}")
        Log.d("WorkManagerDebug", "  this.workerFactory: ${factoryBeforeInit.hashCode()}")

        // Check if WorkManager already exists - if so, just reuse it
        var workManagerExists = false
        try {
            val existing = androidx.work.WorkManager.getInstance(context)
            Log.d("WorkManagerDebug", "  WorkManager ALREADY EXISTS: ${existing.hashCode()}")
            workManagerExists = true
            testDriver = WorkManagerTestInitHelper.getTestDriver(context)
            Log.d("WorkManagerDebug", "  Reusing existing WorkManager and testDriver")
        } catch (e: IllegalStateException) {
            Log.d("WorkManagerDebug", "  WorkManager does not exist yet, will initialize")
        }

        // Only initialize if WorkManager doesn't exist yet
        if (!workManagerExists) {
            try {
                val factory = this.getWorkManagerFactory()
                Log.d("WorkManagerDebug", "  getWorkManagerFactory() returned: ${factory.hashCode()}")

                val config = Configuration.Builder()
                    .setMinimumLoggingLevel(Log.DEBUG)
                    .setExecutor(SynchronousExecutor())
                    .setWorkerFactory(factory)
                    .build()

                Log.d("WorkManagerDebug", "  Calling initializeTestWorkManager...")
                WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
                Log.d("WorkManagerDebug", "  initializeTestWorkManager succeeded (first time)")

                testDriver = WorkManagerTestInitHelper.getTestDriver(context)
                Log.d("WorkManagerDebug", "  testDriver: $testDriver")
            } catch (e: IllegalStateException) {
                Log.w("WorkManagerDebug", "WorkManager initialization failed: ${e.message}")
                testDriver = WorkManagerTestInitHelper.getTestDriver(context)
            }
        }

        // Cancel and prune all existing work to ensure clean state for this test
        try {
            val workManager = androidx.work.WorkManager.getInstance(context)
            Log.d("WorkManagerDebug", "  WorkManager instance for cleanup: ${workManager.hashCode()}")
            workManager.cancelAllWork().result.get() // Wait for cancellation
            workManager.pruneWork().result.get() // Wait for pruning
            Log.d("WorkManagerDebug", "  Cancelled and pruned all existing work")
        } catch (e: Exception) {
            Log.w("WorkManagerDebug", "  Error cancelling/pruning work: ${e.message}")
        }
    }
}
