/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.pandautils.domain.usecase.studytools

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.instructure.canvasapi2.models.studyassist.StudyAssistErrorResponse
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.domain.models.studytools.StudyToolsError
import com.instructure.pandautils.domain.models.studytools.StudyToolsOutcome
import java.io.IOException
import javax.inject.Inject

/**
 * Turns a failed Study Assist call into the typed error the UI branches on.
 *
 * The status code is read from the raw response rather than from `Failure`, because the shared call
 * adapter collapses both 401 and 403 into `Failure.Authorization` without keeping the code.
 */
class StudyToolsErrorMapper @Inject constructor() {

    private val gson = Gson()

    fun <T> toOutcome(result: DataResult<T>): StudyToolsOutcome<T> = when (result) {
        is DataResult.Success -> StudyToolsOutcome.Success(result.data)
        is DataResult.Fail -> StudyToolsOutcome.Failure(map(result))
    }

    fun map(fail: DataResult.Fail): StudyToolsError {
        val payload = parseBody(fail)
        val message = payload?.error
        val header = payload?.errorHeader

        return when (fail.response?.code) {
            HTTP_UNAUTHORIZED, HTTP_FORBIDDEN, HTTP_NOT_FOUND -> StudyToolsError.Unavailable(message)
            HTTP_TOO_MANY_REQUESTS -> StudyToolsError.RateLimited(message)
            HTTP_UNPROCESSABLE_CONTENT -> StudyToolsError.ContentProblem(message, header)
            else -> StudyToolsError.TemporarilyUnavailable(message)
        }
    }

    private fun parseBody(fail: DataResult.Fail): StudyAssistErrorResponse? {
        val body = fail.errorBody ?: return null
        return try {
            gson.fromJson(body.string(), StudyAssistErrorResponse::class.java)
        } catch (e: JsonSyntaxException) {
            null
        } catch (e: IllegalStateException) {
            null
        } catch (e: IOException) {
            null
        }
    }

    companion object {
        private const val HTTP_UNAUTHORIZED = 401
        private const val HTTP_FORBIDDEN = 403
        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_UNPROCESSABLE_CONTENT = 422
        private const val HTTP_TOO_MANY_REQUESTS = 429
    }
}
