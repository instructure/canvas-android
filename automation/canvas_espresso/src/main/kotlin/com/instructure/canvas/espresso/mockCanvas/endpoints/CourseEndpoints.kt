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
package com.instructure.canvas.espresso.mockCanvas.endpoints

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.instructure.canvas.espresso.mockCanvas.Endpoint
import com.instructure.canvas.espresso.mockCanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockCanvas.addReplyToDiscussion
import com.instructure.canvas.espresso.mockCanvas.endpoint
import com.instructure.canvas.espresso.mockCanvas.utils.*
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionParticipant
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.DiscussionTopicPermission
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Buffer
import retrofit2.http.Multipart
import java.time.Instant
import java.util.*

/**
 * Endpoint that can return a list of courses in which the request user is enrolled.
 *
 * ROUTES:
 * - `{courseId}` -> [CourseEndpoint]
 */
object CourseListEndpoint : Endpoint(
        LongId(PathVars::courseId) to CourseEndpoint,
        response = {
            GET {
                val user = request.user!!
                val courses = data.enrollments
                        .values
                        .filter { it.userId == user.id }
                        .map { data.courses[it.courseId]!! }
                request.successPaginatedResponse(courses)
            }
        }
)

/**
 * Endpoint that can return the course specified by [PathVars.courseId]. If the request user is not enrolled in
 * the requested course, an unauthorized response is returned.
 *
 * ROUTES:
 * - `tabs` -> [CourseTabsEndpoint]
 */
object CourseEndpoint : Endpoint(
        Segment("tabs") to CourseTabsEndpoint,
        Segment("assignments") to AssignmentIndexEndpoint,
        Segment("assignment_groups") to AssignmentGroupListEndpoint,
        Segment("external_tools") to ExternalToolsEndpoint,
        Segment("pages") to CoursePagesEndpoint,
        Segment("folders") to CourseFoldersEndpoint,
        Segment("discussion_topics") to CourseDiscussionTopicListEndpoint,
        response = {
            GET {
                val course = data.courses[pathVars.courseId]!!
                val userId = request.user!!.id
                if (data.enrollments.values.any { it.courseId == course.id && it.userId == userId }) {
                    request.successResponse(course)
                } else {
                    request.unauthorizedResponse()
                }
            }
        }
)

/**
 * Endpoint that returns the tabs for a course
 */
object CourseTabsEndpoint : Endpoint(response = {
    GET {
        val course = data.courses[pathVars.courseId]!!
        request.successResponse(data.courseTabs[course.id]!!) // returns a list of tabs
    }
})

/**
 * Endpoint that returns the pages for a course
 */
object CoursePagesEndpoint : Endpoint(
        LongId(PathVars::pageId) to CoursePageEndpoint,
        response = {
            GET {
                val pages = data.coursePages[pathVars.courseId]
                if (pages == null || pages.isEmpty()) {
                    request.unauthorizedResponse()
                } else {
                    request.successResponse(pages)
                }
            }
        })

/**
 * Endpoint that returns a specific page from a course
 */
object CoursePageEndpoint : Endpoint(response = {
    GET {
        val pages = data.coursePages[pathVars.courseId]
        val page = pages?.firstOrNull { it -> it.id == pathVars.pageId }
        if (page == null) {
            request.unauthorizedResponse()
        } else {
            request.successResponse(page)
        }
    }
})

/** Course folder support.  Right now we only support grabbing the root folder. */
object CourseFoldersEndpoint : Endpoint(
        Segment("root") to CourseRootFolderEndpoint
)

/** Course root folder support. */
object CourseRootFolderEndpoint : Endpoint(response = {
    GET {
        val folder = data.courseRootFolders[pathVars.courseId]
        if (folder != null) {
            request.successResponse(folder)
        } else {
            request.unauthorizedResponse()
        }
    }
})

/**
 * Endpoint that can return a list of discussion topic headers for a course
 *
 * GET to retrieve a list of DiscussionTopicHeaders for a course
 * POST to create a DiscussionTopicHeader for a course
 *
 * ROUTES:
 * - `{topicId}` -> [CourseDiscussionTopicEndpoint]
 */
object CourseDiscussionTopicListEndpoint : Endpoint(
        LongId(PathVars::topicId) to CourseDiscussionTopicEndpoint,
        response = {
            GET {
                Log.d("<--", "discussion_topics request: ${request}")
                val currentDiscussionTopics = data.courseDiscussionTopicHeaders[pathVars.courseId]
                        ?: listOf<DiscussionTopicHeader>()
                request.successResponse(currentDiscussionTopics)
            }

            POST {
                val jsonObject = grabJsonFromMultiPartBody(request.body()!!)
                //val json = jsonObject.toString()
                var newHeader = Gson().fromJson(jsonObject, DiscussionTopicHeader::class.java)
                var course = data.courses.values.find {it.id == pathVars.courseId}
                var user = request.user!!
                // Let's route through our manual discussion topic creation logic to minimize
                // code duplication.
                newHeader = data.addDiscussionTopicToCourse(
                        course = course!!,
                        user = user,
                        prePopulatedTopicHeader = newHeader,
                        topicTitle = newHeader.title!!, // or else it will be defaulted to something random
                        topicDescription = newHeader.message!!, // or else it will be defaulted to something random
                        allowRating = data.discussionRatingsEnabled,
                        allowReplies = data.discussionRepliesEnabled,
                        allowAttachments = data.discussionAttachmentsEnabled
                )
                Log.d("<--", "new discussion topic request body: $jsonObject")
                Log.d("<--", "new header: $newHeader")

                request.successResponse(newHeader)
            }

        }
)

/**
 * Endpoint that can return a specific discussion topic header for a specified course
 *
 * ROUTES:
 * - `view` -> anonymous endpoint to return a DiscussionTopic associated with the endpoint
 * - 'entries -> to [CourseDiscussionEntryListEndpoint]
 */
object CourseDiscussionTopicEndpoint : Endpoint (
        Segment("view") to endpoint {
            GET {
                Log.d("<--", "Discussion topic view get request: $request")
                val result = data.discussionTopics[pathVars.topicId]
                if(result != null) {
                    request.successResponse(result)
                }
                else {
                    request.unauthorizedResponse()
                }
            }
        },
        Segment("entries") to CourseDiscussionEntryListEndpoint,
        response = {
            GET {
                val topic = data.courseDiscussionTopicHeaders[pathVars.courseId]?.firstOrNull { item ->
                    item.id == pathVars.topicId
                }
                if(topic != null) {
                    request.successResponse(topic)
                }
                else {
                    request.unauthorizedResponse()
                }
            }
        }
)

/**
 * Endpoint that can add a discussion entry for a specified discussion.
 *
 * ROUTES:
 * - `{entryId}` -> [CourseDiscussionEntryEndpoint]
 */
object CourseDiscussionEntryListEndpoint : Endpoint(
        LongId(PathVars::entryId) to CourseDiscussionEntryEndpoint,
        response = {
            POST {
                val jsonObject = grabJsonFromMultiPartBody(request.body()!!)
                Log.d("<--", "Discussion topic entries post request: $request")
                Log.d("<--", "post body: $jsonObject")
                val discussionTopicHeader =
                        data.courseDiscussionTopicHeaders[pathVars.courseId]?.find { it.id == pathVars.topicId}
                if(discussionTopicHeader != null) {
                    // Let's route through our manual discussion reply creation logic to avoid
                    // code duplication.
                    var entry = data.addReplyToDiscussion(
                            topicHeader = discussionTopicHeader,
                            user = request.user!!,
                            replyMessage = jsonObject.get("message").asString )
                    request.successResponse(entry)
                }
                else {
                    request.unauthorizedResponse()
                }
            }

        }
)

/**
 * Endpoint that corresponds to a specific discussion entry.
 *
 * ROUTES:
 * - `read` -> anonymous endpoint to mark the discussion entry as read
 * - `rating` -> anonymous endpoint to make the endpoint as favorited or unfavorited by the user
 */
object CourseDiscussionEntryEndpoint : Endpoint(
        Segment("read") to endpoint {
            PUT {
                val topic = data.discussionTopics[pathVars.topicId]
                val topicHeader =
                        data.courseDiscussionTopicHeaders[pathVars.courseId]?.find {it.id == pathVars.topicId}
                val entry = topic?.views?.find { it.id == pathVars.entryId }

                if(topic == null || entry == null || topicHeader == null) {
                    request.unauthorizedResponse()
                }
                else {
                    entry.unread = false
                    topicHeader.unreadCount -= 1
                    topic.unreadEntries.remove(entry.id)
                    request.noContentResponse()
                }

            }
        },
        Segment("rating") to endpoint {
            POST {
                var ratingVal = request.url().queryParameter("rating")?.toInt()
                val topic = data.discussionTopics[pathVars.topicId]
                val entry = topic?.views?.find { it.id == pathVars.entryId }
                if (ratingVal == null || topic == null || entry == null) {
                    request.unauthorizedResponse()
                }
                else {
                    val currentCount = topic.entryRatings[entry.id]
                    if(ratingVal == 0) {
                        entry._hasRated = false
                        topic.entryRatings[entry.id] = (currentCount ?: 1) - 1
                    }
                    else {
                        entry._hasRated = true
                        topic.entryRatings[entry.id] = (currentCount ?: 0) + 1
                    }
                    request.noContentResponse()
                }
            }

        }
)

// The body might look something like this:
//    --39f93652-2013-49f1-85c5-c9373052de66
//    Content-Disposition: form-data; name="title"
//    Content-Transfer-Encoding: binary
//    Content-Type: multipart/form-data; charset=utf-8
//    Content-Length: 21
//
//    Discussion Topic Name
//    --39f93652-2013-49f1-85c5-c9373052de66
//    Content-Disposition: form-data; name="message"
//    Content-Transfer-Encoding: binary
//    Content-Type: multipart/form-data; charset=utf-8
//    Content-Length: 8
//
//    Awesome!
private fun grabJsonFromMultiPartBody(body: RequestBody) : JsonObject {
    val buffer = Buffer()
    body.writeTo(buffer)

    val result = JsonObject()

    while(grabJsonFieldFromBuffer(buffer,result)) { }
    return result
}

private fun grabJsonFieldFromBuffer(buffer: Buffer, jsonObject: JsonObject): Boolean {
    var line = buffer.readUtf8Line()
    if(line == null) return false
    if(!line.startsWith("--")) {
        return false
    }

    // Read a number of header lines followed by a blank line
    var fieldName: String? = null
    line = buffer.readUtf8Line()
    while(line != null && line.length > 0) {
        val nameRegex = """name=\"(\w+)\"""".toRegex()
        val match = nameRegex.find(line!!)
        if(match != null) {
            fieldName = match.groupValues[1]
            Log.d("<--","Found fieldName=$fieldName in line=$line")
        }
        line = buffer.readUtf8Line()
    }

    if(line == null) return false // Otherwise, it was blank

    // Grab the field content.  Assume for now that it is a single line.
    var fieldStringValue = buffer.readUtf8Line()
    if(fieldStringValue == null) return false

    // Let's attempt to add our field and value to jsonObject, correctly typed
    var fieldValue: Any? = null
    if(fieldStringValue.equals("true", ignoreCase = true) || fieldStringValue.equals("false", ignoreCase = true))
    {
        jsonObject.addProperty(fieldName,fieldStringValue.toBoolean())
    }
    else {
        fieldValue = fieldStringValue.toIntOrNull()
        if(fieldValue != null) {
            jsonObject.addProperty(fieldName, fieldValue as Int)
        }
        else{
            fieldValue = fieldStringValue.toDoubleOrNull()
            if(fieldValue != null) {
                jsonObject.addProperty(fieldName, fieldValue as Double)
            }
            else {
                jsonObject.addProperty(fieldName, fieldStringValue)
            }
        }
    }
    return true
}