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
import com.instructure.canvas.espresso.mockCanvas.addReplyToDiscussion
import com.instructure.canvas.espresso.mockCanvas.endpoint
import com.instructure.canvas.espresso.mockCanvas.utils.*
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.toApiString
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
        Segment("files") to CourseFilesEndpoint,
        Segment("discussion_topics") to CourseDiscussionTopicListEndpoint,
        Segment("modules") to CourseModuleListEndpoint,
        Segment("quizzes") to CourseQuizListEndpoint,
        Segment("permissions") to CoursePermissionsEndpoint,

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

/**
 * Endpoint that can return a list of files for a course
 *
 * Right now, only supports grabbing a specific file
 *
 * ROUTES:
 * - `{fileId}` -> anonymous endpoint to retrieve a file
 */
object CourseFilesEndpoint : Endpoint(
        LongId(PathVars::fileId) to endpoint {
            GET {
                val courseRootFolder = data.courseRootFolders[pathVars.courseId]
                val targetFileFolder = data.folderFiles[courseRootFolder?.id]?.find { it.id == pathVars.fileId }
                if (targetFileFolder != null) {
                    request.successResponse(targetFileFolder)
                } else {
                    request.unauthorizedResponse()
                }

            }
        }
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
                        allowAttachments = data.discussionAttachmentsEnabled
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
                                                val jsonObject = grabJsonFromMultiPartBody(request.body()!!)
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
                                val jsonObject = grabJsonFromMultiPartBody(request.body()!!)
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
    val now = Calendar.getInstance().time.time // ms
    val submission = QuizSubmission(
            id = data.newItemId(),
            quizId = quizId,
            userId = userId,
            startedAt = Date(now).toApiString(),
            endAt = Date(now + quiz.timeLimit * 1000).toApiString(),
            workflowState = "untaken",
            validationToken = "abcd" // just so it's not null??
    )
    var submissionList = data.quizSubmissions[quizId]
    if(submissionList == null) {
        submissionList = mutableListOf<QuizSubmission>()
        data.quizSubmissions[quizId] = submissionList!!
    }
    submissionList!!.add(submission)

    Log.d("<--", "New submission: $submission")

    // It seems like we will need to populate a list of QuizSubmissionQuestions
    // for this submission, to match the QuizQuestions for the quiz.  We'll do that
    // right now.
    val submissionQuestionList = mutableListOf<QuizSubmissionQuestion>()
    val questionList = data.quizQuestions[quizId] ?: listOf<QuizQuestion>()
    for(q in questionList) {
        val submissionAnswers = mutableListOf<QuizSubmissionAnswer>()
        if(q.answers != null) {
            for (a in q.answers!!) {
                submissionAnswers.add(QuizSubmissionAnswer(
                        text = a.answerText,
                        weight = a.answerWeight
                ))
            }
        }
        val sq = QuizSubmissionQuestion(
                id = q.id,
                quizId = quizId,
                questionName = q.questionName,
                questionType = q.questionTypeString,
                questionText = q.questionText,
                answers = submissionAnswers.toTypedArray()
        )
        submissionQuestionList.add(sq)
    }
    data.quizSubmissionQuestions[submission.id] = submissionQuestionList

    val response = QuizSubmissionResponse(quizSubmissions = submissionList!!)
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
                val jsonObject = grabJsonFromMultiPartBody(request.body()!!)
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
                var ratingVal = request.url().queryParameter("rating")?.toInt()
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
                val jsonObject = grabJsonFromMultiPartBody(request.body()!!)
                Log.d("<--", "topic entry replies post body: $jsonObject")
                val newEntry = DiscussionEntry(
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
