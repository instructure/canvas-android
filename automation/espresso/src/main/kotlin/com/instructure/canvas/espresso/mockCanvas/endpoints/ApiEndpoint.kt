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
import com.instructure.canvas.espresso.mockCanvas.Endpoint
import com.instructure.canvas.espresso.mockCanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockCanvas.addPlannable
import com.instructure.canvas.espresso.mockCanvas.endpoint
import com.instructure.canvas.espresso.mockCanvas.utils.DontCareAuthModel
import com.instructure.canvas.espresso.mockCanvas.utils.LongId
import com.instructure.canvas.espresso.mockCanvas.utils.PathVars
import com.instructure.canvas.espresso.mockCanvas.utils.Segment
import com.instructure.canvas.espresso.mockCanvas.utils.StringId
import com.instructure.canvas.espresso.mockCanvas.utils.getJsonFromRequestBody
import com.instructure.canvas.espresso.mockCanvas.utils.grabJsonFromMultiPartBody
import com.instructure.canvas.espresso.mockCanvas.utils.successRedirectWithHeader
import com.instructure.canvas.espresso.mockCanvas.utils.successResponse
import com.instructure.canvas.espresso.mockCanvas.utils.unauthorizedResponse
import com.instructure.canvas.espresso.mockCanvas.utils.user
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.models.Progress
import com.instructure.canvasapi2.models.QuizSubmissionQuestion
import com.instructure.canvasapi2.models.QuizSubmissionQuestionResponse
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.models.UpdateFileFolder
import com.instructure.canvasapi2.models.postmodels.PlannerNoteBody
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.utils.orDefault
import okio.Buffer
import java.util.Date

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
        LongId(PathVars::submissionId) to endpoint(
            Segment("questions") to endpoint(
                configure = {
                    GET {
                        val submissionQuestions =
                            data.quizSubmissionQuestions[pathVars.submissionId]
                                ?: listOf<QuizSubmissionQuestion>()
                        val response =
                            QuizSubmissionQuestionResponse(quizSubmissionQuestions = submissionQuestions)
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
        LongId(PathVars::eventId) to Endpoint(
            response = {
                GET {
                    val event = data.courseCalendarEvents.values.flatten()
                        .plus(data.userCalendarEvents.values.flatten())
                        .find { it.id == pathVars.eventId }
                    if (event != null) {
                        request.successResponse(event)
                    } else {
                        request.unauthorizedResponse()
                    }
                }
                DELETE {
                    val event = data.courseCalendarEvents.values.flatten().find { it.id == pathVars.eventId }
                    if (event != null) {
                        data.courseCalendarEvents[event.courseId]?.remove(event)
                        request.successResponse(event)
                    } else {
                        request.unauthorizedResponse()
                    }
                }
                PUT {
                    val params = getJsonFromRequestBody<ScheduleItem.ScheduleItemParamsWrapper>(request.body)?.calendarEvent
                    val event = ScheduleItem(
                        contextCode = params?.contextCode,
                        title = params?.title,
                        description = params?.description,
                        startAt = params?.startAt,
                        endAt = params?.endAt,
                        isAllDay = params?.isAllDay ?: false,
                        rrule = params?.rrule,
                        seriesNaturalLanguage = params?.rrule,
                        locationName = params?.locationName,
                        locationAddress = params?.locationAddress,
                        itemId = data.newItemId().toString(),
                    )

                    when (event.contextType) {
                        CanvasContext.Type.COURSE -> {
                            val courseEvents = data.courseCalendarEvents[event.courseId] ?: mutableListOf()
                            courseEvents.find { it.id == pathVars.eventId }?.let {
                                courseEvents.remove(it)
                                courseEvents.add(event)
                            }
                            data.courseCalendarEvents[event.courseId] = courseEvents
                        }

                        CanvasContext.Type.USER -> {
                            val userEvents = data.userCalendarEvents[event.userId] ?: mutableListOf()
                            userEvents.find { it.id == pathVars.eventId }?.let {
                                userEvents.remove(it)
                                userEvents.add(event)
                            }
                            data.userCalendarEvents[event.userId] = userEvents
                        }

                        else -> {}
                    }

                    if (params?.rrule?.isNotEmpty().orDefault()) {
                        request.successResponse(listOf(event))
                    } else {
                        request.successResponse(event)
                    }
                }
            }
        ),
        Segment("") to Endpoint( // Our call has an extra "/" at the end, thus the blank segment
            response = {
                GET {
                    val contextCodes = request.url.queryParameterValues("context_codes[]")
                    val courseIds = contextCodes
                        .filter { it?.startsWith("course_").orDefault() }
                        .map { it?.substringAfter("course_")?.toLong() }
                    val userIds = contextCodes
                        .filter { it?.startsWith("user_").orDefault() }
                        .map { it?.substringAfter("user_")?.toLong() }
                    val eventType = request.url.queryParameter("type") ?: "event"
                    if (courseIds.isNotEmpty() || userIds.isNotEmpty()) {
                        val events = when (eventType) {
                            "assignment" -> data
                                .assignments
                                .values
                                .filter { a -> a.courseId in courseIds }
                                .map { a ->
                                    ScheduleItem(
                                        itemId = a.id.toString(),
                                        title = a.name,
                                        description = a.description,
                                        startAt = a.dueAt,
                                        assignment = a,
                                        contextCode = "course_${a.courseId}",
                                        contextName = data.courses[a.courseId]?.name
                                    )
                                }

                            // default handler assumes "event" event type
                            else -> {
                                data.courseCalendarEvents.filter { it.key in courseIds }.values.flatten() +
                                        data.userCalendarEvents.filter { it.key in userIds }.values.flatten()
                            }
                        }
                        request.successResponse(events)
                    } else {
                        request.unauthorizedResponse()
                    }
                }
                POST {
                    val params = getJsonFromRequestBody<ScheduleItem.ScheduleItemParamsWrapper>(request.body)?.calendarEvent
                    val event = ScheduleItem(
                        contextCode = params?.contextCode,
                        title = params?.title,
                        description = params?.description,
                        startAt = params?.startAt,
                        endAt = params?.endAt,
                        isAllDay = params?.isAllDay ?: false,
                        rrule = params?.rrule,
                        seriesNaturalLanguage = params?.rrule,
                        locationName = params?.locationName,
                        locationAddress = params?.locationAddress,
                        itemId = data.newItemId().toString(),
                    )

                    when (event.contextType) {
                        CanvasContext.Type.COURSE -> {
                            val courseEvents = (data.courseCalendarEvents[event.courseId] ?: mutableListOf()).apply {
                                add(event)
                            }
                            data.courseCalendarEvents[event.courseId] = courseEvents
                        }

                        CanvasContext.Type.USER -> {
                            val userEvents = (data.userCalendarEvents[event.userId] ?: mutableListOf()).apply {
                                add(event)
                            }
                            data.userCalendarEvents[event.userId] = userEvents
                        }

                        else -> {}
                    }

                    request.successResponse(event)
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
        },
        Segment("items") to Endpoint {
            GET {
                val contextCodes = request.url.queryParameterValues("context_codes[]")
                val startDate = request.url.queryParameter("start_date").toDate()
                val endDate = request.url.queryParameter("end_date").toDate()
                val filter = request.url.queryParameter("filter")
                val courseIds = contextCodes
                    .filter { it?.startsWith("course_").orDefault() }
                    .map { it?.substringAfter("course_")?.toLong() }
                val userIds = contextCodes
                    .filter { it?.startsWith("user_").orDefault() }
                    .map { it?.substringAfter("user_")?.toLong() }

                val plannerItems = data.todos.filter {
                    courseIds.contains(it.courseId) || userIds.contains(it.userId)
                }.filter {
                    if (it.plannableDate == null) return@filter true
                    if (startDate == null || endDate == null) return@filter true
                    it.plannableDate.time in startDate.time..endDate.time
                }
                request.successResponse(plannerItems)
            }
        }
    ),
    Segment("features") to Endpoint(
        Segment("environment") to object : Endpoint(
            response = {
                GET {
                    request.successResponse(mapOf("mobile_offline_mode" to data.offlineModeEnabled))
                }
            }
        ) {
            override val authModel = DontCareAuthModel
        }
    ),
    Segment("settings") to Endpoint(
        Segment("environment") to Endpoint(
            response = {
                GET {
                    request.successResponse(mapOf("calendar_contexts_limit" to 20, "enable_inbox_signature_block" to true))
                }
            }
        )
    ),
    Segment("progress") to Endpoint(
        LongId(PathVars::progressId) to Endpoint {
            GET {
                request.successResponse(Progress(pathVars.progressId, workflowState = "completed"))
            }
            Segment("cancel") to Endpoint {
                POST {
                    request.successResponse(Progress(pathVars.progressId, workflowState = "failed"))
                }
            }
        }
    ),
    Segment("planner_notes") to Endpoint(
        LongId(PathVars::plannerNoteId) to Endpoint {
            DELETE {
                val plannerNote = data.todos.find { it.plannable.id == pathVars.plannerNoteId }
                if (plannerNote != null) {
                    data.todos.remove(plannerNote)
                    request.successResponse(Unit)
                } else {
                    request.unauthorizedResponse()
                }
            }
            PUT {
                val params = getJsonFromRequestBody<PlannerNoteBody>(request.body)
                val plannerItem = data.todos.find { it.plannable.id == pathVars.plannerNoteId }!!
                val course = params?.courseId?.let { data.courses[it] }
                val date = params?.toDoDate?.toDate()
                data.todos.remove(plannerItem)
                data.todos.add(
                    plannerItem.copy(
                        courseId = course?.id,
                        contextName = course?.name,
                        plannable = plannerItem.plannable.copy(
                            title = params?.title.orEmpty(),
                            courseId = course?.id,
                            todoDate = date.toApiString(),
                            details = params?.details
                        ),
                        plannableDate = date ?: Date()
                    )
                )
                request.successResponse(Unit)
            }
        },
        response = {
            GET {
                val contextCodes = request.url.queryParameterValues("context_codes[]")
                val startDate = request.url.queryParameter("start_date").toDate()
                val endDate = request.url.queryParameter("end_date").toDate()
                val courseIds = contextCodes
                    .filter { it?.startsWith("course_").orDefault() }
                    .map { it?.substringAfter("course_")?.toLong() }
                val userIds = contextCodes
                    .filter { it?.startsWith("user_").orDefault() }
                    .map { it?.substringAfter("user_")?.toLong() }

                val plannables = data.todos.filter {
                    courseIds.contains(it.courseId) || userIds.contains(it.userId)
                }.filter {
                    if (it.plannable.todoDate == null) return@filter true
                    if (startDate == null || endDate == null) return@filter true
                    it.plannable.todoDate.toDate()?.time in startDate.time..endDate.time
                }.map { it.plannable }
                request.successResponse(plannables)
            }
            POST {
                val params = getJsonFromRequestBody<PlannerNoteBody>(request.body)
                val plannerItem = data.addPlannable(
                    name = params?.title.orEmpty(),
                    userId = data.currentUser!!.id,
                    course = params?.courseId?.let { data.courses[it] },
                    type = PlannableType.PLANNER_NOTE,
                    date = params?.toDoDate?.toDate(),
                    details = params?.details
                )
                request.successResponse(plannerItem)
            }
        }
    )
)

object FileUrlEndpoint : Endpoint(
    LongId(PathVars::fileId) to Endpoint {
        GET {
            val file = data.folderFiles.values.flatten().find { it.id == pathVars.fileId }
            if (file != null) {
                request.successResponse(file)
            } else {
                request.unauthorizedResponse()
            }
        }
        PUT {
            val buffer = Buffer()
            request.body!!.writeTo(buffer)
            val body = buffer.readUtf8()
            val updateFileFolder = Gson().fromJson(body, UpdateFileFolder::class.java)
            val folderFiles = data.folderFiles.values.find { it.any { it.id == pathVars.fileId } }
            val file = data.folderFiles.values.flatten().find { it.id == pathVars.fileId }
            if (file == null || folderFiles == null) {
                request.unauthorizedResponse()
            } else {
                val folderId = file.parentFolderId
                val updatedFile = file.copy(
                    lockDate = updateFileFolder.lockAt.toDate() ?: file.lockDate,
                    unlockDate = updateFileFolder.unlockAt.toDate() ?: file.unlockDate,
                    isLocked = updateFileFolder.locked ?: file.isLocked,
                    isHidden = updateFileFolder.hidden ?: file.isHidden,
                    visibilityLevel = updateFileFolder.visibilityLevel ?: file.visibilityLevel
                )
                val updatedFolderFiles =
                    folderFiles.map { if (it.id == pathVars.fileId) updatedFile else it }
                data.folderFiles[folderId] = updatedFolderFiles.toMutableList()

                val affectedCourseMap = data.courseModules.filterValues { modules ->
                    modules.any { module ->
                        module.items.any { it.contentId == pathVars.fileId }
                    }
                }

                affectedCourseMap.forEach { (courseId, modules) ->
                    val updatedModules = modules.map { module ->
                        val updatedItems = module.items.filter { it.contentId == pathVars.fileId }
                            .associate { moduleItem ->
                                moduleItem.id to moduleItem.copy(
                                    published = !updatedFile.isLocked,
                                    moduleDetails = ModuleContentDetails(
                                        lockAt = updatedFile.lockDate?.toString(),
                                        unlockAt = updatedFile.unlockDate?.toString(),
                                        locked = updatedFile.isLocked,
                                        hidden = updatedFile.isHidden
                                    )
                                )
                            }
                        module.copy(items = module.items.map { moduleItem ->
                            updatedItems[moduleItem.id] ?: moduleItem
                        })
                    }
                    data.courseModules[courseId] = updatedModules.toMutableList()
                }

                request.successResponse(updatedFile)
            }

        }
    }
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
object GroupsEndpoint : Endpoint(
    LongId(PathVars::groupId) to endpoint(
        Segment("tabs") to endpoint( // group tabs
            configure = {
                GET {
                    request.successResponse(data.groupTabs[pathVars.groupId] ?: listOf<Tab>())
                }
            }
        ),

        Segment("discussion_topics") to endpoint( // group discussion topics
            LongId(PathVars::topicId) to endpoint(
                Segment("view") to endpoint(
                    configure = {
                        GET {
                            val topic = data
                                .groupDiscussionTopicHeaders[pathVars.groupId]
                                ?.find { it.id == pathVars.topicId }

                            if (topic != null) {
                                request.successResponse(topic)
                            } else {
                                request.unauthorizedResponse()
                            }
                        }
                    }
                )
            ),
            configure = {
                GET {
                    // TODO: Merge this logic with course discussion_topics logic
                    val announcementsOnly =
                        request.url.queryParameter("only_announcements")?.equals("1") ?: false
                    var topics = data.groupDiscussionTopicHeaders[pathVars.groupId]
                    if (topics != null) {
                        if (announcementsOnly) {
                            // return only announcements
                            topics = topics!!.filter { it.announcement }.toMutableList()
                        } else {
                            // return only non-announcement discussions
                            topics = topics!!.filter { !it.announcement }.toMutableList()
                        }
                        request.successResponse(topics!!)
                    } else {
                        request.unauthorizedResponse()
                    }
                }
                POST {
                    val jsonObject = grabJsonFromMultiPartBody(request.body!!)
                    var newHeader = Gson().fromJson(jsonObject, DiscussionTopicHeader::class.java)
                    var group = data.groups.values.find { it.id == pathVars.groupId }
                    var course = data.courses.values.find { it.id == group!!.courseId }
                    var user = request.user!!

                    newHeader = data.addDiscussionTopicToCourse(
                        course = course!!,
                        groupId = group!!.id,
                        user = user,
                        prePopulatedTopicHeader = newHeader,
                        topicTitle = newHeader.title!!,
                        topicDescription = newHeader.message!!,
                        allowRating = data.discussionRatingsEnabled,
                        allowReplies = data.discussionRepliesEnabled,
                        allowAttachments = data.discussionAttachmentsEnabled,
                        isAnnouncement = newHeader.announcement
                    )
                    Log.d("<--", "new discussion topic request body: $jsonObject")
                    Log.d("<--", "new header: $newHeader")

                    request.successResponse(newHeader)
                }
            }
        ),

        Segment("pages") to endpoint(// group pages
            StringId(PathVars::pageUrl) to endpoint(
                configure = {
                    GET {
                        val page =
                            data.groupPages[pathVars.groupId]?.find { it.url == pathVars.pageUrl }
                        if (page != null) {
                            request.successResponse(page)
                        } else {
                            request.unauthorizedResponse()
                        }
                    }
                }
            ),

            configure = {
                GET {
                    val pages = data.groupPages[pathVars.groupId]
                    if (pages != null) {
                        request.successResponse(pages)
                    } else {
                        request.unauthorizedResponse()
                    }
                }
            }
        ),

        Segment("folders") to endpoint(// groups folders
            Segment("root") to endpoint(
                configure = {
                    GET {
                        val rootFolder = data.groupRootFolders[pathVars.groupId]
                        if (rootFolder != null) {
                            request.successResponse(rootFolder)
                        } else {
                            request.unauthorizedResponse()
                        }
                    }
                }
            )

        ),

        Segment("users") to endpoint( // group's users
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
                if (group != null) {
                    request.successResponse(group)
                } else {
                    request.unauthorizedResponse()
                }
            }
        }
    )

)
