/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.di.feature

import android.content.res.Resources
import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.features.calendarevent.details.EventRouter
import com.instructure.pandautils.features.calendarevent.details.EventViewModelBehavior
import com.instructure.parentapp.features.calendarevent.ParentEventRouter
import com.instructure.parentapp.features.calendarevent.ParentEventViewModelBehavior
import com.instructure.parentapp.util.ParentPrefs
import com.instructure.parentapp.util.navigation.Navigation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(FragmentComponent::class)
class EventModule {

    @Provides
    fun provideEventRouter(activity: FragmentActivity, navigation: Navigation): EventRouter {
        return ParentEventRouter(activity, navigation)
    }
}

@Module
@InstallIn(ViewModelComponent::class)
class EventViewModelModule {

    @Provides
    fun provideEventViewModelBehavior(resources: Resources, parentPrefs: ParentPrefs): EventViewModelBehavior {
        return ParentEventViewModelBehavior(resources, parentPrefs)
    }
}
