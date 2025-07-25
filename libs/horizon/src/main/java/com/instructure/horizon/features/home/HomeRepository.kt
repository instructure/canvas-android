/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.home

import com.instructure.canvasapi2.apis.ThemeAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.CourseWithProgress
import com.instructure.canvasapi2.managers.HorizonGetCoursesManager
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val apiPrefs: ApiPrefs,
    private val themeApi: ThemeAPI.ThemeInterface,
    private val userApi: UserAPI.UsersInterface,
    private val getCoursesManager: HorizonGetCoursesManager,
) {

    suspend fun getTheme(): CanvasTheme? {
        val params = RestParams(isForceReadFromNetwork = false)
        return themeApi.getTheme(params).dataOrNull
    }

    suspend fun getSelf(): User? {
        val params = RestParams(isForceReadFromNetwork = true)
        return userApi.getSelf(params).dataOrNull
    }

    suspend fun getCourses(): List<CourseWithProgress> {
        return getCoursesManager.getCoursesWithProgress(apiPrefs.user!!.id, false).dataOrThrow
    }
}