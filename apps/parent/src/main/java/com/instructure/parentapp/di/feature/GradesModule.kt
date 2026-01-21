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

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.pandautils.features.grades.GradesRepository
import com.instructure.pandautils.features.grades.GradesViewModelBehavior
import com.instructure.parentapp.features.grades.ParentGradesRepository
import com.instructure.parentapp.features.grades.ParentGradesViewModelBehavior
import com.instructure.parentapp.util.ParentPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class GradesModule {

    @Provides
    fun provideGradesRepository(
        assignmentApi: AssignmentAPI.AssignmentInterface,
        courseApi: CourseAPI.CoursesInterface,
        parentPrefs: ParentPrefs,
        customGradeStatusesManager: CustomGradeStatusesManager
    ): GradesRepository {
        return ParentGradesRepository(assignmentApi, courseApi, parentPrefs, customGradeStatusesManager)
    }

    @Provides
    fun provideGradesViewModelBehavior(): GradesViewModelBehavior {
        return ParentGradesViewModelBehavior()
    }
}
