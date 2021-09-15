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

import com.instructure.canvas.espresso.mockCanvas.Endpoint
import com.instructure.canvas.espresso.mockCanvas.addFileToFolder
import com.instructure.canvas.espresso.mockCanvas.endpoint
import com.instructure.canvas.espresso.mockCanvas.utils.*
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.pageview.PandataInfo
import okio.Buffer
import java.nio.charset.Charset
import java.util.*

/**
 * ROUTES:
 * - `{userId}` -> [UserEndpoint]
 */
object UserListEndpoint : Endpoint(
    UserId() to UserEndpoint
)

/**
 * ROUTES:
 * - `profile` -> [UserProfileEndpoint]
 * - `colors` -> [UserColorsEndpoint]
 * - `pandata_events_token` -> Returns empty Pandata token info
 * - `settings` -> [UserSettingsEndpoint]
 * - `groups` -> [UserGroupListEndpoint]
 * - `enrollments` -> [UserEnrollmentEndpoint]
 */
object UserEndpoint : Endpoint(
    Segment("profile") to UserProfileEndpoint,
    Segment("colors") to UserColorsEndpoint,
    Segment("pandata_events_token") to endpoint {
        POST { request.successResponse(PandataInfo("", "", "", Double.MAX_VALUE)) }
    },
    Segment("settings") to UserSettingsEndpoint,
    Segment("groups") to UserGroupListEndpoint,
    Segment("enrollments") to UserEnrollmentEndpoint,
    Segment("favorites") to UserFavoritesEndpoint,
    Segment("communication_channels") to UserCommunicationChannelsEndpoint,
    Segment("folders") to UserFoldersEndpoint,
    Segment("files") to UserFilesEndpoint,
    Segment("todo") to UserTodoEndpoint,
    Segment("observer_pairing_codes") to UserPairingCodeEndpoint,
    Segment("activity_stream") to UserActivityStreamEndpoint,
    Segment("planner") to Endpoint(
        Segment("items") to Endpoint(
            response = {
                GET {
                    val userId = pathVars.userId
                    val userCourseIds = data.enrollments.values.filter {it.userId == userId}.map {it -> it.courseId}

                    val todos = data.todos.filter { it.userId == userId }

                    // Gather our assignments
                    // Currently we assume all the assignments are due today
                    val plannerItemsList = data.assignments.values
                        .filter { userCourseIds.contains(it.courseId) }
                        .map {
                            val plannableDate = it.dueDate ?: Date()
                            val plannable = Plannable(it.id, it.name
                                ?: "", it.courseId, null, userId, null, it.dueDate, it.id, null)
                            PlannerItem(it.courseId, null, userId, null, null, PlannableType.ASSIGNMENT, plannable, plannableDate, null, SubmissionState(), false)
                        }
                        .plus(todos)

                    request.successResponse(plannerItemsList)
                }
            }
        )
    ),
    Segment("missing_submissions") to Endpoint(
        response = {
            GET {
                val userId = pathVars.userId
                val userCourseIds = data.enrollments.values.filter {it.userId == userId}.map {it -> it.courseId}

                // Currently we assume all the assignments are missing
                val missingAssignmentsList = data.assignments.values
                    .filter { userCourseIds.contains(it.courseId) }

                request.successResponse(missingAssignmentsList)
            }
        }
    ),
    Segment("bookmarks") to UserBookmarksEndpoint,
    response = {
        GET {
            val userId = pathVars.userId
            val user = data.users[userId]
            if(user != null) {
                request.successResponse(user)
            }
            else {
                request.unauthorizedResponse()
            }
        }

        PUT {
            val userId = pathVars.userId
            val user = data.users[userId]
            val newShortName = request.url.queryParameter("user[short_name]")
            if(user != null && newShortName != null) {
                // Replace the user object with a clone that has the new short name
                val newUser = user.copy(shortName = newShortName)
                data.users[userId] = newUser

                // And return the altered user
                request.successResponse(newUser)
            }
            else {
                request.unauthorizedResponse()
            }
        }
    }
)

/**
 * Endpoint for user todo download
 */
object UserTodoEndpoint : Endpoint (
        response = {
            GET {
                val toDoList = mutableListOf<ToDo>()
                val userid = pathVars.userId
                val userCourseIds = data.enrollments.values.filter {it.userId == userid}.map {it -> it.courseId}

                // Gather our assignments, assuming that all are "to-do"
                data.assignments.values.filter {userCourseIds.contains(it.courseId)}.forEach {
                    toDoList.add(ToDo(
                            type = ToDo.Type.UPCOMING_ASSIGNMENT,
                            courseId =  it.courseId,
                            assignment = it
                    ))
                }

                // Since we are now creating "shadow assignments" along with each quiz,
                // we will no longer return quiz info explicitly for this call.
//                // Gather our quizzes, assuming that all are "to-do"
//                userCourseIds.forEach {courseId ->
//                    data.courseQuizzes[courseId]?.forEach { quiz ->
//                        toDoList.add(ToDo(
//                                type = ToDo.Type.UPCOMING_ASSIGNMENT,
//                                courseId = courseId,
//                                quiz = quiz
//                        ))
//                    }
//                }

                // TODO: Be more picky about which assignments and quizzes are "to-do"

                request.successResponse(toDoList)
            }
        }
)
/**
 * Endpoint for user file upload
 */
object UserFilesEndpoint : Endpoint(
        response = {
            POST {
                val fileName = request.url.queryParameter("name")!!
                val fileType = request.url.queryParameter("content_type")!!
                val fileParentFolder = request.url.queryParameter("parent_folder_id")?.toLong()!!

                // Assumes a binary payload... May not always be valid.
                // We only hit this logic when uploading files from the global file list page,
                // not when uploading course assignments.
                val buffer = Buffer()
                request.body?.writeTo(buffer)
                // This is a little weak, and possibly wrong for image files.  But since
                // we do not actually check the content of image files, we should be OK.
                val content = buffer.readByteArray().toString(Charset.defaultCharset()) // Should be utf-8

                data.addFileToFolder(folderId = fileParentFolder, displayName = fileName, fileContent = content, contentType = fileType )

                // Really bogus.  Works for the current user-file-upload tests, but we may need to revisit
                // this in the future to make it more like SubmissionUserEndpoint/Files/POST
                request.successResponse(FileUploadParams())
            }
        }
)

/**
 * Endpoint for user folders/files
 */
object UserFoldersEndpoint : Endpoint(
        Segment("root") to endpoint(
                configure = {
                    GET {
                        val userId = pathVars.userId
                        val folder = data.fileFolders.values.find { it.contextType.equals("user") && it.contextId == userId }
                        if(folder != null) {
                            request.successResponse(folder)
                        }
                        else {
                            request.unauthorizedResponse()
                        }
                    }
                }
        )
)

/**
 * Endpoint for setting push notif channels, we only check for a non-null body and a 200.
 */
object UserCommunicationChannelsEndpoint : Endpoint(
    response = {
        POST {
            request.successResponse("hodor")
        }
    }
)

/**
 * Endpoint (midpoint?) for user favorites
 *
 * ROUTES:
 * - `courses` -> [UserFavoriteCourseListEndpoint]
 */
object UserFavoritesEndpoint : Endpoint (
    Segment("courses") to UserFavoriteCourseListEndpoint,
    Segment("groups") to UserFavoriteGroupListEndpoint
)

object UserFavoriteGroupListEndpoint : Endpoint(
    response = {
        GET {
            request.successResponse(listOf<Group>())
        }
    }
)

/**
 * Endpoint for user favorite courses
 *
 * ROUTES:
 * - `{courseId}` -> UserFavoriteCourseEndpoint
 */
object UserFavoriteCourseListEndpoint : Endpoint (
        LongId(PathVars::courseId) to UserFavoriteCourseEndpoint,
        response = {
            GET {
                val user = request.user!!
                val courses = data.enrollments
                        .values
                        .filter { it.userId == user.id }
                        .map { data.courses[it.courseId]!! }
                        .filter { it.isFavorite }
                request.successPaginatedResponse(courses)

            }
        }
)

/**
 * Endpoint that handles setting / unsetting "favoriteness" of a course
 * POST will favorite a course, DELETE will unfavorite a course
 */
object UserFavoriteCourseEndpoint : Endpoint ( response = {

    POST {
        val course = data.courses[pathVars.courseId]!!
        val userId = pathVars.userId
        if(data.enrollments.values.any{ it.courseId == course.id && it.userId == userId}) {
            course.isFavorite = true
            request.successResponse(Favorite())
        }
        else {
            request.unauthorizedResponse()
        }
    }

    DELETE {
        val course = data.courses[pathVars.courseId]!!
        val userId = pathVars.userId
        if(data.enrollments.values.any{ it.courseId == course.id && it.userId == userId}) {
            course.isFavorite = false
            request.successResponse(Favorite())
        }
        else {
            request.unauthorizedResponse()
        }
    }

})

/**
 * Endpoint that can return a list of enrollments for the user specified by [PathVars.userId]
 */
object UserEnrollmentEndpoint : Endpoint(response = {
    GET {
        val states = request.url.queryParameterValues("state[]")
        var enrollments = data.enrollments.values.filter { it.userId == pathVars.userId }
        if (states.isNotEmpty()) {
            enrollments = enrollments.filter { it.enrollmentState in states }
        }
        request.successPaginatedResponse(enrollments)
    }
})

/**
 * Endpoint that can return a list of groups. Currently this returns all groups and does not account for the
 * request user or and query parameters.
 */
object UserGroupListEndpoint : Endpoint(response = {
    GET { request.successPaginatedResponse(data.groups.values.toList()) }
})

/**
 * Endpoint that can return the user profile of user specified by [PathVars.userId]
 */
object UserProfileEndpoint : Endpoint(response = {
    GET { request.successResponse(data.users[pathVars.userId]!!) }
})

/**
 * Endpoint that can return user colors for user specified by [PathVars.userId]
 */
object UserColorsEndpoint : Endpoint(response = {
    GET { request.successResponse(data.userColors.getValue(pathVars.userId)) }
})

/**
 * Endpoint that can return or modify user settings for user specified by [PathVars.userId]
 */
object UserSettingsEndpoint : Endpoint(response = {
    GET { request.successResponse(data.userSettings[pathVars.userId]!!) }
    PUT {
        var settings = data.userSettings[pathVars.userId]!!
        request.url.queryParameter("hide_dashcard_color_overlays")?.let {
            settings = settings.copy(hideDashCardColorOverlays = it.equals("true", true))
        }
        data.userSettings[pathVars.userId] = settings
        request.successResponse(settings)
    }
})

var pairingCodeCount = 0
/**
 * Endpoint that can return user generated pairing codes for a parent observer
 * Increments an index, so different codes are returned each request
 */
object UserPairingCodeEndpoint : Endpoint(response = {
    POST {
        request.successResponse(PairingCode((++pairingCodeCount).toString()))
    }
})

/**
 * Endpoint that returns a user's activity stream
 */
object UserActivityStreamEndpoint : Endpoint( response = {
    GET {
        val userId = pathVars.userId
        val response = data.streamItems[userId] ?: mutableListOf<StreamItem>()
        request.successResponse(response)
    }
})

object UserBookmarksEndpoint : Endpoint(

        // Logic to change info for a specific bookmark
        LongId(PathVars::bookmarkId) to Endpoint(response = {
            PUT {
                // path vars
                val bookmarkId = pathVars.bookmarkId
                val userId = pathVars.userId

                // query vars
                val name = request.url.queryParameter("name")
                val url = request.url.queryParameter("url")
                val position = request.url.queryParameter("position")

                val bookmark = data.bookmarks[userId]?.firstOrNull {b -> b.id == bookmarkId}
                if(bookmark != null) {
                    // If the bookmark actually exists, modify it and return the modified bookmark
                    val bookmarkCopy = bookmark.copy(
                            id = bookmark.id,
                            name = name ?: bookmark.name,
                            url = url ?: bookmark.url,
                            position = position?.toInt() ?: bookmark.position
                    )

                    data.bookmarks[userId]!!.remove(bookmark)
                    data.bookmarks[userId]!!.add(bookmarkCopy)

                    request.successResponse(bookmarkCopy)
                }
                else {
                    // Bookmark with specified id does not exist
                    request.unauthorizedResponse()
                }
            }
        }),

        // Logic to create a new bookmark or grab a list of bookmarks
        response = {

            POST {
                val name = request.url.queryParameter("name")
                val url = request.url.queryParameter("url")
                val position = request.url.queryParameter("position")?.toInt() ?: 0

                val bookmark = Bookmark(
                        id = data.newItemId(),
                        name = name,
                        url = url,
                        position = position
                )

                val userId = pathVars.userId
                var list = data.bookmarks[userId]
                if (list == null) {
                    list = mutableListOf<Bookmark>()
                    data.bookmarks[userId] = list!!
                }
                list!!.add(bookmark)

                request.successResponse(bookmark)
            }

            GET {
                val userId = pathVars.userId
                val response = data.bookmarks[userId] ?: mutableListOf<Bookmark>()
                request.successResponse(response)
            }
        }
)
