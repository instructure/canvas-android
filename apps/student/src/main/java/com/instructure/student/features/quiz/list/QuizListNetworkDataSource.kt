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

package com.instructure.student.features.quiz.list

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.depaginate

class QuizListNetworkDataSource(private val quizApi: QuizAPI.QuizInterface, private val courseApi: CourseAPI.CoursesInterface) : QuizListDataSource {

    override suspend fun loadQuizzes(contextType: String, contextId: Long, forceNetwork: Boolean): List<Quiz> {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return quizApi.getFirstPageQuizzesList(contextType, contextId, restParams).depaginate {
            quizApi.getNextPageQuizzesList(it, restParams)
        }.dataOrThrow
    }

    override suspend fun loadCourseSettings(courseId: Long, forceNetwork: Boolean): CourseSettings? {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return courseApi.getCourseSettings(courseId, restParams).dataOrNull
    }
}