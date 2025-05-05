/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

import com.instructure.pandautils.features.assignments.list.AssignmentListBehavior
import com.instructure.pandautils.features.assignments.list.AssignmentListRepository
import com.instructure.pandautils.features.assignments.list.AssignmentListRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(FragmentComponent::class)
class AssignmentListFragmentModule {
    @Provides
    fun provideAssignmentListRouter(): AssignmentListRouter {
        throw NotImplementedError()
    }
}

@Module
@InstallIn(ViewModelComponent::class)
class AssignmentListModule {
    @Provides
    fun provideAssignmentListBehavior(): AssignmentListBehavior {
        throw NotImplementedError()
    }

    @Provides
    fun provideAssignmentListRepository(): AssignmentListRepository {
        throw NotImplementedError()
    }
}