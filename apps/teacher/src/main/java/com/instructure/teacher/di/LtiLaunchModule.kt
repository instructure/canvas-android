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
package com.instructure.teacher.di

import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.features.lti.LtiLaunchFragmentBehavior
import com.instructure.pandautils.utils.Const
import com.instructure.teacher.features.lti.TeacherLtiLaunchFragmentBehavior
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class LtiLaunchModule {

    @Provides
    fun provideLtiLaunchFragmentBehavior(fragment: Fragment): LtiLaunchFragmentBehavior {
        val canvasContext = fragment.arguments?.getParcelable(Const.CANVAS_CONTEXT) ?: CanvasContext.emptyUserContext()
        return TeacherLtiLaunchFragmentBehavior(canvasContext)
    }
}