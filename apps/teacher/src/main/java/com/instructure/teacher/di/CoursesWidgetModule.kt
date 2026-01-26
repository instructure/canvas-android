/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

import com.instructure.pandautils.features.dashboard.widget.courses.CoursesWidgetBehavior
import com.instructure.pandautils.features.dashboard.widget.courses.CoursesWidgetRouter
import com.instructure.teacher.features.dashboard.widget.courses.TeacherCoursesWidgetBehavior
import com.instructure.teacher.features.dashboard.widget.courses.TeacherCoursesWidgetRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class CoursesWidgetModule {

    @Provides
    fun provideCoursesWidgetRouter(): CoursesWidgetRouter {
        return TeacherCoursesWidgetRouter()
    }

    @Provides
    fun provideCoursesWidgetBehavior(
        teacherCoursesWidgetBehavior: TeacherCoursesWidgetBehavior
    ): CoursesWidgetBehavior {
        return teacherCoursesWidgetBehavior
    }
}