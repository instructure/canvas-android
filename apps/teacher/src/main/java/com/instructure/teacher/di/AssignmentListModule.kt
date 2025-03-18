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
package com.instructure.teacher.di

import android.content.res.Resources
import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.pandautils.features.assignments.list.AssignmentListBehavior
import com.instructure.pandautils.features.assignments.list.AssignmentListRepository
import com.instructure.pandautils.features.assignments.list.AssignmentListRouter
import com.instructure.pandautils.room.assignment.list.daos.AssignmentListFilterDao
import com.instructure.teacher.features.assignment.list.TeacherAssignmentListBehavior
import com.instructure.teacher.features.assignment.list.TeacherAssignmentListRepository
import com.instructure.teacher.features.assignment.list.TeacherAssignmentListRouter
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
        return TeacherAssignmentListRouter()
    }
}

@Module
@InstallIn(ViewModelComponent::class)
class AssignmentListViewModelModule {
    @Provides
    fun provideAssignmentListRepository(
        assignmentApi: AssignmentAPI.AssignmentInterface,
        courseApi: CourseAPI.CoursesInterface,
        assignmentListFilterDao: AssignmentListFilterDao
    ): AssignmentListRepository {
        return TeacherAssignmentListRepository(assignmentApi, courseApi, assignmentListFilterDao)
    }

    @Provides
    fun provideAssignmentListBehavior(resources: Resources): AssignmentListBehavior {
        return TeacherAssignmentListBehavior(resources)
    }
}