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
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkerFactory
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestDriver
import androidx.work.testing.WorkManagerTestInitHelper
import com.instructure.canvasapi2.AppManager
import com.instructure.canvasapi2.utils.RemoteConfigUtils

open class TestAppManager: AppManager() {

    companion object {

        @JvmStatic
        fun detectE2ETest(): Boolean {
            return try {
                // Check instrumentation arguments for test class/package info
                val arguments = InstrumentationRegistry.getArguments()
                val testClass = arguments.getString("class")
                val testPackage = arguments.getString("package")

                Log.d("TestAppManager", "Detecting E2E test - class: $testClass, package: $testPackage")

                // Check if running specific E2E test class or package
                val isE2EClass = testClass?.contains(".e2e.") == true || testClass?.contains("E2ETest") == true
                val isE2EPackage = testPackage?.contains(".e2e") == true

                isE2EClass || isE2EPackage
            } catch (e: Exception) {
                Log.w("TestAppManager", "Failed to detect E2E test: ${e.message}")
                false
            }
        }
    }

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

    // Provide configuration for automatic WorkManager initialization
    // We hijack the auto-init by proactively initializing with TestDriver support
    override val workManagerConfiguration: Configuration
        get() {
            Log.d("TestAppManager", "workManagerConfiguration called - initializing with TestDriver support")

            val config = Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .setExecutor(SynchronousExecutor())
                .setWorkerFactory(getWorkManagerFactory())
                .build()

            try {
                // Proactively initialize WorkManager with TestDriver support
                //HA NEM JÓ AKKOR valahogy itt ezt kivinni ezen kívül hogy tuti egyszer legyen hívva!
                WorkManagerTestInitHelper.initializeTestWorkManager(applicationContext, config)
                testDriver = WorkManagerTestInitHelper.getTestDriver(applicationContext)
                Log.d("TestAppManager", "Successfully initialized WorkManager with TestDriver: $testDriver")
            } catch (e: IllegalStateException) {
                Log.w("TestAppManager", "WorkManager already initialized: ${e.message}")
                // Try to get TestDriver anyway in case it was test-initialized before
                try {
                    testDriver = WorkManagerTestInitHelper.getTestDriver(applicationContext)
                    Log.d("TestAppManager", "Retrieved existing TestDriver: $testDriver")
                } catch (e2: Exception) {
                    Log.w("TestAppManager", "Could not get TestDriver: ${e2.message}")
                }
            }

            return config
        }

    override fun performLogoutOnAuthError() = Unit

    fun initWorkManager(context: Context) {
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

               /* val config = Configuration.Builder()
                    .setMinimumLoggingLevel(Log.DEBUG)
                    .setExecutor(SynchronousExecutor())
                    .setWorkerFactory(factory)
                    .build()
                */
                Log.d("WorkManagerDebug", "  Calling initializeTestWorkManager...")
                WorkManagerTestInitHelper.initializeTestWorkManager(context, workManagerConfiguration)
                Log.d("WorkManagerDebug", "  initializeTestWorkManager succeeded (first time)")

                testDriver = WorkManagerTestInitHelper.getTestDriver(context)
                Log.d("WorkManagerDebug", "  testDriver: $testDriver")
            } catch (e: IllegalStateException) {
                Log.w("WorkManagerDebug", "WorkManager initialization failed: ${e.message}")
                testDriver = WorkManagerTestInitHelper.getTestDriver(context)
            }
        }

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
