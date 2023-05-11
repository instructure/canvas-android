/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.emeritus.student.db

import com.emeritus.student.PendingSubmissionComment
import com.emeritus.student.Submission
import com.emeritus.student.db.sqlColAdapters.CanvasContextColAdapter
import com.emeritus.student.db.StudentDb
import com.emeritus.student.db.sqlColAdapters.DateAdapter
import com.squareup.sqldelight.db.SqlDriver

fun createQueryWrapper(driver: SqlDriver): StudentDb {
    return StudentDb(
        driver = driver,
        submissionAdapter = Submission.Adapter(
            lastActivityDateAdapter = DateAdapter(),
            canvasContextAdapter = CanvasContextColAdapter()
        ),
        pendingSubmissionCommentAdapter = PendingSubmissionComment.Adapter(
            canvasContextAdapter = CanvasContextColAdapter(),
            lastActivityDateAdapter = DateAdapter()
        )
    )
}

object Schema : SqlDriver.Schema by StudentDb.Schema {
    override fun create(driver: SqlDriver) {
        StudentDb.Schema.create(driver)
    }
}
