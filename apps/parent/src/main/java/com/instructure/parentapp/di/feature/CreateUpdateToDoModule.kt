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

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.di.PLANNER_API_SERIALIZE_NULLS
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoBehavior
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoRepository
import com.instructure.parentapp.features.calendartodo.ParentCreateUpdateToDoRepository
import com.instructure.parentapp.util.ParentPrefs
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
        parentPrefs: ParentPrefs,
        @Named(PLANNER_API_SERIALIZE_NULLS) plannerApi: PlannerAPI.PlannerInterface
    ): CreateUpdateToDoRepository {
        return ParentCreateUpdateToDoRepository(coursesApi, parentPrefs, plannerApi)
    }

    @Provides
    fun provideCreateUpdateToDoBehavior(): CreateUpdateToDoBehavior {
        return object : CreateUpdateToDoBehavior {
            override fun updateWidget() = Unit
        }
    }
}
