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
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.DefaultWorkerFactory
import androidx.work.WorkerFactory
import androidx.work.testing.TestDriver
import androidx.work.testing.WorkManagerTestInitHelper
import com.instructure.canvasapi2.AppManager
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors

open class TestAppManager : AppManager() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerFactoryEntryPoint {
        fun hiltWorkerFactory(): HiltWorkerFactory
    }

    private var workerFactory: WorkerFactory? = null
    var workManagerInitialized = false
        private set
    var testDriver: TestDriver? = null

    @SuppressLint("RestrictedApi")
    override fun onCreate() {
        super.onCreate()
        RemoteConfigUtils.initialize()
        Log.d("WorkManagerTest", "TestAppManager.onCreate() - Application@${System.identityHashCode(this)}")

        // Get HiltWorkerFactory early via EntryPointAccessors so it's available before activities are created
        try {
            val entryPoint = EntryPoints.get(this, WorkerFactoryEntryPoint::class.java)
            val hiltFactory = entryPoint.hiltWorkerFactory()
            Log.d("WorkManagerTest", "TestAppManager.onCreate() - Retrieved HiltWorkerFactory@${System.identityHashCode(hiltFactory)} via EntryPoint")
            initializeTestWorkManager(hiltFactory)
        } catch (e: Exception) {
            Log.w("WorkManagerTest", "Failed to get HiltWorkerFactory in onCreate(), will retry in test setup", e)
        }
    }

    override val workManagerConfiguration: Configuration
        get() {
            val factory = workerFactory ?: DefaultWorkerFactory
            Log.d("WorkManagerTest", "workManagerConfiguration accessed - Application@${System.identityHashCode(this)}, workerFactory=${factory.javaClass.simpleName}@${System.identityHashCode(factory)}, isHilt=${workerFactory != null}")
            Log.d("WorkManagerTest", "Stack trace: ${Thread.currentThread().stackTrace.take(15).joinToString("\n")}")
            return Configuration.Builder()
                .setWorkerFactory(factory)
                .setMinimumLoggingLevel(Log.DEBUG)
                .setExecutor(Executors.newSingleThreadExecutor())
                .build()
        }

    override fun getWorkManagerFactory(): WorkerFactory {
        val factory = workerFactory ?: DefaultWorkerFactory
        Log.d("WorkManagerTest", "getWorkManagerFactory() - Application@${System.identityHashCode(this)}, returning ${factory.javaClass.simpleName}@${System.identityHashCode(factory)}, isHilt=${workerFactory != null}")
        return factory
    }

    override fun performLogoutOnAuthError() = Unit

    @SuppressLint("RestrictedApi")
    fun initializeTestWorkManager(factory: WorkerFactory) {
        Log.d("WorkManagerTest", "initializeTestWorkManager() called - Application@${System.identityHashCode(this)}, factory=${factory.javaClass.simpleName}@${System.identityHashCode(factory)}, workManagerInitialized=$workManagerInitialized")

        if (workManagerInitialized) {
            Log.d("WorkManagerTest", "Test WorkManager already initialized, skipping")
            return
        }

        workerFactory = factory
        Log.d("WorkManagerTest", "Set workerFactory to HiltWorkerFactory@${System.identityHashCode(factory)}")

        try {
            val config = Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .setExecutor(Executors.newSingleThreadExecutor())
                .setWorkerFactory(factory)
                .build()

            Log.d("WorkManagerTest", "Calling WorkManagerTestInitHelper.initializeTestWorkManager()")
            // WorkManagerTestInitHelper sets up a test delegate that overrides any existing initialization
            WorkManagerTestInitHelper.initializeTestWorkManager(this, config)
            testDriver = WorkManagerTestInitHelper.getTestDriver(this)
            workManagerInitialized = true
            Log.d("WorkManagerTest", "Test WorkManager initialized successfully - testDriver@${System.identityHashCode(testDriver)}")
        } catch (e: IllegalStateException) {
            Log.e("WorkManagerTest", "Failed to initialize WorkManager - already initialized with wrong factory?", e)
        }
    }
}
