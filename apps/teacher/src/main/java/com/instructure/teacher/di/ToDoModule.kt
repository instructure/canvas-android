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

import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.features.calendartodo.details.ToDoViewModelBehavior
import com.instructure.pandautils.features.calendartodo.details.ToDoRouter
import com.instructure.pandautils.features.todolist.DefaultToDoListRouter
import com.instructure.pandautils.features.todolist.ToDoListRouter
import com.instructure.teacher.features.calendartodo.TeacherToDoRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(FragmentComponent::class)
class ToDoModule {

    @Provides
    fun provideToDoRouter(activity: FragmentActivity): ToDoRouter {
        return TeacherToDoRouter(activity)
    }

    @Provides
    fun provideToDoListRouter(): ToDoListRouter {
        return DefaultToDoListRouter()
    }
}

@Module
@InstallIn(ViewModelComponent::class)
class ToDoViewModelModule {

    @Provides
    fun provideToDoBehavior(): ToDoViewModelBehavior {
        return object : ToDoViewModelBehavior {
            override fun updateWidget() = Unit
        }
    }
}
