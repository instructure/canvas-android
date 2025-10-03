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
package com.instructure.canvas.espresso.mockcanvas.endpoints

import com.instructure.canvas.espresso.mockcanvas.Endpoint
import com.instructure.canvas.espresso.mockcanvas.endpoint
import com.instructure.canvas.espresso.mockcanvas.utils.AccountId
import com.instructure.canvas.espresso.mockcanvas.utils.LongId
import com.instructure.canvas.espresso.mockcanvas.utils.PathVars
import com.instructure.canvas.espresso.mockcanvas.utils.Segment
import com.instructure.canvas.espresso.mockcanvas.utils.UserId
import com.instructure.canvas.espresso.mockcanvas.utils.successPaginatedResponse
import com.instructure.canvas.espresso.mockcanvas.utils.successResponse
import com.instructure.canvas.espresso.mockcanvas.utils.unauthorizedResponse
import com.instructure.canvasapi2.models.Account
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.BecomeUserPermission
import com.instructure.canvasapi2.models.HelpLink
import com.instructure.canvasapi2.models.HelpLinks
import com.instructure.canvasapi2.models.LaunchDefinition

/**
 * Endpoint that can return a list of [Account]s. We currently assume that only one account is supported at a time and that
 * no users are admins (only admins can list account), so this will always return an empty list.
 *
 * ROUTES:
 * - `{accountId}` -> [AccountEndpoint]
 */
object AccountListEndpoint : Endpoint(
    AccountId() to AccountEndpoint,
    response = {
        // NOTE: Only supporting one account for now and we'll assume no users are admins, so return an empty list
        GET { request.successPaginatedResponse(emptyList<Account>()) }
    }
)

/**
 * Endpoint that can return the [Account] specified by [PathVars.accountId]. We currently assume that no users are
 * admins (only admins can view accounts) so this will always return an unauthorized response.
 *
 * ROUTES:
 * - `users/{userId}/account_notifications` -> [AccountNotificationListEndpoint]
 * - `lti_apps/launch_notifications` -> [LaunchDefinitionsEndpoint]
 */
object AccountEndpoint : Endpoint(
    Segment("users") to endpoint(
        UserId() to endpoint(
            Segment("account_notifications") to AccountNotificationListEndpoint
        )
    ),
    Segment("lti_apps") to endpoint(
        Segment("launch_definitions") to LaunchDefinitionsEndpoint
    ),
    Segment("permissions") to AccountPermissionsEndpoint,
    Segment("help_links") to HelpLinksEndpoint,
    Segment("terms_of_service") to TermsOfServiceEndpoint,
    response = {
        // NOTE: Only supporting one account for now and we'll assume no users are admins, so return a 401
        GET { request.unauthorizedResponse() }
    }
)

object TermsOfServiceEndpoint : Endpoint(
    response = {
        GET {
            request.successResponse(data.accountTermsOfService)
        }
    }
)

object AccountPermissionsEndpoint : Endpoint(
    response = {
        GET {
            request.successResponse(BecomeUserPermission(false))
        }
    }
)

/**
 * Endpoint that can return a list of [LaunchDefinition]s
 */
object LaunchDefinitionsEndpoint : Endpoint(response = {
    GET { request.successPaginatedResponse(data.launchDefinitions.values.toList()) }
})

/**
 * Endpoint that can return a list of [AccountNotification]s
 *
 * ROUTES:
 * - `{accountNotificationId}` -> [AccountNotificationEndpoint]
 */
object AccountNotificationListEndpoint : Endpoint(
    LongId(PathVars::accountNotificationId) to AccountNotificationEndpoint,
    response = {
        GET { request.successPaginatedResponse(data.accountNotifications.values.toList()) }
    }
)

/**
 * Endpoint that can return or delete the [AccountNotification] specified by [PathVars.accountNotificationId]
 */
object AccountNotificationEndpoint : Endpoint(response = {
    GET { request.successResponse(data.accountNotifications[pathVars.accountNotificationId]!!) }
    DELETE {
        val notification = data.accountNotifications[pathVars.accountNotificationId]!!
        data.accountNotifications.remove(pathVars.accountNotificationId)
        request.successResponse(notification)
    }
})

/**
 * Endpoint that returns help links.
 * I populated the links based on the result of a "real" help-links call.
 */
object HelpLinksEndpoint : Endpoint( response = {
    GET {
        val result = HelpLinks(
                customHelpLinks = listOf(
                        HelpLink(
                                id = "instructor_question",
                                type = "default",
                                availableTo = listOf("student"),
                                url = "#teacher_feedback",
                                text = "Ask Your Instructor a Question",
                                subtext = "Questions are submitted to your instructor"
                        ),
                        HelpLink(
                                id="search_the_canvas_guides",
                                type="default",
                                availableTo=listOf("user", "student", "teacher", "admin", "observer", "unenrolled"),
                                url="https://community.canvaslms.com/community/answers/guides/",
                                text="Search the Canvas Guides",
                                subtext="Find answers to common questions"
                        ),
                        HelpLink (
                                id="report_a_problem",
                                type="default",
                                availableTo=listOf("user", "student", "teacher", "admin", "observer", "unenrolled"),
                                url="#create_ticket",
                                text="Report a Problem",
                                subtext="If Canvas misbehaves, tell us about it"
                        ),
                        HelpLink(
                                id="training_services_portal",
                                type="default",
                                availableTo=listOf("teacher", "admin", "unenrolled"),
                                url="https://training-portal-prod-pdx.insproserv.net?canvas_domain=jhoag.instructure.com&sf_id=",
                                text="Training Services Portal",
                                subtext="Access Canvas training videos and courses"
                        ),
                        HelpLink(
                                id="ask_community",
                                type="default",
                                availableTo=listOf("teacher", "admin"),
                                url="https://community.canvaslms.com/community/answers",
                                text="Ask the Community",
                                subtext="Get help from a Canvas expert"
                        ),
                        HelpLink(
                                id="submit_feature_idea",
                                type="default",
                                availableTo=listOf("user", "student", "teacher", "admin"),
                                url="https://community.canvaslms.com/community/ideas/feature-ideas",
                                text="Submit a Feature Idea", subtext="Have an idea to improve Canvas?"
                        )
                ),
                defaultHelpLinks = listOf(
                        HelpLink(
                                id="instructor_question",
                                type="default",
                                availableTo=listOf("student"),
                                url="#teacher_feedback",
                                text="Ask Your Instructor a Question",
                                subtext="Questions are submitted to your instructor"
                        ),
                        HelpLink(
                                id="search_the_canvas_guides",
                                type="default",
                                availableTo=listOf("user", "student", "teacher", "admin", "observer", "unenrolled"),
                                url="https://community.canvaslms.com/community/answers/guides/",
                                text="Search the Canvas Guides",
                                subtext="Find answers to common questions"
                        ),
                        HelpLink(
                                id="report_a_problem",
                                type="default",
                                availableTo=listOf("user", "student", "teacher", "admin", "observer", "unenrolled"),
                                url="#create_ticket",
                                text="Report a Problem",
                                subtext="If Canvas misbehaves, tell us about it"
                        ),
                        HelpLink(
                                id="training_services_portal",
                                type="default",
                                availableTo=listOf("teacher", "admin", "unenrolled"),
                                url="https://training-portal-prod-pdx.insproserv.net?canvas_domain=jhoag.instructure.com&sf_id=",
                                text="Training Services Portal",
                                subtext="Access Canvas training videos and courses"
                        ),
                        HelpLink(
                                id="ask_community",
                                type="default",
                                availableTo=listOf("teacher", "admin"),
                                url="https://community.canvaslms.com/community/answers",
                                text="Ask the Community",
                                subtext="Get help from a Canvas expert"
                        ),
                        HelpLink(
                                id="submit_feature_idea",
                                type="default",
                                availableTo=listOf("user", "student", "teacher", "admin"),
                                url="https://community.canvaslms.com/community/ideas/feature-ideas",
                                text="Submit a Feature Idea",
                                subtext="Have an idea to improve Canvas?"
                        )
                )
        )

        request.successResponse(result)
    }
})
