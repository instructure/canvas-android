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

import com.instructure.canvasapi2.models.User
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

data class PactUserFieldInfo(
        val id: Long? = null,
        val numEnrollments: Int = 0
)

fun LambdaDslObject.populateUserFields(fieldInfo: PactUserFieldInfo = PactUserFieldInfo()) : LambdaDslObject {

    this
            .stringType("name")
            .stringType("short_name")
            .stringType("login_id")
            .stringType("avatar_url")
            .stringType("primary_email")
            .stringType("email")
            .stringType("sortable_name")
            .stringType("bio")
            .stringType("locale")
            .stringType("effective_locale")

    //
    // Optional / configurable fields
    //

    if(fieldInfo.id != null) {
        this.id("id", fieldInfo.id)
    }
    else {
        this.id("id")
    }

    if(fieldInfo.numEnrollments > 0) {
        this.array("enrollments") { enrollments ->
            repeat(fieldInfo.numEnrollments) {
                enrollments.`object`() { enrollment ->
                    enrollment.populateEnrollmentFields()
                }
            }
        }
    }

    return this
}

fun assertUserPopulated(description: String, user: User, fieldInfo: PactUserFieldInfo = PactUserFieldInfo()) {
    assertNotNull("$description + id", user.id)
    assertNotNull("$description + name", user.name)
    assertNotNull("$description + shortName", user.shortName)
    assertNotNull("$description + loginId", user.loginId)
    assertNotNull("$description + avatarUrl", user.avatarUrl)
    assertNotNull("$description + primaryEmail", user.primaryEmail)
    assertNotNull("$description + email", user.email)
    assertNotNull("$description + sortableName", user.sortableName)
    assertNotNull("$description + bio", user.bio)
    assertNotNull("$description + locale", user.locale)
    assertNotNull("$description + effective_locale", user.effective_locale)

    if(fieldInfo.id != null) {
        assertEquals("$description + id", fieldInfo.id, user.id)
    }

    if(fieldInfo.numEnrollments > 0) {
        assertNotNull("$description + enrollments", user.enrollments)
        assertEquals("$description + enrollment count", fieldInfo.numEnrollments, user.enrollments.size)
    }
}