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
import com.instructure.canvas.espresso.mockCanvas.endpoint
import com.instructure.canvas.espresso.mockCanvas.utils.*
import com.instructure.canvasapi2.models.Favorite
import com.instructure.canvasapi2.utils.pageview.PandataInfo
import okhttp3.ResponseBody

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
    Segment("communication_channels") to UserCommunicationChannelsEndpoint
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
        Segment("courses") to UserFavoriteCourseListEndpoint
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
        val states = request.url().queryParameterValues("state[]")
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
        request.url().queryParameter("hide_dashcard_color_overlays")?.let {
            settings = settings.copy(hideDashCardColorOverlays = it.equals("true", true))
        }
        data.userSettings[pathVars.userId] = settings
        request.successResponse(settings)
    }
})
