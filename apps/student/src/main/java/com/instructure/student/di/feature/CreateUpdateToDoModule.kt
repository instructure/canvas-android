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
package com.instructure.student.di.feature

import android.appwidget.AppWidgetManager
import android.content.Context
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.di.PLANNER_API_SERIALIZE_NULLS
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoViewModelBehavior
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoRepository
import com.instructure.student.features.calendartodo.createupdate.StudentCreateUpdateToDoViewModelBehavior
import com.instructure.student.features.calendartodo.createupdate.StudentCreateUpdateToDoRepository
import com.instructure.student.widget.WidgetUpdater
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Named

@Module
@InstallIn(ViewModelComponent::class)
class CreateUpdateToDoModule {

    @Provides
    fun provideCreateUpdateToDoRepository(
        coursesApi: CourseAPI.CoursesInterface,
        @Named(PLANNER_API_SERIALIZE_NULLS) plannerApi: PlannerAPI.PlannerInterface
    ): CreateUpdateToDoRepository {
        return StudentCreateUpdateToDoRepository(coursesApi, plannerApi)
    }

    @Provides
    fun provideCreateUpdateToDoBehavior(
        @ApplicationContext context: Context,
        widgetUpdater: WidgetUpdater,
        appWidgetManager: AppWidgetManager
    ): CreateUpdateToDoViewModelBehavior {
        return StudentCreateUpdateToDoViewModelBehavior(context, widgetUpdater, appWidgetManager)
    }
}