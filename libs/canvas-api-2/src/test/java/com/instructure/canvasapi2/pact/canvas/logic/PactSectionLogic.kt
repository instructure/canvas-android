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
package com.instructure.canvasapi2.pact.canvas.logic

import com.instructure.canvasapi2.models.Section
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert.assertNotNull

/**&
 * Information on how to populate a Section object's fields.
 */
data class PactSectionFieldConfig(
        val includeTotalStudents: Boolean = false
)

/**
 * Populate a Section object in a Pact specification, based on PactSectionFieldConfig settings.
 */
fun LambdaDslObject.populateSectionFields(fieldConfig: PactSectionFieldConfig = PactSectionFieldConfig()) : LambdaDslObject {

    this
            .id("id")
            .stringType("name")
            .stringMatcher("start_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .stringMatcher("end_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")

    if(fieldConfig.includeTotalStudents) {
        this.numberType("total_students")
    }

    return this
}

/**
 * Assert that a Section object in a response has been populated correctly, based on PactSectionFieldConfig settings.
 */
fun assertSectionPopulated(description: String, section: Section, fieldConfig: PactSectionFieldConfig = PactSectionFieldConfig()) {
    assertNotNull("$description + id", section.id)
    assertNotNull("$description + name", section.name)
    assertNotNull("$description + startAt", section.startAt)
    assertNotNull("$description + endAt", section.endAt)

    if(fieldConfig.includeTotalStudents) {
        assertNotNull("$description + totalStudents", section.totalStudents)
    }
}