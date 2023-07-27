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

package com.instructure.student.features.grades

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.isInvited

class GradesRepository(private val gradesNetworkDataSource: GradesNetworkDataSource) {

    suspend fun loadCourses(): List<Course> {
        val courses = gradesNetworkDataSource.loadCourses()
        return courses
            .filter { it.isCurrentEnrolment() && !it.isInvited() }
    }

    suspend fun loadColors(): Map<String, String> {
        return gradesNetworkDataSource.loadColors()
    }
}