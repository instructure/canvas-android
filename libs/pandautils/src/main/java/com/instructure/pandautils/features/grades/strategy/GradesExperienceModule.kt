/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.pandautils.features.grades.strategy

import com.instructure.pandautils.features.grades.Experience
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.multibindings.IntoMap

@MapKey
annotation class ExperienceKey(val value: Experience)

@Module
@InstallIn(ViewModelComponent::class)
abstract class GradesExperienceModule {

    @Binds
    @IntoMap
    @ExperienceKey(Experience.Academic)
    abstract fun bindAcademic(impl: AcademicGradesStrategy): GradesExperienceStrategy

    @Binds
    @IntoMap
    @ExperienceKey(Experience.NGC)
    abstract fun bindNgc(impl: NGCGradesStrategy): GradesExperienceStrategy
}
