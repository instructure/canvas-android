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
 *
 */

package com.instructure.parentapp.ui.espresso

import androidx.work.Configuration
import androidx.work.WorkerFactory
import com.instructure.canvas.espresso.WorkManagerTestAppManager
import com.instructure.canvas.espresso.WorkManagerTestHelper
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.parentapp.util.BaseAppManager

open class TestAppManager : BaseAppManager(), WorkManagerTestAppManager {

    override val workManagerTestHelper = WorkManagerTestHelper()

    override val workManagerConfiguration: Configuration
        get() = workManagerTestHelper.workManagerConfiguration

    override fun getWorkManagerFactory(): WorkerFactory = workManagerTestHelper.getWorkManagerFactory()

    override fun getScheduler(): AlarmScheduler? {
        return null
    }

    override fun performFlutterAppMigration() = Unit
}