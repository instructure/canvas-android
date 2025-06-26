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
package com.instructure.teacher.di

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.di.PLANNER_API_SERIALIZE_NULLS
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoViewModelBehavior
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoRepository
import com.instructure.teacher.features.calendartodo.TeacherCreateUpdateToDoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import javax.inject.Named

@Module
@InstallIn(ViewModelComponent::class)
class CreateUpdateToDoModule {

    @Provides
    fun provideCreateUpdateToDoRepository(
        coursesApi: CourseAPI.CoursesInterface,
        @Named(PLANNER_API_SERIALIZE_NULLS) plannerApi: PlannerAPI.PlannerInterface
    ): CreateUpdateToDoRepository {
        return TeacherCreateUpdateToDoRepository(coursesApi, plannerApi)
    }

    @Provides
    fun provideCreateUpdateToDoBehavior(): CreateUpdateToDoViewModelBehavior {
        return object : CreateUpdateToDoViewModelBehavior {
            override fun updateWidget() = Unit
        }
    }
}