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

data class PactSectionFieldInfo(
        val includeTotalStudents: Boolean = false
)

fun LambdaDslObject.populateSectionFields(fieldInfo: PactSectionFieldInfo = PactSectionFieldInfo()) : LambdaDslObject {

    this
            .id("id")
            .stringType("name")
            .timestamp("start_at", PACT_TIMESTAMP_FORMAT)
            .timestamp("end_at", PACT_TIMESTAMP_FORMAT)

    if(fieldInfo.includeTotalStudents) {
        this.numberType("total_students")
    }

    return this
}

fun assertSectionPopulated(description: String, section: Section, fieldInfo: PactSectionFieldInfo = PactSectionFieldInfo()) {
    assertNotNull("$description + id", section.id)
    assertNotNull("$description + name", section.name)
    assertNotNull("$description + startAt", section.startAt)
    assertNotNull("$description + endAt", section.endAt)

    if(fieldInfo.includeTotalStudents) {
        assertNotNull("$description + totalStudents", section.totalStudents)
    }
}