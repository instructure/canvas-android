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
 */
package com.instructure.canvas.espresso.mockcanvas.endpoints

import com.instructure.canvas.espresso.mockcanvas.Endpoint
import com.instructure.canvas.espresso.mockcanvas.endpoint
import com.instructure.canvas.espresso.mockcanvas.utils.AnnotationId
import com.instructure.canvas.espresso.mockcanvas.utils.LongId
import com.instructure.canvas.espresso.mockcanvas.utils.PathVars
import com.instructure.canvas.espresso.mockcanvas.utils.Segment
import com.instructure.canvas.espresso.mockcanvas.utils.successResponse
import com.instructure.canvas.espresso.mockcanvas.utils.unauthorizedResponse
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotationResponse

/**
 * Catch all that covers the 3 different api starts for canvadocs:
 *
 * `2018-04-06/sessions`
 * `2018-03-07/sessions`
 * `1/sessions`
 *
 * `sessionId` -> handles the fetch for the docSession
 * `sessionsId/annotations` -> [AnnotationsEndpoint] session ID based index for annotations
 * `sessionsId/annotations/stringId` -> [AnnotationsEndpoint] PUT for annotation creation/editing
 *
 */
object CanvadocApiEndpoint : Endpoint(
    LongId(PathVars::sessionId) to endpoint(
        Segment("annotations") to AnnotationsEndpoint,
        configure = {
            GET {
                if(data.docSessions.containsKey(pathVars.sessionId.toString())) {
                    request.successResponse(data.docSessions[pathVars.sessionId.toString()]!!)
                } else {
                    request.unauthorizedResponse()
                }
            }
        }
    )
)

/**
 * `sessions/sessionsId/annotations/stringId` -> [AnnotationsEndpoint] PUT for annotation creation/editing
 *
 * AnnotationId allows us to capture the stringId param at the end
 *
 */
object AnnotationsEndpoint : Endpoint(
    AnnotationId() to endpoint(
        configure = {
            PUT {
                // If the sent annotation or the list of annotations aren't present, return 401
                if(data.sentAnnotationComment == null || !data.annotations.containsKey(pathVars.sessionId.toString())) {
                    request.unauthorizedResponse()
                } else {
                    val sentAnnotation = data.sentAnnotationComment!!
                    val annotationList = data.annotations[pathVars.sessionId.toString()]!!
                    data.annotations[pathVars.sessionId.toString()] = annotationList.plus(sentAnnotation)
                    data.sentConversation = null
                    request.successResponse(sentAnnotation)
                }
            }
        }
    ),
    response = {
        GET {
            if(!data.annotations[pathVars.sessionId.toString()].isNullOrEmpty()) {
                request.successResponse(CanvaDocAnnotationResponse(data.annotations[pathVars.sessionId.toString()]!!))
            } else {
                request.unauthorizedResponse()
            }
        }
    }
)

