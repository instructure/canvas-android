/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 */
package com.instructure.student.ui.utils.di

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