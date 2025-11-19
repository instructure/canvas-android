/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.welcome.di

import com.instructure.pandautils.features.dashboard.widget.welcome.SystemTimeProvider
import com.instructure.pandautils.features.dashboard.widget.welcome.TimeOfDayCalculator
import com.instructure.pandautils.features.dashboard.widget.welcome.TimeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class WelcomeWidgetModule {

    @Provides
    fun provideTimeProvider(): TimeProvider = SystemTimeProvider()

    @Provides
    fun provideTimeOfDayCalculator(timeProvider: TimeProvider): TimeOfDayCalculator {
        return TimeOfDayCalculator(timeProvider)
    }
}