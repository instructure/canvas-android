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
package com.instructure.canvasapi2.models

import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.utils.isValidTerm
import kotlinx.parcelize.Parcelize
import java.util.*

@Suppress("unused")
@JvmSuppressWildcards
@Parcelize
data class Group(
        override val id: Long = 0,
        override val name: String? = "",
        val description: String? = null,
        @SerializedName("avatar_url")
        val avatarUrl: String? = null,
        @SerializedName("is_public")
        val isPublic: Boolean = false,
        // Pact: We don't use isFollowedByUser in any code, and it does not appear to
        // be populated by our API calls.  So I'm removing it for now.
        //@SerializedName("followed_by_user")
        //val isFollowedByUser: Boolean = false,
        @SerializedName("members_count")
        val membersCount: Int = 0,
        val users: List<User> = emptyList(),
        @SerializedName("join_level")
        val joinLevel: Group.JoinLevel? = Group.JoinLevel.Unknown,
        // Pact: the context_type wasn't being serialized correctly (it was looking for "course"/"account"
        // rather than "Course"/"Account"), and it is not used in our code.  So I'm removing it for now.
        //@SerializedName("context_type")
        //val contextType: Group.GroupContext? = Group.GroupContext.Other,
        // At most, ONE of course_id and account_id will be set.
        @SerializedName("course_id")
        val courseId: Long = 0,
        @SerializedName("account_id")
        val accountId: Long = 0,
        val role: Group.GroupRole? = Group.GroupRole.Course,
        @SerializedName("group_category_id")
        val groupCategoryId: Long = 0,
        @SerializedName("storage_quota_mb")
        val storageQuotaMb: Long = 0,
        @SerializedName("is_favorite")
        var isFavorite: Boolean = false,
        var concluded: Boolean = false,
        @SerializedName("can_access")
        val canAccess: Boolean? = null
) : CanvasContext() {
    override val comparisonString get() = name
    override val type get() = CanvasContext.Type.GROUP

    fun isActive(course: Course?): Boolean {
        if (canAccess == false) return false
        val courseAvailable = course?.run { isValidTerm() && !accessRestrictedByDate && endDate?.before(Date()) != true }

        // Either the group belongs to a course (that's available) or has no course id (Account groups)
        return (courseAvailable == true || courseId == 0L) && !concluded
    }

    enum class JoinLevel {
        /* If "parent_context_auto_join", anyone can join and will be automatically accepted */
        @SerializedName("parent_context_auto_join") Automatic,
        /* If "parent_context_request", anyone  can request to join, which must be approved by a group moderator. */
        @SerializedName("parent_context_request") Request,
        /* If "invitation_only", only those how have received an invitation my join the group, by accepting that invitation. */
        @SerializedName("invitation_only") Invitation,
        Unknown
    }

    /* Certain types of groups have special role designations. Currently,
       these include: "communities", "student_organized", and "imported".
       Regular course/account groups have a role of null. */
    enum class GroupRole {
        @SerializedName("communities") Community,
        @SerializedName("student_organized") Student,
        @SerializedName("imported") Imported,
        Course
    }

    enum class GroupContext {
        @SerializedName("course") Course,
        @SerializedName("account") Account,
        Other
    }
}
