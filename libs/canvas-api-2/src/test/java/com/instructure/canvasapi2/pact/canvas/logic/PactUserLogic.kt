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

import com.instructure.canvasapi2.models.User
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

/**
 * Information about how to set up user fields.
 * If [id] is non-null, we will expect an id with that value, otherwise we will expect a generic id value.
 * The [includeEnrollment] value determines whether an Enrollment object will appear in the user object.
 * If [includeLocaleInfo] is true, we will expect locale and effective_locale fields to be populated.  [includeLocaleInfo] is
 * typically false for user objects that are embedded in other types of objects.
 * If [includeProfileInfo] is true, we will expect bio, avatar_url and primary_email fields to be populated.
 * If [includePronouns] is true, we will expect the pronouns field to be populated.
 * If [includeLoginId] is true, we will expect the login_id field to be populated.
 * If [includePermissions] is true, we will expect the permissions object/fields to be populated
 */
data class PactUserFieldConfig(
        val id: Long? = null,
        val includeEnrollment: Boolean = false,
        val includeLocaleInfo: Boolean = false,
        val includeProfileInfo: Boolean = false,
        val includePronouns: Boolean = true,
        val includeLoginId: Boolean = false,
        val includePermissions: Boolean = false
)

/**
 * Populate a user object in a Pact specification, based on PactUserFieldConfig.
 */
fun LambdaDslObject.populateUserFields(fieldConfig: PactUserFieldConfig = PactUserFieldConfig()) : LambdaDslObject {

    this
            .stringType("name")
            .stringType("short_name")
            .stringType("sortable_name")

    //
    // Optional / configurable fields
    //

    if(fieldConfig.includePermissions) {
        this.`object`("permissions") { permissions ->
            permissions.booleanType("can_update_name")
            permissions.booleanType("can_update_avatar")
        }

    }
    if(fieldConfig.includeLoginId) {
        this.stringType("login_id")
    }

    if(fieldConfig.includePronouns) {
        this.stringType("pronouns")
    }

    if(fieldConfig.id != null) {
        this.id("id", fieldConfig.id)
    }
    else {
        this.id("id")
    }

    if(fieldConfig.includeEnrollment) {
        this.array("enrollments") { enrollments ->
            enrollments.`object`() { enrollment ->
                enrollment.populateEnrollmentFields(PactEnrollmentFieldConfig(includeGrades = true))
            }
        }
    }

    if(fieldConfig.includeLocaleInfo) {
        this
                .stringType("locale")
                .stringType("effective_locale")

    }

    if(fieldConfig.includeProfileInfo) {
        this
                //.stringType("avatar_url") // Evidently, we just never get avatar_urls
                .stringType("primary_email")
                .stringType("bio")
    }

    return this
}

/**
 * Assert that a user object has been properly populated in a response.
 */
fun assertUserPopulated(description: String, user: User, fieldConfig: PactUserFieldConfig = PactUserFieldConfig()) {
    assertNotNull("$description + id", user.id)
    assertNotNull("$description + name", user.name)
    assertNotNull("$description + shortName", user.shortName)
    assertNotNull("$description + sortableName", user.sortableName)

    if(fieldConfig.includePermissions) {
        assertNotNull("permissions",  user.permissions)
        assertNotNull("permissions.canUpdateAvatar",  user.permissions!!.canUpdateAvatar)
        assertNotNull("permissions.canUpdateName",  user.permissions!!.canUpdateName)
    }

    if(fieldConfig.includeLoginId) {
        assertNotNull("$description + loginId", user.loginId)
    }

    if(fieldConfig.includePronouns) {
        assertNotNull("$description + pronouns", user.pronouns)
    }

    if(fieldConfig.id != null) {
        assertEquals("$description + id", fieldConfig.id, user.id)
    }

    if(fieldConfig.includeEnrollment) {
        assertNotNull("$description + enrollments", user.enrollments)
        assertEquals("$description + enrollment count", 1, user.enrollments.size)
        assertEnrollmentPopulated("$description + enrollment", user.enrollments[0])
    }

    if(fieldConfig.includeLocaleInfo) {
        assertNotNull("$description + locale", user.locale)
        assertNotNull("$description + effective_locale", user.effective_locale)
    }

    if(fieldConfig.includeProfileInfo) {
        //assertNotNull("$description + avatarUrl", user.avatarUrl)
        assertNotNull("$description + primaryEmail", user.primaryEmail)
        assertNotNull("$description + bio", user.bio)
    }
}