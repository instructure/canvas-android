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

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
        override val id: Long = 0,
        override val name: String = "",
        @SerializedName("short_name")
        val shortName: String? = null,
        @SerializedName("login_id")
        val loginId: String? = null,
        @SerializedName("avatar_url")
        var avatarUrl: String? = null,
        @SerializedName("primary_email")
        val primaryEmail: String? = null,
        val email: String? = null,
        @SerializedName("sortable_name")
        val sortableName: String? = null,
        val bio: String? = null,
        val enrollments: List<Enrollment> = ArrayList(),
        // Helper variable for the "specified" enrollment.
        val enrollmentIndex: Int = 0,
        @SerializedName("last_login")
        val lastLogin: String? = null,
        val locale: String? = null,
        @SerializedName("effective_locale")
        val effective_locale: String? = null,
        val pronouns: String? = null,
        @SerializedName("k5_user")
        val k5User: Boolean = false,
        @SerializedName("root_account")
        val rootAccount: String? = null,
        val isFakeStudent: Boolean = false,
        val calendar: UserCalendar? = null,
        val uuid: String? = null,
        @SerializedName("account_uuid")
        val accountUuid: String? = null
) : CanvasContext() {
    override val comparisonString get() = name
    override val type get() = CanvasContext.Type.USER

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (id xor id.ushr(32)).toInt()
        return result
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null -> false
            other is User -> id == other.id
            else -> false
        }
    }

    // Matches recipients common_courses or common_groups format
    val enrollmentsHash: MutableMap<String, ArrayList<Enrollment.EnrollmentType?>>
        get() {
            val enrollmentsMap = mutableMapOf<String, ArrayList<Enrollment.EnrollmentType?>>()
            for (enrollment in enrollments) {
                val key = enrollment.courseId.toString()
                if (enrollmentsMap.containsKey(key)) {
                    enrollmentsMap[key]?.add(enrollment.role)
                } else {
                    val newList = arrayListOf<Enrollment.EnrollmentType?>()
                    newList.add(enrollment.role)
                    enrollmentsMap[key] = newList
                }
            }

            val stringArrayEnrollments = mutableMapOf<String, ArrayList<Enrollment.EnrollmentType?>>()
            for ((key, value) in enrollmentsMap) {
                stringArrayEnrollments[key] = value
            }

            return stringArrayEnrollments
        }

    // User Permissions - defaults to false, returned with UserAPI.getSelfWithPermissions()
    fun canUpdateAvatar(): Boolean = permissions != null && permissions!!.canUpdateAvatar
    fun canUpdateName(): Boolean = permissions != null && permissions!!.canUpdateName
    fun limitParentAppWebAccess(): Boolean = permissions?.limitParentAppWebAccess ?: false
}

@Parcelize
data class UserCalendar(val ics: String) : Parcelable
