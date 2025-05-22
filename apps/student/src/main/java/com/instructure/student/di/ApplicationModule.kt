/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 *
 */

package com.instructure.student.di

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.pandautils.utils.LogoutHelper
import com.instructure.student.router.EnabledTabs
import com.instructure.student.router.EnabledTabsImpl
import com.instructure.student.util.StudentLogoutHelper
import com.instructure.student.widget.grades.GradesWidgetRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {

    @Provides
    fun provideLogoutHelper(): LogoutHelper {
        return StudentLogoutHelper()
    }

    @Provides
    @Singleton
    fun provideEnabledTabs(): EnabledTabs {
        return EnabledTabsImpl()
    }

    @Provides
    fun provideGradesWidgetRepository(
        courseApi: CourseAPI.CoursesInterface
    ): GradesWidgetRepository {
        return GradesWidgetRepository(courseApi)
    }
}