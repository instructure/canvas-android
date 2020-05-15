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

import com.instructure.canvasapi2.models.Group
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert.assertNotNull

data class PactGroupFieldConfig(
    val includeFavorites: Boolean = false,
    val includeCanAccess: Boolean = false
) {
    companion object {
        fun fromQueryString(query: String?) : PactGroupFieldConfig {
            return PactGroupFieldConfig(
                    includeFavorites = query?.contains("=favorites") == true,
                    includeCanAccess = query?.contains("=can_access") == true
            )
        }
    }
}

fun LambdaDslObject.populateGroupFields(fieldConfig: PactGroupFieldConfig = PactGroupFieldConfig()): LambdaDslObject {
    this
            .id("id")
            .stringType("name")
            .stringType("description")
            .stringType("avatar_url")
            .booleanType("is_public")
            .id("members_count") // essentially an int
            .stringMatcher("join_level","parent_context_auto_join|parent_context_request|invitation_only", "invitation_only")
            //.stringType("role") // Typically not populated
            .id("group_category_id")
            .id("storage_quota_mb") // like a long
            .booleanType("concluded")
            .id("course_id")

    if(fieldConfig.includeCanAccess) {
        this.booleanType("can_access")
    }

    if(fieldConfig.includeFavorites) {
        this.booleanType("is_favorite")
    }

    return this
}

fun assertGroupPopulated(description: String, group: Group, fieldConfig: PactGroupFieldConfig = PactGroupFieldConfig()) {
    assertNotNull("$description + id", group.id)
    assertNotNull("$description + name", group.name)
    assertNotNull("$description + description", group.description)
    assertNotNull("$description + avatarUrl", group.avatarUrl)
    assertNotNull("$description + isPublic", group.isPublic)
    assertNotNull("$description + membersCount", group.membersCount)
    assertNotNull("$description + joinLevel", group.joinLevel)
    assertNotNull("$description + groupCategoryId", group.groupCategoryId)
    assertNotNull("$description + storageQuotaMb", group.storageQuotaMb)
    assertNotNull("$description + concluded", group.concluded)
    assertNotNull("$description + courseId", group.courseId)

    if(fieldConfig.includeFavorites) {
        assertNotNull("$description + isFavorite", group.isFavorite)
    }

    if(fieldConfig.includeCanAccess) {
        assertNotNull("$description + canAccess", group.canAccess)
    }
}