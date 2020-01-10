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

/**
 * Information about how to set up user fields.
 * If [id] is non-null, we will expect an id with that value, otherwise we will expect a generic id value.
 * The [numEnrollments] value determines the number of Enrollment objects that will appear in the user object.
 * If [populateFully] is true, we will expect locale and effective_locale fields to be populated.  [PopulateFully] is
 * false for user objects that are embedded in other types of objects.
 * If [isProfile] is true, we will expect bio, avatar_url and primary_email fields to be populated.
 * If [includePronouns] is true, we will expect the pronouns field to be populated.
 */
data class PactUserFieldInfo(
        val id: Long? = null,
        val numEnrollments: Int = 0,
        val populateFully: Boolean = false,
        val isProfile: Boolean = false,
        val includePronouns: Boolean = true
)

/**
 * Populate a user object in a Pact specification, based on PaceUserFieldInfo.
 */
fun LambdaDslObject.populateUserFields(fieldInfo: PactUserFieldInfo = PactUserFieldInfo()) : LambdaDslObject {

    this
            .stringType("name")
            .stringType("short_name")
            .stringType("login_id")
            .stringType("sortable_name")

    //
    // Optional / configurable fields
    //

    if(fieldInfo.includePronouns) {
        this.stringType("pronouns")
    }

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

    if(fieldInfo.populateFully) {
        this
                .stringType("locale")
                .stringType("effective_locale")

    }

    if(fieldInfo.isProfile) {
        this
                .stringType("avatar_url")
                .stringType("primary_email")
                .stringType("bio")
    }

    return this
}

/**
 * Assert that a user object has been properly populated in a response.
 */
fun assertUserPopulated(description: String, user: User, fieldInfo: PactUserFieldInfo = PactUserFieldInfo()) {
    assertNotNull("$description + id", user.id)
    assertNotNull("$description + name", user.name)
    assertNotNull("$description + shortName", user.shortName)
    assertNotNull("$description + loginId", user.loginId)
    assertNotNull("$description + sortableName", user.sortableName)

    if(fieldInfo.includePronouns) {
        assertNotNull("$description + pronouns", user.pronouns)
    }

    if(fieldInfo.id != null) {
        assertEquals("$description + id", fieldInfo.id, user.id)
    }

    if(fieldInfo.numEnrollments > 0) {
        assertNotNull("$description + enrollments", user.enrollments)
        assertEquals("$description + enrollment count", fieldInfo.numEnrollments, user.enrollments.size)
    }

    if(fieldInfo.populateFully) {
        assertNotNull("$description + locale", user.locale)
        assertNotNull("$description + effective_locale", user.effective_locale)
    }

    if(fieldInfo.isProfile) {
        assertNotNull("$description + avatarUrl", user.avatarUrl)
        assertNotNull("$description + primaryEmail", user.primaryEmail)
        assertNotNull("$description + bio", user.bio)
    }
}