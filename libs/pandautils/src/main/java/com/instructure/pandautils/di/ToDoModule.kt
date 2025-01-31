/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */

package com.instructure.pandautils.di

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.pandautils.features.calendartodo.details.ToDoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class ToDoModule {

    @Provides
    fun provideToDoRepository(
        plannerApi: PlannerAPI.PlannerInterface,
        courseApi: CourseAPI.CoursesInterface,
        groupApi: GroupAPI.GroupInterface,
        userApi: UserAPI.UsersInterface,
    ): ToDoRepository {
        return ToDoRepository(plannerApi, courseApi, groupApi, userApi)
    }
}