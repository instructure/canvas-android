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
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.instructure.student.util.BaseAppManager

open class TestAppManager : BaseAppManager() {

    var workerFactory: WorkerFactory? = null

    override fun onCreate() {
        super.onCreate()
        initWorkManagerForTesting(this)
    }

    override fun getWorkManagerFactory(): WorkerFactory {
        return workerFactory ?: WorkerFactory.getDefaultWorkerFactory()
    }

    fun initWorkManagerForTesting(context: Context) {
        val config = Configuration.Builder()
            .setExecutor(SynchronousExecutor())
            .setWorkerFactory(workerFactory ?: WorkerFactory.getDefaultWorkerFactory())
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
        
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        Log.d("TestAppManager", "WorkManager initialized for testing with HiltWorkerFactory and SynchronousExecutor.")
    }
}
