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
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.QLCallback
import com.instructure.canvasapi2.StudentContextCardQuery
import com.instructure.canvasapi2.enqueueQuery

class StudentContextManagerImpl : StudentContextManager {
    override fun getStudentContext(
        courseId: Long,
        userId: Long,
        submissionPageSize: Int,
        forceNetwork: Boolean,
        callback: QLCallback<StudentContextCardQuery.Data>
    ) {
        val query = StudentContextCardQuery.builder()
            .courseId(courseId.toString())
            .studentId(userId.toString())
            .pageSize(submissionPageSize)
            .nextCursor(callback.nextCursor)
            .build()

        callback.enqueueQuery(query, forceNetwork)
    }
}