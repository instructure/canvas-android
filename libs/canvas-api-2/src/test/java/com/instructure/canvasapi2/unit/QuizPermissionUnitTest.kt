/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.QuizPermission
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class QuizPermissionUnitTest : Assert() {

    @Test
    fun testQuizPermissions() {
        val quizPermission: QuizPermission = quizPermissionJSON.parse()

        Assert.assertNotNull(quizPermission)
        Assert.assertFalse(quizPermission.manage)
        Assert.assertFalse(quizPermission.readStatistics)
        Assert.assertTrue(quizPermission.read)
        Assert.assertFalse(quizPermission.update)
        Assert.assertFalse(quizPermission.delete)
        Assert.assertFalse(quizPermission.create)
        Assert.assertTrue(quizPermission.submit)
        Assert.assertFalse(quizPermission.grade)
        Assert.assertFalse(quizPermission.reviewGrades)
        Assert.assertFalse(quizPermission.viewAnswerAudits)
    }

    @Language("JSON")
    private var quizPermissionJSON = """
      {
        "read_statistics": false,
        "manage": false,
        "read": true,
        "update": false,
        "delete": false,
        "create": false,
        "submit": true,
        "grade": false,
        "review_grades": false,
        "view_answer_audits": false
      }"""
}
