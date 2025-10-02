/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.canvas.espresso.mockcanvas.utils

import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import okhttp3.Request
import java.net.URLEncoder
import kotlin.reflect.KMutableProperty1

/**
 * A class which stores and provides statically-typed access to parsed URL path variables
 */
class PathVars {
    private val map = mutableMapOf<String, Any>()
    var userId: Long by map
    var accountId: Long by map
    var courseId: Long by map
    var assignmentId: Long by map
    var accountNotificationId: Long by map
    var pageUrl: String by map
    var pageId: Long by map
    var folderId: Long by map
    var fileId: Long by map
    var topicId: Long by map
    var entryId: Long by map
    var moduleId: Long by map
    var moduleItemId: Long by map
    var quizId: Long by map
    var questionId: Long by map
    var conversationId: Long by map
    var submissionId: Long by map
    var groupId: Long by map
    var sessionId: Long by map
    var annotationid: String by map
    var bookmarkId: Long by map
    var enrollmentId: Long by map
    var progressId: Long by map
    var plannerNoteId: Long by map
    var eventId: Long by map
    var studentId: Long by map
    var workflowState: String by map
    var thresholdId: Long by map
}

/**
 * A class for parsing and qualifying URL segments, used for path matching and variable extraction
 */
abstract class SegmentQualifier<T> {
    abstract val printName: String
    abstract fun matches(segmentName: String): Boolean
    abstract fun appendVars(segmentName: String, vars: PathVars, request: Request): T
}

/**
 * Represents a static path segment where [name] should exactly match the segment name
 */
class Segment(val name: String) : SegmentQualifier<String>() {
    override val printName = name
    override fun matches(segmentName: String) = segmentName == name
    override fun appendVars(segmentName: String, vars: PathVars, request: Request): String {
        // Do not append normal segments to path vars
        return segmentName
    }
}

/**
 * Created in order to match the string id of the annotation PUT endpoint:
 *
 * sessions/{sessionId}/annotations/{non-numeric, random StringId}
 *
 * See AnnotationsEndpoint for usage
 */
class AnnotationId : SegmentQualifier<String>() {
    override val printName = "generic string id"
    override fun matches(segmentName: String) = segmentName.isNotEmpty()
    override fun appendVars(segmentName: String, vars: PathVars, request: Request): String {
        PathVars::annotationid.set(vars, segmentName)
        return segmentName
    }
}

/**
 * Represents a variable path segment that can be parsed as a [Long] and stored as a [property][pathProperty] in [PathVars]
 */
class LongId(private val pathProperty: KMutableProperty1<PathVars, Long>) : SegmentQualifier<Long>() {
    override val printName = "{longId}"
    override fun matches(segmentName: String) = segmentName.toLongOrNull() != null
    override fun appendVars(segmentName: String, vars: PathVars, request: Request): Long {
        val id = segmentName.toLong()
        pathProperty.set(vars, id)
        return id
    }
}


/**
 * Represents a variable path segment that can be parsed as a [String] and stored as a [property][pathProperty] in [PathVars]
 */
class StringId(private val pathProperty: KMutableProperty1<PathVars, String>) : SegmentQualifier<String>() {
    override val printName = "{stringId}"
    override fun matches(segmentName: String) = segmentName.isNotEmpty()
    override fun appendVars(segmentName: String, vars: PathVars, request: Request): String {
        pathProperty.set(vars, URLEncoder.encode(segmentName, "UTF-8"))
        return segmentName
    }
}

/**
 * Represents a variable path segment that can be parsed as a user id - either a [Long] or "self" - and stored as [PathVars.userId]
 */
class UserId : SegmentQualifier<Long>() {
    override val printName = "{userId}"
    override fun matches(segmentName: String) = segmentName == "self" || segmentName.toLongOrNull() != null
    override fun appendVars(segmentName: String, vars: PathVars, request: Request): Long {
        val userId = if (segmentName == "self") {
            request.user!!.id
        } else {
            segmentName.toLong()
        }
        PathVars::userId.set(vars, userId)
        return userId
    }
}

/**
 * Represents a variable path segment that can be parsed as an account id - either a [Long] or "self" - and stored as [PathVars.accountId]
 */
class AccountId : SegmentQualifier<Long>() {
    override val printName = "{accountId}"
    override fun matches(segmentName: String) = segmentName == "self" || segmentName.toLongOrNull() != null
    override fun appendVars(segmentName: String, vars: PathVars, request: Request): Long {
        // NOTE: Only supporting one account for now
        val accountId = MockCanvas.data.account.id
        PathVars::accountId.set(vars, accountId)
        return accountId
    }
}
