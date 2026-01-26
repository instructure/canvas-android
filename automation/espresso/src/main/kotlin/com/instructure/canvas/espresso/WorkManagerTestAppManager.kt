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
import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkerFactory
import androidx.work.testing.TestDriver
import androidx.work.testing.WorkManagerTestInitHelper
import java.util.concurrent.Executors

/**
 * Interface for test application managers that provides WorkManager testing infrastructure.
 */
interface WorkManagerTestAppManager {
    val workManagerTestHelper: WorkManagerTestHelper

    val testDriver: TestDriver?
        get() = workManagerTestHelper.testDriver

    fun initializeTestWorkManager(factory: WorkerFactory, application: Application) {
        workManagerTestHelper.initializeTestWorkManager(factory, application)
    }
}

/**
 * Helper class that encapsulates WorkManager testing logic.
 * Use via composition in test application managers.
 */
class WorkManagerTestHelper {
    private val delegatingFactory = DelegatingWorkerFactory()
    private var workManagerInitialized = false
    var testDriver: TestDriver? = null
        private set

    val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(delegatingFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(Executors.newSingleThreadExecutor())
            .build()

    fun getWorkManagerFactory(): WorkerFactory = delegatingFactory

    @SuppressLint("RestrictedApi")
    fun initializeTestWorkManager(factory: WorkerFactory, application: Application) {
        if (!workManagerInitialized) {
            WorkManagerTestInitHelper.initializeTestWorkManager(application, workManagerConfiguration)
            testDriver = WorkManagerTestInitHelper.getTestDriver(application)
            workManagerInitialized = true
        }

        // Update the delegate to use the provided factory
        delegatingFactory.setDelegate(factory)
    }
}
