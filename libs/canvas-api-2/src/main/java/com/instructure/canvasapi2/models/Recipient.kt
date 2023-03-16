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
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recipient(
        @SerializedName("id")
        val stringId: String? = null,
        val name: String? = null,
        val pronouns: String? = null,
        @SerializedName("full_name")
        val fullName: String? = null,
        @SerializedName("user_count")
        val userCount: Int = 0,
        @SerializedName("item_count")
        val itemCount: Int = 0,
        @SerializedName("avatar_url")
        val avatarURL: String? = null,
        @SerializedName("common_courses")
        val commonCourses: HashMap<String, Array<String>>? = null,
        @SerializedName("common_groups")
        val commonGroups: HashMap<String, Array<String>>? = null
) : CanvasComparable<Recipient>() {

    override val id: Long
        get() = throw IllegalAccessException("Recipient.id is not populated. Use Recipient.stringId instead.")

    val idAsLong: Long
        get() {
            try {
                if (stringId!!.startsWith("group_") || stringId.startsWith("course_")) {
                    val indexUnder = stringId.indexOf("_")
                    return stringId.substring(indexUnder + 1, stringId.length).toLong()
                }
                return stringId.toLong()
            } catch (ex: NumberFormatException) {
                return 0
            }

        }

    val recipientType: Type
        get() {
            try {
                stringId?.toLong()
                return Type.Person
            } catch (E: Exception) { }

            return if (userCount > 0) {
                Type.Group
            } else Type.Metagroup

        }

    val enrollment: Enrollment
        get() = when (name) {
            "Teachers" -> Enrollment.Teacher
            "Teaching Assistants" -> Enrollment.TeachingAssistant
            "Designers" -> Enrollment.Designer
            "Students" -> Enrollment.Student
            "Observers" -> Enrollment.Observer
            else -> Enrollment.Other
        }

    enum class Type {
        Group, Metagroup, Person
    }

    enum class Enrollment {
        Teacher, TeachingAssistant, Designer, Student, Observer, Other
    }

    override val comparisonString get() = name

    override fun equals(other: Any?): Boolean {
        return (other as? Recipient)?.stringId?.compareTo(this.stringId.orEmpty()) == 0
    }

    override fun hashCode(): Int {
        return stringId?.hashCode() ?: super.hashCode()
    }

    companion object {
        fun recipientTypeToInt(t: Type): Int = when (t) {
            Type.Group -> 0
            Type.Metagroup -> 1
            Type.Person -> 2
        }

        fun intToRecipientType(i: Int): Type? = when (i) {
            0 -> Type.Group
            1 -> Type.Metagroup
            2 -> Type.Person
            else -> null
        }

        fun from(user: User) = Recipient(
            stringId = user.id.toString(),
            name = user.shortName,
            pronouns = user.pronouns,
            avatarURL = user.avatarUrl,
        )

        fun from(user: BasicUser) = Recipient(
            stringId = user.id.toString(),
            name = user.name,
            pronouns = user.pronouns,
            avatarURL = user.avatarUrl,
        )

        fun from(group: Group) = Recipient(
            stringId = group.contextId,
            name = group.name,
            userCount = group.membersCount,
        )
    }
}
