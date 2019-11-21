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
import com.instructure.canvas.espresso.mockCanvas.utils.LongId
import com.instructure.canvas.espresso.mockCanvas.utils.PathVars
import com.instructure.canvas.espresso.mockCanvas.utils.Segment
import com.instructure.canvas.espresso.mockCanvas.utils.successResponse
import com.instructure.canvas.espresso.mockCanvas.utils.unauthorizedResponse
import com.instructure.canvasapi2.models.QuizSubmissionQuestion
import com.instructure.canvasapi2.models.QuizSubmissionQuestionResponse
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
                                    request.body()!!.writeTo(buffer)
                                    val body = buffer.readUtf8()
                                    Log.d("submissionQuestions", "submission question post body: $body")
                                    //val jsonObject = grabJsonFromMultiPartBody(request.body()!!)
                                    //Log.d("submissionQuestions", "submission question post body: $jsonObject")
                                    request.unauthorizedResponse()
                                }
                            }
                    )
            )
    )
)
