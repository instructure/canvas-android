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

import androidx.work.DefaultWorkerFactory
import androidx.work.WorkerFactory
import com.instructure.canvasapi2.AppManager
import com.instructure.canvasapi2.utils.RemoteConfigUtils

open class TestAppManager: AppManager() {

    override fun onCreate() {
        super.onCreate()
        RemoteConfigUtils.initialize()
    }

    var workerFactory: WorkerFactory? = null
    override fun getWorkManagerFactory(): WorkerFactory {
        return workerFactory ?: DefaultWorkerFactory
    }

    override fun performLogoutOnAuthError() = Unit
}
