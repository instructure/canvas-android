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
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockCanvas.addFileToCourse
import com.instructure.canvas.espresso.mockCanvas.addQuizSubmission
import com.instructure.canvas.espresso.mockCanvas.addReplyToDiscussion
import com.instructure.canvas.espresso.mockCanvas.endpoint
import com.instructure.canvas.espresso.mockCanvas.utils.AuthModel
import com.instructure.canvas.espresso.mockCanvas.utils.DontCareAuthModel
import com.instructure.canvas.espresso.mockCanvas.utils.LongId
import com.instructure.canvas.espresso.mockCanvas.utils.PathVars
import com.instructure.canvas.espresso.mockCanvas.utils.Segment
import com.instructure.canvas.espresso.mockCanvas.utils.StringId
import com.instructure.canvas.espresso.mockCanvas.utils.UserId
import com.instructure.canvas.espresso.mockCanvas.utils.getJsonFromRequestBody
import com.instructure.canvas.espresso.mockCanvas.utils.grabJsonFromMultiPartBody
import com.instructure.canvas.espresso.mockCanvas.utils.noContentResponse
import com.instructure.canvas.espresso.mockCanvas.utils.successPaginatedResponse
import com.instructure.canvas.espresso.mockCanvas.utils.successResponse
import com.instructure.canvas.espresso.mockCanvas.utils.unauthorizedResponse
import com.instructure.canvas.espresso.mockCanvas.utils.user
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionParticipant
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.models.GradingPeriodResponse
import com.instructure.canvasapi2.models.LaunchDefinition
import com.instructure.canvasapi2.models.ModuleItemSequence
import com.instructure.canvasapi2.models.Progress
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.QuizSubmission
import com.instructure.canvasapi2.models.QuizSubmissionResponse
import com.instructure.canvasapi2.models.QuizSubmissionTime
import com.instructure.canvasapi2.models.SmartSearchContentType
import com.instructure.canvasapi2.models.SmartSearchResult
import com.instructure.canvasapi2.models.SmartSearchResultWrapper
import com.instructure.canvasapi2.models.postmodels.BulkUpdateProgress
import com.instructure.canvasapi2.models.postmodels.BulkUpdateResponse
import com.instructure.canvasapi2.models.postmodels.UpdateCourseWrapper
import com.instructure.canvasapi2.utils.globalName
import com.instructure.canvasapi2.utils.toApiString
import okio.Buffer
import org.json.JSONObject
import java.util.Calendar
import java.util.Date
import kotlin.random.Random

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
                val enrollmentState = request.url.queryParameter("enrollment_state")
                val user = request.user!!
                val courses = data.enrollments
                        .values
                        .filter { it.userId == user.id }
                        .mapNotNull {
                            var course = data.courses.getOrDefault(it.courseId, null)
                            if (request.url.queryParameterValues("include[]").contains("tabs")) {
                                course = course?.copy(tabs = data.courseTabs[course?.id])
                            }
                            if (request.url.queryParameterValues("include[]").contains("permissions")) {
                                course?.permissions = data.coursePermissions[course?.id]
                            }
                            course
                        }
                        .filter {
                            when (enrollmentState) {
                                "active" -> it.isCurrentEnrolment()
                                "completed" -> it.isPastEnrolment()
                                "invited_or_pending" -> it.isFutureEnrolment()
                                else -> true
                            }
                        }
                request.successPaginatedResponse(courses.distinct())
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
        Segment("files") to CourseFilesEndpoint,
        Segment("discussion_topics") to CourseDiscussionTopicListEndpoint,
        Segment("modules") to CourseModuleListEndpoint,
        Segment("module_item_sequence") to CourseModuleItemSequenceEndpoint,
        Segment("quizzes") to CourseQuizListEndpoint,
        Segment("all_quizzes") to CourseQuizListEndpoint,
        Segment("users") to CourseUsersEndpoint,
        Segment("permissions") to CoursePermissionsEndpoint,
        Segment("lti_apps") to CourseLTIAppsEndpoint,
        Segment("grading_periods") to CourseGradingPeriodsEndpoint,
        Segment("sections") to CourseSectionsEndpoint,
        Segment("enrollments") to EnrollmentIndexEndpoint,
        Segment("features") to Endpoint(
                Segment("enabled") to CourseEnabledFeaturesEndpoint
        ),
        Segment("settings") to Endpoint(
                response = {
                    GET {
                        val courseId = pathVars.courseId
                        val settings = data.courseSettings[courseId] ?: CourseSettings()
                        request.successResponse(settings)
                    }

                    PUT {
                        val courseId = pathVars.courseId
                        val settings = data.courseSettings[courseId] ?: CourseSettings()

                        // Handle course settings change, if present
                        val newSyllabusSummaryVisibility = request.url.queryParameter("syllabus_course_summary")
                        if (newSyllabusSummaryVisibility != null) {
                            settings.courseSummary = newSyllabusSummaryVisibility.toBoolean()
                        }

                        // Return the updated course
                        request.successResponse(settings)
                    }
                }
        ),
        Segment("smartsearch") to Endpoint(
            response = {
                GET {
                    val courseId = pathVars.courseId
                    val query = request.url.queryParameter("q").orEmpty()

                    //We are only looking for matches in the name of the items

                    val assignments = data.assignments.values.filter { it.courseId == courseId }
                        .filter { it.name?.contains(query) == true }
                        .map {
                            SmartSearchResult(
                                contentId = it.id,
                                title = it.name.orEmpty(),
                                contentType = SmartSearchContentType.ASSIGNMENT,
                                htmlUrl = "https://mock-data.instructure.com/courses/$courseId/assignments/${it.id}",
                                body = "query body",
                                relevance = Random.nextInt(50, 100),
                                distance = Random.nextDouble(0.0, 1.0)
                            )
                        }

                    val announcements = data.courseDiscussionTopicHeaders[courseId]?.filter {
                        it.announcement && it.title?.contains(query) == true
                    }?.map {
                        SmartSearchResult(
                            contentId = it.id,
                            title = it.title.orEmpty(),
                            contentType = SmartSearchContentType.ANNOUNCEMENT,
                            htmlUrl = "https://mock-data.instructure.com/courses/$courseId/announcements/${it.id}",
                            body = "query body",
                            relevance = Random.nextInt(50, 100),
                            distance = Random.nextDouble(0.0, 1.0)
                        )
                    } ?: emptyList()

                    val discussions = data.courseDiscussionTopicHeaders[courseId]?.filter {
                        !it.announcement && it.title?.contains(query) == true
                    }?.map {
                        SmartSearchResult(
                            contentId = it.id,
                            title = it.title.orEmpty(),
                            contentType = SmartSearchContentType.DISCUSSION_TOPIC,
                            htmlUrl = "https://mock-data.instructure.com/courses/$courseId/discussion_topics/${it.id}",
                            body = "query body",
                            relevance = Random.nextInt(50, 100),
                            distance = Random.nextDouble(0.0, 1.0)
                        )
                    } ?: emptyList()

                    val pages = data.coursePages[courseId]?.filter { it.title?.contains(query) == true }
                        ?.map {
                            SmartSearchResult(
                                contentId = it.id,
                                title = it.title.orEmpty(),
                                contentType = SmartSearchContentType.WIKI_PAGE,
                                htmlUrl = it.url.orEmpty(),
                                body = it.body.orEmpty(),
                                relevance = Random.nextInt(50, 100),
                                distance = Random.nextDouble(0.0, 1.0)
                            )
                        } ?: emptyList()

                    val results = (assignments + announcements + discussions + pages).sortedByDescending { it.relevance }

                    request.successResponse(SmartSearchResultWrapper(results))
                }
            }
        ),
        response = {
            GET {
                val courseId = pathVars.courseId
                var course = data.courses[courseId]!!
                if (request.url.queryParameterValues("include[]").contains("tabs")) {
                    course = course.copy(tabs = data.courseTabs[courseId])
                }
                if (request.url.queryParameterValues("include[]").contains("permissions")) {
                    course.permissions = data.coursePermissions[courseId]
                }
                if (request.url.queryParameterValues("include[]").contains("settings")) {
                    course = course.copy(settings = data.courseSettings[courseId])
                }
                val userId = request.user!!.id
                if (data.enrollments.values.any { it.courseId == course.id && it.userId == userId }) {
                    request.successResponse(course)
                } else {
                    request.unauthorizedResponse()
                }
            }

            PUT {
                val course = data.courses[pathVars.courseId]!!

                // Handle course name change request if present
                val newCourseName = request.url.queryParameter("course[name]")
                if(newCourseName != null) {
                    course.globalName = newCourseName
                }

                // Handle course default view change request, if present
                val newHomePage = request.url.queryParameter("course[default_view]")
                if(newHomePage != null) {
                    course.homePage = when(newHomePage) {
                        "feed" -> Course.HomePage.HOME_FEED
                        "assignments" -> Course.HomePage.HOME_ASSIGNMENTS
                        "modules" -> Course.HomePage.HOME_MODULES
                        "syllabus" -> Course.HomePage.HOME_SYLLABUS
                        "wiki" -> Course.HomePage.HOME_WIKI
                        else -> Course.HomePage.HOME_MODULES
                    }
                }

                // Handle course syllabus change, if present
                val updateCourseWrapper = getJsonFromRequestBody<UpdateCourseWrapper>(request.body)
                val newSyllabusBody = updateCourseWrapper?.course?.syllabusBody
                if (newSyllabusBody != null) {
                    course.syllabusBody = newSyllabusBody
                }

                // Return the updated course
                request.successResponse(course)
            }
        }
)

/**
 * Endpoint for course features
 */
object CourseEnabledFeaturesEndpoint : Endpoint( response = {
    GET {
        request.successResponse(listOf("assignments_2_student"))
    }
})

/**
 * Endpoint for sections
 */
object CourseSectionsEndpoint : Endpoint( response = {
    GET {
        val courseId = pathVars.courseId
        val sections = data.courses.values.filter {course -> course.id == courseId}.first().sections
        request.successResponse(sections)
    }
})
/**
 * Endpoint for grading periods
 *
 * Returns an empty list if no grading periods have been created for the course
 */
object CourseGradingPeriodsEndpoint : Endpoint(response = {
    GET {
        val gradingPeriods = data.courseGradingPeriods[pathVars.courseId] ?: listOf<GradingPeriod>()
        request.successResponse(GradingPeriodResponse(gradingPeriods))
    }
})

/**
 * Endpoint for course LTI apps
 *
 * For now, just the "launch_definitions" segment is supported.
 */
object CourseLTIAppsEndpoint : Endpoint (
        Segment("launch_definitions") to Endpoint( response = {
            GET {
                // For now, just return an empty list of LaunchDefinitions
                request.successResponse(listOf<LaunchDefinition>())
            }
        })
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
        StringId(PathVars::pageUrl) to CoursePageEndpoint,
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
        val page = pages?.firstOrNull { it.url == pathVars.pageUrl }
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

/**
 * Endpoint that can return a list of files for a course
 *
 * Right now, only supports grabbing a specific file
 *
 * ROUTES:
 * - `{fileId}` -> anonymous endpoint to retrieve a file
 */
object CourseFilesEndpoint : Endpoint(
        LongId(PathVars::fileId) to CourseFileEndpoint

)

object CourseFileEndpoint : Endpoint (
        response = {
            GET {
                val courseRootFolder = data.courseRootFolders[pathVars.courseId]
                val targetFileFolder = data.folderFiles[courseRootFolder?.id]?.find { it.id == pathVars.fileId }
                if (targetFileFolder != null) {
                    request.successResponse(targetFileFolder)
                } else {
                    request.unauthorizedResponse()
                }

            }

            // This is the endpoint that I created for the purpose of uploading assignment files.
            POST {
                val courseId = pathVars.courseId
                val jsonObj = grabJsonFromMultiPartBody(request.body!!)
                val fileName = jsonObj["name"].asString
                val fileSize = jsonObj["size"].asInt
                val contentType = jsonObj["content_type"].asString
                val contents = jsonObj["file"].asString

                val fileId = pathVars.fileId
                val url = "https://mock-data.instructure.com/api/v1/courses/$courseId/files/$fileId"
                data.addFileToCourse(
                        courseId = courseId,
                        displayName = fileName,
                        fileContent = contents,
                        contentType = contentType,
                        url = url,
                        fileId = fileId
                )

                val response = Attachment(
                        id = fileId,
                        contentType = contentType,
                        filename = fileName,
                        displayName = fileName,
                        url = url,
                        previewUrl = url,
                        size = fileSize.toLong()
                )
                request.successResponse(response)

            }
        }

) {
    // Disable auth-check for course files endpoint
    override val authModel: AuthModel
        get() = DontCareAuthModel
}

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
                var announcementsOnly = request.url.queryParameter("only_announcements")?.equals("1")
                var sectionsOnly = request.url.queryParameterValues("include[]").contains("sections")

                // Base course discussion topic list
                var courseDiscussionTopics = data.courseDiscussionTopicHeaders[pathVars.courseId]

                // If specified, filter down to discussions that are announcements
                if(courseDiscussionTopics != null && announcementsOnly != null && announcementsOnly == true)  {
                    courseDiscussionTopics = courseDiscussionTopics?.filter {it.announcement}?.toMutableList()
                }

                // If specified, filter down to discussions with course sections enrolled in by the user
                if(courseDiscussionTopics != null && sectionsOnly != null && sectionsOnly == true) {

                    val userId = request.user!!.id
                    val courseId = pathVars.courseId
                    // While we will probably encounter at most one enrollment for this user in this course,
                    // we'll allow for the user to be enrolled in multiple sections.
                    val enrollmentSectionIds =
                            data.enrollments.values
                                    .filter {it.userId == userId && it.courseId == courseId}
                                    .map {it -> it.courseSectionId}
                    courseDiscussionTopics =
                            courseDiscussionTopics!!.filter {
                                it.sections == null
                                        || it.sections!!.count() == 0
                                        || it.sections!!.find {enrollmentSectionIds.contains(it.id)} != null
                            }.toMutableList()
                }

                // Now return the final list
                request.successResponse(courseDiscussionTopics ?: listOf<DiscussionTopicHeader>())
            }

            POST {
                val jsonObject = grabJsonFromMultiPartBody(request.body!!)
                var newHeader = Gson().fromJson(jsonObject, DiscussionTopicHeader::class.java)
                var course = data.courses.values.find { it.id == pathVars.courseId }
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
                        allowAttachments = data.discussionAttachmentsEnabled,
                        isAnnouncement = newHeader.announcement
                )
                Log.d("<--", "new discussion topic request body: $jsonObject")
                Log.d("<--", "new header: $newHeader")

                request.successResponse(newHeader)
            }

        }
)

/**
 * Endpoint that handles all course quiz related calls
 *
 * GET to retrieve a list of quizzes for a course
 *
 * ROUTES:
 * - `{quizId}` -> get info for specific quiz
 *                  `submissions` -> get quiz submission list, post new submission
 *                      `{submissionId}` -> get specific submission
 *                          `events` -> post submission events
 *                          `complete` -> record (post) submission completion
 *                          `time` -> get the running time of the submission
 *                  `questions` -> get quiz question list
 *                      `{questionId} -> get specific quiz question
 */
object CourseQuizListEndpoint : Endpoint(
        LongId(PathVars::quizId) to endpoint(
                Segment("submissions") to endpoint (
                        LongId(PathVars::submissionId) to endpoint (
                                Segment("events") to endpoint (
                                        configure = {
                                            POST {
                                                val jsonObject = grabJsonFromMultiPartBody(request.body!!)
                                                Log.d("submissions", "new event jsonObject = $jsonObject")
                                                request.noContentResponse()
                                            }
                                        }
                                ),
                                Segment("complete") to endpoint (
                                        configure = {
                                            POST {
                                                val quizId = pathVars.quizId
                                                val submissionId = pathVars.submissionId
                                                val submission = data.quizSubmissions[quizId]?.find {it.id == submissionId}

                                                if(submission != null) {
                                                    request.successResponse(
                                                            getQuizSubmissionCompleteResponse(data, submission, quizId)
                                                    )
                                                }
                                                else {
                                                    request.unauthorizedResponse()
                                                }
                                            }
                                        }
                                ),
                                Segment("time") to endpoint(
                                        configure = {
                                            GET { // return running time stats for the submission
                                                // Do our best to fill in "endAt" and "timeLeft"
                                                val quiz = data.courseQuizzes[pathVars.courseId]?.find {it.id == pathVars.quizId}
                                                val quizId = pathVars.quizId
                                                val submissionId = pathVars.submissionId
                                                val submission = data.quizSubmissions[quizId]?.find {it.id == submissionId}
                                                if(quiz != null && submission != null && submission.startedDate != null) {
                                                    request.successResponse(
                                                            getQuizSubmissionTimeResponse(submission, quiz)
                                                    )
                                                }
                                                else {
                                                    request.unauthorizedResponse()
                                                }

                                            }
                                        }
                                )
                        ),
                        configure = {
                            GET { // Return submission list for quiz
                                val submissionList = data.quizSubmissions[pathVars.quizId] ?: mutableListOf<QuizSubmission>()
                                val response = QuizSubmissionResponse(quizSubmissions = submissionList)
                                request.successResponse(response)
                            }

                            POST { // new submission
                                val jsonObject = grabJsonFromMultiPartBody(request.body!!)
                                Log.d("submissions", "new submission jsonObject = $jsonObject")
                                val response = getQuizSubmissionResponse(
                                        data = data,
                                        courseId = pathVars.courseId,
                                        quizId = pathVars.quizId,
                                        userId = request.user!!.id)
                                request.successResponse(response)

                            }
                        }
                ),

                Segment("questions") to endpoint(
                        LongId(PathVars::questionId) to endpoint(
                                configure = {
                                    GET {
                                        val question = data.quizQuestions[pathVars.quizId]?.find { it.id == pathVars.questionId }
                                        if (question != null) {
                                            request.successResponse(question)
                                        } else {
                                            request.unauthorizedResponse()
                                        }

                                    }
                                }
                        ), // end specific-question endpoint

                        configure = {
                            GET { // Get quiz question list for our quiz
                                val quizzesForCourse = data.courseQuizzes[pathVars.courseId]
                                if (quizzesForCourse != null) {
                                    request.successResponse(quizzesForCourse)
                                } else {
                                    request.unauthorizedResponse()
                                }
                            }
                        }

                ), // End question list endpoint
                configure = {
                    GET {
                        // Get single quiz
                        val quiz = data.courseQuizzes[pathVars.courseId]?.find {it.id == pathVars.quizId }
                        if(quiz != null) {
                            request.successResponse(quiz)
                        }
                        else {
                            request.unauthorizedResponse()
                        }
                    }

                    PUT {
                        // Edit a quiz
                        val courseId = pathVars.courseId
                        val quizId = pathVars.quizId
                        val courseQuizList = data.courseQuizzes[courseId]
                        val quiz = courseQuizList?.firstOrNull { q -> q.id == quizId }

                        if(quiz != null) {
                            // Grab the QuizPostBodyWrapper in JSON format
                            val buffer = Buffer()
                            request.body?.writeTo(buffer)
                            val stringOutput = buffer.readUtf8()
                            val jsonObject = JSONObject(stringOutput)

                            // Then extract the QuizPostBody object from the json object
                            val quizPostBody = jsonObject.getJSONObject("quiz")

                            // Make a new quiz object based on the requested changes
                            // Right now, we are only cognizant of changes to the "title" and "access_code"
                            // fields, because that's all it takes to get our tests to work.  In the future,
                            // we may need to recognize / handle additional fields.
                            val newTitle = quizPostBody.optString("title", null) ?: quiz.title
                            val newAccessCode = quizPostBody.optString("access_code", null) ?: quiz.accessCode
                            val newQuiz = quiz.copy(
                                    title = newTitle,
                                    accessCode = newAccessCode
                            )

                            // Now substitute the new quiz for the original quiz
                            courseQuizList.remove(quiz)
                            courseQuizList.add(newQuiz)

                            // And return the new quiz
                            request.successResponse(newQuiz)
                        }
                        else {
                            request.unauthorizedResponse()
                        }
                    }
                }
        ), // End single-quiz endpoint

        response = {
            GET { // Get the list of quizzes for course
                val quizzesForCourse = data.courseQuizzes[pathVars.courseId]
                if (quizzesForCourse != null) {
                    request.successResponse(quizzesForCourse)
                } else {
                    request.unauthorizedResponse()
                }
            }
        }

)

// Utility method to construct a response to a quiz-submission-complete request
// POST /course/{courseId}/quizzes/{quizId}/submissions/{submissionId}/complete
private fun getQuizSubmissionCompleteResponse(
        data: MockCanvas,
        submission: QuizSubmission,
        quizId: Long
) : QuizSubmissionResponse {
    val updatedSubmission = submission.copy(
            finishedAt = Calendar.getInstance().time.toApiString(),
            workflowState = "complete"
    )
    // Swap out submission for updated submission
    data.quizSubmissions[quizId]!!.remove(submission)
    data.quizSubmissions[quizId]!!.add(updatedSubmission)
    val response = QuizSubmissionResponse(quizSubmissions = listOf(updatedSubmission))

    return response
}

// Utility method to construct a response to a quiz-submission-time request
// GET /course/{courseId}/quizzes/{quizId}/submissions/{submissionId}/time
private fun getQuizSubmissionTimeResponse(
        submission: QuizSubmission,
        quiz: Quiz
) : QuizSubmissionTime {
    val startTime = submission.startedDate!!
    val currentTime = Calendar.getInstance().time
    val diffMs = currentTime.time - startTime.time
    val diffSecs = diffMs / 1000
    val timeLeft = quiz.timeLimit - diffSecs
    val endAtMs = startTime.time + timeLeft * 1000
    val endAt = Date(endAtMs).toApiString()

    val response = QuizSubmissionTime(endAt = endAt, timeLeft = timeLeft.toInt())
    return response
}

// Utility method to construct a response to a new-quiz-submission request
// POST /course/{courseId}/quizzes/{quizId}/submissions
private fun getQuizSubmissionResponse(
        data: MockCanvas,
        courseId: Long,
        quizId: Long,
        userId: Long
) : QuizSubmissionResponse {
    val quiz = data.courseQuizzes[courseId]!!.find {it.id == quizId}!!
    val user = data.users[userId]!!
    data.addQuizSubmission(quiz, user)
    val quizSubmissionList = data.quizSubmissions[quiz.id]

    val response = QuizSubmissionResponse(quizSubmissions = quizSubmissionList!!)
    return response
}

/**
 * Endpoint that can return a specific discussion topic header for a specified course
 *
 * ROUTES:
 * - `view` -> anonymous endpoint to return a DiscussionTopic associated with the endpoint
 * - 'entries -> to [CourseDiscussionEntryListEndpoint]
 */
object CourseDiscussionTopicEndpoint : Endpoint(
        Segment("view") to endpoint {
            GET {
                Log.d("<--", "Discussion topic view get request: $request")
                val result = data.discussionTopics[pathVars.topicId]
                if (result != null) {
                    request.successResponse(result)
                } else {
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
                if (topic != null) {
                    request.successResponse(topic)
                } else {
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
                val jsonObject = grabJsonFromMultiPartBody(request.body!!)
                Log.d("<--", "Discussion topic entries post request: $request")
                Log.d("<--", "post body: $jsonObject")
                val discussionTopicHeader =
                        data.courseDiscussionTopicHeaders[pathVars.courseId]?.find { it.id == pathVars.topicId }
                if (discussionTopicHeader != null) {
                    // Let's route through our manual discussion reply creation logic to avoid
                    // code duplication.
                    var entry = data.addReplyToDiscussion(
                            topicHeader = discussionTopicHeader,
                            user = request.user!!,
                            replyMessage = jsonObject.get("message").asString)
                    request.successResponse(entry)
                } else {
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
 * - `replies` -> anonymous endpoint for threaded replies (POST for a new reply, GET for a list of replies)
 */
object CourseDiscussionEntryEndpoint : Endpoint(
        Segment("read") to endpoint {
            PUT {
                val topic = data.discussionTopics[pathVars.topicId]
                val topicHeader =
                        data.courseDiscussionTopicHeaders[pathVars.courseId]?.find { it.id == pathVars.topicId }
                val entry = topic?.views?.find { it.id == pathVars.entryId }

                if (topic == null || entry == null || topicHeader == null) {
                    request.unauthorizedResponse()
                } else {
                    entry.unread = false
                    topicHeader.unreadCount -= 1
                    topic.unreadEntries.remove(entry.id)
                    request.noContentResponse()
                }

            }
        },
        Segment("rating") to endpoint {
            POST {
                var ratingVal = request.url.queryParameter("rating")?.toInt()
                val topic = data.discussionTopics[pathVars.topicId]
                val entry = topic?.views?.find { it.id == pathVars.entryId }
                if (ratingVal == null || topic == null || entry == null) {
                    request.unauthorizedResponse()
                } else {
                    val currentCount = topic.entryRatings[entry.id]
                    if (ratingVal == 0) {
                        entry._hasRated = false
                        topic.entryRatings[entry.id] = (currentCount ?: 1) - 1
                    } else {
                        entry._hasRated = true
                        topic.entryRatings[entry.id] = (currentCount ?: 0) + 1
                    }
                    request.noContentResponse()
                }
            }

        },
        Segment("replies") to endpoint {
            POST {
                val jsonObject = grabJsonFromMultiPartBody(request.body!!)
                Log.d("<--", "topic entry replies post body: $jsonObject")
                val newEntry = DiscussionEntry(
                        id = data.newItemId(),
                        message = jsonObject.get("message").asString, // This is all that comes with the POST object
                        createdAt = Calendar.getInstance().time.toString(),
                        author = DiscussionParticipant(id = request.user!!.id, displayName = request.user!!.name)
                )
                val topic = data.discussionTopics[pathVars.topicId]
                val entry = topic?.views?.find { it.id == pathVars.entryId }
                if (entry != null && newEntry != null) {
                    entry.addReply(newEntry)
                    request.successResponse(newEntry)
                } else {
                    request.unauthorizedResponse()
                }
            }

            GET {
                val topic = data.discussionTopics[pathVars.topicId]
                val entry = topic?.views?.find { it.id == pathVars.entryId }

                if (entry != null) {
                    request.successResponse(entry.replies ?: mutableListOf<DiscussionEntry>())
                } else {
                    request.unauthorizedResponse()
                }

            }
        }
)

/**
 * Endpoint that can return a list of modules for a course
 *
 * GET to retrieve a list of modules for a course
 *
 * ROUTES:
 * - `{moduleId}` -> [CourseModuleEndpoint]
 */
object CourseModuleListEndpoint : Endpoint(
        LongId(PathVars::moduleId) to CourseModuleEndpoint,
        response = {
            GET {
                val moduleList = data.courseModules[pathVars.courseId]
                if (moduleList != null) {
                    request.successResponse(moduleList)
                } else {
                    request.unauthorizedResponse()
                }
            }
            PUT {
                val moduleIds = request.url.queryParameterValues("module_ids[]").filterNotNull().map { it.toLong() }
                val event = request.url.queryParameter("event")
                val skipContentTags = request.url.queryParameter("skip_content_tags").toBoolean()

                val modules = data.courseModules[pathVars.courseId]?.filter { moduleIds.contains(it.id) }

                val updatedModules = modules?.map {
                    val updatedItems = if (skipContentTags) {
                        it.items
                    } else {
                        it.items.map { it.copy(published = event == "publish") }
                    }
                    it.copy(
                        published = event == "publish",
                        items = updatedItems
                    )
                }

                data.courseModules[pathVars.courseId]?.map { moduleObject ->
                    updatedModules?.find { updatedModuleObject ->
                        updatedModuleObject.id == moduleObject.id
                    } ?: moduleObject
                }?.let {
                    data.courseModules[pathVars.courseId] = it.toMutableList()
                }

                request.successResponse(BulkUpdateResponse(BulkUpdateProgress(Progress(1L, workflowState = "running"))))
            }
        }
)

/**
 * Endpoint that can return a specific module for a course
 *
 * GET to retrieve specific module for a course
 *
 * ROUTES:
 * - `items` -> [CourseModuleItemsListEndpoint]
 */
object CourseModuleEndpoint : Endpoint(
        Segment("items") to CourseModuleItemsListEndpoint,
        response = {
            GET {
                val moduleList = data.courseModules[pathVars.courseId]
                val moduleObject = moduleList?.find { it.id == pathVars.moduleId }
                if (moduleObject != null) {
                    request.successResponse(moduleObject)
                } else {
                    request.unauthorizedResponse()
                }
            }
        }
)


/**
 * Endpoint that can return a list of items for a module in a course
 *
 * GET to retrieve the list of items for a module in a course
 *
 * ROUTES:
 * - `{moduleItemId` -> anonymous endpoint to return a specific module item (for a module in a course)
 */
object CourseModuleItemsListEndpoint : Endpoint(
        LongId(PathVars::moduleItemId) to endpoint {
            GET {
                val moduleList = data.courseModules[pathVars.courseId]
                val moduleObject = moduleList?.find { it.id == pathVars.moduleId }
                val itemList = moduleObject?.items
                val moduleItem = itemList?.find { it.id == pathVars.moduleItemId }

                if (moduleItem != null) {
                    request.successResponse(moduleItem)
                } else {
                    request.unauthorizedResponse()
                }
            }

            PUT {
                val isPublished = request.url.queryParameter("module_item[published]").toBoolean()
                val moduleList = data.courseModules[pathVars.courseId]
                val moduleObject = moduleList?.find { it.id == pathVars.moduleId }
                val itemList = moduleObject?.items
                val moduleItem = itemList?.find { it.id == pathVars.moduleItemId }

                if (moduleItem != null) {
                    val updatedItem = moduleItem.copy(published = isPublished)

                    val updatedModule = moduleObject.copy(
                        items = itemList.map {
                            if (it.id == updatedItem.id) {
                                updatedItem
                            } else {
                                it
                            }
                        }
                    )

                    data.courseModules[pathVars.courseId]?.map {
                        if (it.id == updatedModule.id) {
                            updatedModule
                        } else {
                            it
                        }
                    }?.let {
                        data.courseModules[pathVars.courseId] = it.toMutableList()
                    }

                    request.successResponse(updatedItem)
                } else {
                    request.unauthorizedResponse()
                }
            }
        },
        response = {
            GET {
                val moduleList = data.courseModules[pathVars.courseId]
                val moduleObject = moduleList?.find { it.id == pathVars.moduleId }
                val itemList = moduleObject?.items

                if (itemList != null) {
                    request.successResponse(itemList)
                } else {
                    request.unauthorizedResponse()
                }

            }
        }
)

/**
 * Endpoint that returns list of users in a course
 */
object CourseUsersEndpoint : Endpoint (
    UserId() to CourseSingleUserEndpoint,
    response = {
    GET {
        // We may need to add more "onlyXxx" vars in the future
        val onlyTeachers = request.url.queryParameter("enrollment_type")?.equals("teacher")
                ?: false
        val onlyTas = request.url.queryParameter("enrollment_type")?.equals("ta") ?: false
        val courseId = pathVars.courseId
        var courseEnrollments = data.enrollments.values.filter { it.courseId == courseId }
        if (onlyTeachers) {
            courseEnrollments = courseEnrollments.filter { it.isTeacher }
        } else if (onlyTas) {
            courseEnrollments = courseEnrollments.filter { it.isTA }
        }
        val users = courseEnrollments.map { data.users[it.userId] }
        request.successResponse(users)
    }
})

object CourseSingleUserEndpoint : Endpoint(
    response = {
        GET {
            val requestedUser = data.users[pathVars.userId]
            if(requestedUser != null) {
                request.successResponse(requestedUser)
            } else {
                request.unauthorizedResponse()
            }
        }

    }
)

object CourseModuleItemSequenceEndpoint : Endpoint(
    response = {
        GET {
            request.successResponse(ModuleItemSequence())
        }
    }
)
