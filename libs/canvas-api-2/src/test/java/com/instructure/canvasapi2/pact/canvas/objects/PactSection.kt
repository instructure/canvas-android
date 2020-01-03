/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.canvasapi2.pact.canvas.objects

import com.instructure.canvasapi2.models.Section
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

data class PactSectionFieldInfo (
        val numStudents: Int = 1,
        val courseId: Long? = null
)

fun LambdaDslObject.populateSectionFields(fieldInfo: PactSectionFieldInfo = PactSectionFieldInfo()) : LambdaDslObject {

    this
            .id("id")
            .stringType("name")
            .id("course_id")
            .timestamp("start_at", PACT_TIMESTAMP_FORMAT)
            .timestamp("end_at", PACT_TIMESTAMP_FORMAT)
            .numberValue("total_students", fieldInfo.numStudents)

    if(fieldInfo.numStudents > 0) {
        this.array("students") { students ->
            repeat(fieldInfo.numStudents) { index ->
                students.`object`()  { student ->
                    student.populateUserFields(PactUserFieldInfo(numEnrollments = 1))
                }

            }
        }
    }

    if(fieldInfo.courseId != null) {
        this.id("course_id", fieldInfo.courseId)
    }
    else {
        this.id("course_id")
    }

    return this
}

fun assertSectionPopulated(description: String, section: Section, fieldInfo: PactSectionFieldInfo) {
    assertNotNull("$description + id", section.id)
    assertNotNull("$description + name", section.name)
    assertNotNull("$description + courseId", section.courseId)
    assertNotNull("$description + startAt", section.startAt)
    assertNotNull("$description + endAt", section.endAt)
    assertNotNull("$description + totalStudents", section.totalStudents)

    if(fieldInfo.numStudents > 0) {
        assertNotNull("$description + students", section.students)
        assertEquals("$description + students count", fieldInfo.numStudents, section.students!!.size)
    }
}