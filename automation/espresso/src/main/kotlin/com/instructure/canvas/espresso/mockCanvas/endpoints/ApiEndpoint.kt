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
import com.instructure.canvas.espresso.mockCanvas.Endpoint
import com.instructure.canvas.espresso.mockCanvas.endpoint
import com.instructure.canvas.espresso.mockCanvas.utils.*
import com.instructure.canvasapi2.models.*
import okio.Buffer

/**
 * Base endpoint for the Canvas API
 *
 * ROUTES:
 * - `courses` -> [CourseListEndpoint]
 * - `users` -> [UserListEndpoint]
 * - `accounts` -> [AccountListEndpoint]
 * - `brand_variables` -> Returns account branding information
 * - `conversations` -> [ConversationListEndpoint]
 * - `dashboard/dashboard_cards` -> [DashboardCardsEndpoint]
 * - `folders` -> [FolderListEndpoint]
 * - `quiz_submissions/:submission_id/questions` -> anonymous endpoint for quiz questions
 * - `groups` -> [GroupsEndpoint]
 * - `search` -> [SearchEndpoint]
 */
object ApiEndpoint : Endpoint(
    Segment("courses") to CourseListEndpoint,
    Segment("users") to UserListEndpoint,
    Segment("accounts") to AccountListEndpoint,
    Segment("brand_variables") to endpoint { GET { request.successResponse(data.brandVariables) } },
    Segment("conversations") to ConversationListEndpoint,
    Segment("dashboard") to endpoint(
        Segment("dashboard_cards") to DashboardCardsEndpoint
    ),
    Segment("folders") to FolderListEndpoint,
    Segment("search") to SearchEndpoint,
    Segment("canvadoc_session") to CanvadocRedirectEndpoint,
    Segment("files") to FileUrlEndpoint,
    Segment("quiz_submissions") to endpoint(
            LongId(PathVars::submissionId) to endpoint (
                    Segment("questions") to endpoint (
                            configure = {
                                GET {
                                    val submissionQuestions = data.quizSubmissionQuestions[pathVars.submissionId]
                                            ?: listOf<QuizSubmissionQuestion>()
                                    val response = QuizSubmissionQuestionResponse(quizSubmissionQuestions = submissionQuestions)
                                    request.successResponse(response)
                                }
                                POST {
                                    // More or less punting on this for now.  The unauthorized response doesn't seem
                                    // to hurt us.
                                    val buffer = Buffer()
                                    request.body!!.writeTo(buffer)
                                    val body = buffer.readUtf8()
                                    Log.d("submissionQuestions", "submission question post body: $body")
                                    //val jsonObject = grabJsonFromMultiPartBody(request.body()!!)
                                    //Log.d("submissionQuestions", "submission question post body: $jsonObject")
                                    request.unauthorizedResponse()
                                }
                            }
                    )
            )
    ),
    Segment("groups") to GroupsEndpoint,
    Segment("calendar_events") to Endpoint(
            Segment("") to Endpoint( // Our call has an extra "/" at the end, thus the blank segment
                response = {
                    GET {
                        val contextCodes = request.url.queryParameter("context_codes[]")
                        // TODO: Assumes that the context prefix is "course".  We may need to support other possibilities.
                        val courseId = contextCodes?.substringAfter("_")?.toLong()
                        val eventType = request.url.queryParameter("type") ?: "event"
                        if(courseId != null) {
                            val events = when(eventType) {
                                "assignment" -> data
                                        .assignments
                                        .values
                                        .filter {a -> a.courseId == courseId}
                                        .map {a -> ScheduleItem(title = a.name, description = a.description, startAt = a.dueAt, assignment = a)}

                                // default handler assumes "event" event type
                                else -> data.courseCalendarEvents[courseId] ?: mutableListOf<ScheduleItem>()
                            }
                            request.successResponse(events)
                        }
                        else {
                            request.unauthorizedResponse()
                        }
                    }
                }
            )
    ),
    Segment("announcements") to Endpoint(
        response = {
            GET {
                val contextCodes = request.url.queryParameter("context_codes[]")
                val courseId = contextCodes?.substringAfter("_")?.toLong()

                val firstAnnouncement = data.courseDiscussionTopicHeaders[courseId]
                    ?.filter { it.announcement }
                    ?.firstOrNull()

                val result = firstAnnouncement?.let { listOf(it) } ?: emptyList()
                request.successResponse(result)
            }
        }
    ),
    Segment("external_tools") to Endpoint(
        Segment("visible_course_nav_tools") to Endpoint {
            GET {
                val contextCodes = request.url.queryParameterValues("context_codes[]")
                val courseIds = contextCodes.map { it?.substringAfter("_")?.toLong() }

                val ltiToolsForCourse = data.ltiTools.filter {
                    courseIds.contains(it.contextId ?: 0)
                }
                request.successResponse(ltiToolsForCourse)
            }
        }
    ),
    Segment("planner") to Endpoint(
        Segment("overrides") to Endpoint {
            POST {
                val plannerOverride = getJsonFromRequestBody<PlannerOverride>(request.body)
                request.successResponse(plannerOverride!!)
            }
        }
    )
)

object FileUrlEndpoint : Endpoint(
    LongId(PathVars::fileId) to endpoint(
        configure = {
            GET {
                val file = data.folderFiles.values.flatten().find { it.id == pathVars.fileId }
                if(file != null) {
                    request.successResponse(file)
                } else {
                    request.unauthorizedResponse()
                }
            }
        }
    )
)

object CanvadocRedirectEndpoint : Endpoint(
    response = {
        GET { // Throws not initialized exception
            request.successRedirectWithHeader("Location", data.canvadocRedirectUrl)
        }
    }
)

// Logic for /api/v1/groups.
// Does not, for now, handle listing of groups, but rather access to a specific group
// (via the groupId pathvar).
object GroupsEndpoint : Endpoint (
        LongId(PathVars::groupId) to endpoint (
                Segment("tabs") to endpoint ( // group tabs
                        configure = {
                            GET {
                                request.successResponse(data.groupTabs[pathVars.groupId] ?: listOf<Tab>())
                            }
                        }
                ),

                Segment("discussion_topics") to endpoint ( // group discussion topics
                        LongId(PathVars::topicId) to endpoint(
                                Segment("view") to endpoint (
                                        configure = {
                                            GET {
                                                val topic = data
                                                        .groupDiscussionTopicHeaders[pathVars.groupId]
                                                        ?.find {it.id == pathVars.topicId}

                                                if(topic != null) {
                                                    request.successResponse(topic)
                                                }
                                                else {
                                                    request.unauthorizedResponse()
                                                }
                                            }
                                        }
                                )
                        ),
                        configure = {
                            GET {
                                // TODO: Merge this logic with course discussion_topics logic
                                val announcementsOnly = request.url.queryParameter("only_announcements")?.equals("1") ?: false
                                var topics = data.groupDiscussionTopicHeaders[pathVars.groupId]
                                if(topics != null) {
                                    if(announcementsOnly) {
                                        // return only announcements
                                        topics = topics!!.filter {it.announcement}.toMutableList()
                                    }
                                    else {
                                        // return only non-announcement discussions
                                        topics = topics!!.filter {!it.announcement}.toMutableList()
                                    }
                                    request.successResponse(topics!!)
                                }
                                else {
                                    request.unauthorizedResponse()
                                }
                            }
                        }
                ),

                Segment("pages") to endpoint (// group pages
                        StringId(PathVars::pageUrl) to endpoint (
                                configure = {
                                    GET {
                                        val page = data.groupPages[pathVars.groupId]?.find {it.url == pathVars.pageUrl}
                                        if(page != null) {
                                            request.successResponse(page)
                                        }
                                        else {
                                            request.unauthorizedResponse()
                                        }
                                    }
                                }
                        ),

                        configure = {
                            GET {
                                val pages = data.groupPages[pathVars.groupId]
                                if(pages != null) {
                                    request.successResponse(pages)
                                }
                                else {
                                    request.unauthorizedResponse()
                                }
                            }
                        }
                ),

                Segment("folders") to endpoint(// groups folders
                        Segment("root") to endpoint (
                                configure = {
                                    GET {
                                        val rootFolder = data.groupRootFolders[pathVars.groupId]
                                        if(rootFolder != null) {
                                            request.successResponse(rootFolder)
                                        }
                                        else {
                                            request.unauthorizedResponse()
                                        }
                                    }
                                }
                        )

                ),

                Segment("users") to endpoint ( // group's users
                    configure = {
                        GET {
                            val groupId = pathVars.groupId
                            val group = data.groups[groupId]
                            if (group != null && group.users.count() > 0) {
                                request.successResponse(group.users)
                            } else {
                                request.unauthorizedResponse()
                            }
                        }
                    }
                ),

                configure = {
                    GET { // Get general group info for a specific group
                        val group = data.groups[pathVars.groupId]
                        if(group != null) {
                            request.successResponse(group)
                        }
                        else {
                            request.unauthorizedResponse()
                        }
                    }
                }
        )

)
