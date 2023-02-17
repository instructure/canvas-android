/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.emeritus.student.ui.utils

import com.instructure.pandautils.di.DateTimeModule
import com.instructure.pandautils.utils.date.DateTimeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.util.*
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DateTimeModule::class])
class TestDateTimeModule {

    @Provides
    @Singleton
    fun provideDateTimeProvider(): DateTimeProvider {
        return FakeDateTimeProvider()
    }
}

class FakeDateTimeProvider : DateTimeProvider {

    var fakeTimeInMillis: Long = Calendar.getInstance().timeInMillis

    override fun getCalendar(): Calendar {
        return Calendar.getInstance().apply {
            timeInMillis = fakeTimeInMillis
        }
    }
}