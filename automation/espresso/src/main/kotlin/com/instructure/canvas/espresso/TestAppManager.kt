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

    private val delegatingFactory = DelegatingWorkerFactory()
    var workManagerInitialized = false
        private set
    var testDriver: TestDriver? = null

    @SuppressLint("RestrictedApi")
    override fun onCreate() {
        super.onCreate()
        RemoteConfigUtils.initialize()
        Log.d("WorkManagerTest", "TestAppManager.onCreate() - Application@${System.identityHashCode(this)}")
    }

    override val workManagerConfiguration: Configuration
        get() {
            Log.d("WorkManagerTest", "workManagerConfiguration accessed - Application@${System.identityHashCode(this)}, using DelegatingWorkerFactory")
            return Configuration.Builder()
                .setWorkerFactory(delegatingFactory)
                .setMinimumLoggingLevel(Log.DEBUG)
                .setExecutor(Executors.newSingleThreadExecutor())
                .build()
        }

    override fun getWorkManagerFactory(): WorkerFactory {
        Log.d("WorkManagerTest", "getWorkManagerFactory() - Application@${System.identityHashCode(this)}, returning DelegatingWorkerFactory")
        return delegatingFactory
    }

    fun setWorkerFactory(factory: WorkerFactory) {
        Log.d("WorkManagerTest", "setWorkerFactory() called with ${factory.javaClass.simpleName}")
        delegatingFactory.setDelegate(factory)
    }

    override fun performLogoutOnAuthError() = Unit

    @SuppressLint("RestrictedApi")
    fun initializeTestWorkManager(factory: WorkerFactory) {
        Log.d(
            "WorkManagerTest",
            "initializeTestWorkManager() called - factory=${factory.javaClass.simpleName}, workManagerInitialized=$workManagerInitialized"
        )

        if (!workManagerInitialized) {
            // First time: Initialize WorkManager with the delegating factory
            Log.d("WorkManagerTest", "Initializing WorkManager with DelegatingWorkerFactory")
            WorkManagerTestInitHelper.initializeTestWorkManager(this, workManagerConfiguration)
            testDriver = WorkManagerTestInitHelper.getTestDriver(this)
            workManagerInitialized = true
        }

        // Update the delegate to use the new factory
        delegatingFactory.setDelegate(factory)
        Log.d("WorkManagerTest", "WorkManager factory delegate updated to ${factory.javaClass.simpleName}")
    }
}
