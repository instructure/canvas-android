/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.student.ui.utils.di

import com.instructure.pandautils.features.dashboard.widget.welcome.TimeOfDayCalculator
import com.instructure.pandautils.features.dashboard.widget.welcome.TimeProvider
import com.instructure.pandautils.features.dashboard.widget.welcome.di.WelcomeWidgetModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(components = [ViewModelComponent::class], replaces = [WelcomeWidgetModule::class])
class TestTimeProviderModule {

    @Provides
    fun provideTimeProvider(): TimeProvider = FakeTimeProvider

    @Provides
    fun provideTimeOfDayCalculator(timeProvider: TimeProvider): TimeOfDayCalculator {
        return TimeOfDayCalculator(timeProvider)
    }
}

// A plain Kotlin singleton rather than a Hilt-scoped instance, so tests can mutate
// fakeHourOfDay directly without needing to inject anything.
object FakeTimeProvider : TimeProvider {

    private const val DEFAULT_HOUR_OF_DAY = 12

    var fakeHourOfDay: Int = DEFAULT_HOUR_OF_DAY

    override fun getCurrentHourOfDay(): Int = fakeHourOfDay
    fun reset() {
        fakeHourOfDay = DEFAULT_HOUR_OF_DAY
    }
}
