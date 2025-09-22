/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.student.espresso

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkerFactory
import androidx.work.testing.SynchronousExecutor // Import SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper // Import WorkManagerTestInitHelper
import com.instructure.student.util.BaseAppManager

open class TestAppManager : BaseAppManager() {

    var workerFactory: WorkerFactory? = null

    override fun onCreate() {
        super.onCreate()
        initWorkManagerForTesting(this) // Changed method name for clarity
    }

    override fun getWorkManagerFactory(): WorkerFactory {
        return workerFactory ?: WorkerFactory.getDefaultWorkerFactory()
    }

    // Updated to use WorkManagerTestInitHelper and SynchronousExecutor
    fun initWorkManagerForTesting(context: Context) {
        val config = Configuration.Builder()
            .setExecutor(SynchronousExecutor()) // Use SynchronousExecutor for immediate execution
            .setWorkerFactory(getWorkManagerFactory())
            .setMinimumLoggingLevel(Log.DEBUG) // Optional: for test logging
            .build()
        
        // Initialize WorkManager for testing using WorkManagerTestInitHelper
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        Log.d("TestAppManager", "WorkManager initialized for testing with SynchronousExecutor.")
    }
}
