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

package com.instructure.student.di

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.appdatabase.daos.ToDoFilterDao
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.student.widget.WidgetLogger
import com.instructure.student.widget.WidgetUpdater
import com.instructure.student.widget.grades.GradesWidgetRepository
import com.instructure.student.widget.grades.list.GradesWidgetUpdater
import com.instructure.student.widget.grades.singleGrade.SingleGradeWidgetUpdater
import com.instructure.student.widget.todo.ToDoWidgetRepository
import com.instructure.student.widget.todo.ToDoWidgetUpdater
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class WidgetModule {

    @Provides
    fun provideAppWidgetManager(
        @ApplicationContext context: Context
    ): AppWidgetManager {
        return AppWidgetManager.getInstance(context)
    }

    @Provides
    fun provideGlanceAppWidgetManager(
        @ApplicationContext context: Context
    ): GlanceAppWidgetManager {
        return GlanceAppWidgetManager(context)
    }

    @Provides
    fun provideWidgetUpdater(): WidgetUpdater {
        return WidgetUpdater
    }

    @Provides
    fun provideToDoWidgetRepository(
        plannerApi: PlannerAPI.PlannerInterface,
        coursesApi: CourseAPI.CoursesInterface,
        toDoFilterDao: ToDoFilterDao,
        apiPrefs: ApiPrefs
    ): ToDoWidgetRepository {
        return ToDoWidgetRepository(plannerApi, coursesApi, toDoFilterDao, apiPrefs)
    }

    @Provides
    fun provideToDoWidgetUpdater(
        repository: ToDoWidgetRepository,
        apiPrefs: ApiPrefs
    ): ToDoWidgetUpdater {
        return ToDoWidgetUpdater(repository, apiPrefs)
    }

    @Provides
    fun provideGradesWidgetRepository(
        courseApi: CourseAPI.CoursesInterface
    ): GradesWidgetRepository {
        return GradesWidgetRepository(courseApi)
    }

    @Provides
    fun provideGradesWidgetUpdater(
        repository: GradesWidgetRepository,
        apiPrefs: ApiPrefs,
        glanceAppWidgetManager: GlanceAppWidgetManager
    ): GradesWidgetUpdater {
        return GradesWidgetUpdater(repository, apiPrefs, glanceAppWidgetManager)
    }

    @Provides
    fun provideSingleGradeWidgetUpdater(
        repository: GradesWidgetRepository,
        apiPrefs: ApiPrefs,
        glanceAppWidgetManager: GlanceAppWidgetManager
    ): SingleGradeWidgetUpdater {
        return SingleGradeWidgetUpdater(repository, apiPrefs, glanceAppWidgetManager)
    }

    @Singleton
    @Provides
    fun provideWidgetLogger(
        userApi: UserAPI.UsersInterface,
        featureFlagProvider: FeatureFlagProvider,
        featuresManager: FeaturesManager,
        analytics: Analytics
    ): WidgetLogger {
        return WidgetLogger(
            userApi = userApi,
            featureFlagProvider = featureFlagProvider,
            featuresManager = featuresManager,
            analytics = analytics
        )
    }
}
