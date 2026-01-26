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

import com.instructure.canvasapi2.utils.ApiPrefs
import java.util.Date
import java.util.Locale

abstract class CanvasContext : CanvasModel<CanvasContext>() {
    abstract val name: String?
    abstract val type: Type
    abstract override val id: Long

    var permissions: CanvasContextPermission? = null

    override val comparisonDate: Date? get() = null
    override val comparisonString: String? get() = name

    /**
     * For courses, returns the course code.
     * For everything else, returns the Name;
     */

    val secondaryName: String?
        get() {
            var secondaryName: String? = name
            if (type == CanvasContext.Type.COURSE) {
                secondaryName = (this as Course).courseCode
            }
            return secondaryName
        }

    /**
     * @returns group_:id or course_:id
     */
    val contextId: String
        get() {
            val prefix: String = when (type) {
                Type.COURSE -> "course"
                Type.GROUP -> "group"
                Type.USER -> "user"
                Type.SECTION -> TODO()
                Type.UNKNOWN -> "unknown"
            }

            return prefix + "_" + id
        }

    /**
     * Make sure they have the same type and the same ID.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (!super.equals(other)) return false

        val that = other as CanvasContext

        return !(type != that.type || id != that.id)
    }

    enum class Type(val apiString: String) {
        GROUP("groups"),
        COURSE("courses"),
        USER("users"),
        SECTION("sections"),
        UNKNOWN("unknown");

        companion object {
            fun isGroup(canvasContext: CanvasContext?): Boolean = GROUP == canvasContext?.type
            fun isCourse(canvasContext: CanvasContext?): Boolean = COURSE == canvasContext?.type
            fun isUser(canvasContext: CanvasContext?): Boolean = USER == canvasContext?.type
            fun isUnknown(canvasContext: CanvasContext?): Boolean = UNKNOWN == canvasContext?.type
            fun isSection(canvasContext: CanvasContext?): Boolean = SECTION == canvasContext?.type
        }
    }

    fun canCreateDiscussion(): Boolean = permissions != null && permissions!!.canCreateDiscussionTopic

    /**
     * Used for Cache Filenames in the API.
     */
    fun toAPIString(): String {
        val idString = if (type == Type.USER && id == 0L) {
            "self"
        } else id.toString()
        return "/${type.apiString}/$idString"
    }

    fun apiContext(): String = if (type == Type.COURSE) "courses" else "groups"

    companion object {

        fun makeContextId(type: Type, id: Long): String = type.name.lowercase(Locale.getDefault()) + "_" + id

        /**
         * Returns a generic CanvasContext based on the provided context code.
         * @param contextCode Context code string, e.g. "course_1"
         * @return A generic CanvasContext, or null if the provided contextCode is invalid
         */
        fun fromContextCode(contextCode: String?, name: String? = ""): CanvasContext? {
            if (contextCode.isNullOrBlank() || name == null) return null

            val codeParts = contextCode.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (codeParts.size != 2) return null

            val type = when (codeParts[0]) {
                "course" -> Type.COURSE
                "group" -> Type.GROUP
                "user" -> Type.USER
                else -> Type.UNKNOWN
            }

            val id: Long
            try {
                id = codeParts[1].toLong()
            } catch (e: NumberFormatException) {
                return null
            }

            return getGenericContext(type, id, name)
        }

        fun getApiContext(canvasContext: CanvasContext): String = if (canvasContext.type == Type.COURSE) "courses" else "groups"

        fun getGenericContext(type: Type, id: Long = -1L, name: String = ""): CanvasContext =
                when (type) {
                    Type.USER -> User(id = id, name = name)
                    Type.COURSE -> Course(id = id, name = name)
                    Type.GROUP -> Group(id = id, name = name)
                    Type.SECTION -> Section(id = id, name = name)
                    Type.UNKNOWN -> Unknown()
                }

        @JvmOverloads
        fun emptyCourseContext(id: Long = 0L): CanvasContext = getGenericContext(Type.COURSE, id, "")
        fun emptyGroupContext(id: Long = 0L): CanvasContext = getGenericContext(Type.GROUP, id, "")
        fun defaultCanvasContext(): CanvasContext = getGenericContext(Type.UNKNOWN, 0, "")
        fun emptyUserContext(): CanvasContext = getGenericContext(Type.USER, 0, "")
        fun currentUserContext(user: User): CanvasContext = getGenericContext(Type.USER, user.id, user.name)
    }
}

/**
 * Creates a base URL for this CanvasContext using the current domain and context type/id
 */
fun CanvasContext.toBaseUrl(): String {
    return "${ApiPrefs.fullDomain}/${type.apiString}/$id"
}
