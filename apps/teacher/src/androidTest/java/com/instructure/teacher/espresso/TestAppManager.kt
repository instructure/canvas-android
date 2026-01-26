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
package com.instructure.teacher.espresso

import androidx.work.Configuration
import androidx.work.WorkerFactory
import com.instructure.canvas.espresso.WorkManagerTestAppManager
import com.instructure.canvas.espresso.WorkManagerTestHelper
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.teacher.utils.BaseAppManager

open class TestAppManager : BaseAppManager(), WorkManagerTestAppManager {

    override val workManagerTestHelper = WorkManagerTestHelper()

    override val workManagerConfiguration: Configuration
        get() = workManagerTestHelper.workManagerConfiguration

    override fun getWorkManagerFactory(): WorkerFactory = workManagerTestHelper.getWorkManagerFactory()

    override fun getScheduler(): AlarmScheduler? = null
}
