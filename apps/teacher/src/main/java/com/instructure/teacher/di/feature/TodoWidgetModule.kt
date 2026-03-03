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

package com.instructure.teacher.di.feature

import com.instructure.pandautils.features.dashboard.widget.todo.TodoWidgetBehavior
import com.instructure.pandautils.features.dashboard.widget.todo.TodoWidgetRouter
import com.instructure.teacher.features.dashboard.widget.todo.TeacherTodoWidgetBehavior
import com.instructure.teacher.features.dashboard.widget.todo.TeacherTodoWidgetRouter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class TodoWidgetModule {

    @Binds
    abstract fun bindTodoWidgetRouter(impl: TeacherTodoWidgetRouter): TodoWidgetRouter

    @Binds
    abstract fun bindTodoWidgetBehavior(impl: TeacherTodoWidgetBehavior): TodoWidgetBehavior
}