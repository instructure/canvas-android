/*
 * Copyright (C) 2021 - present Instructure, Inc.
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

package com.instructure.teacher.di

import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.features.dashboard.edit.EditDashboardRouter
import com.instructure.pandautils.features.dashboard.notifications.DashboardRouter
import com.instructure.teacher.features.dashboard.edit.TeacherEditDashboardRouter
import com.instructure.teacher.features.dashboard.notifications.TeacherDashboardRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class DashboardModule {

    @Provides
    fun provideHomeroomRouter(activity: FragmentActivity): DashboardRouter {
        return TeacherDashboardRouter(activity)
    }

    @Provides
    fun provideEditDashboardRouter(activity: FragmentActivity): EditDashboardRouter {
        return TeacherEditDashboardRouter(activity)
    }
}