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
package com.instructure.canvas.espresso.mockCanvas.endpoints

import com.instructure.canvas.espresso.mockCanvas.Endpoint
import com.instructure.canvas.espresso.mockCanvas.endpoint
import com.instructure.canvas.espresso.mockCanvas.utils.*

object CanvadocApiEndpoint : Endpoint(
    Segment("annotations") to AnnotationsEndpoint,
    LongId(PathVars::sessionId) to endpoint(
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

object AnnotationsEndpoint : Endpoint(
    LongId(PathVars::annotationId) to endpoint(
        configure = {
            PUT {
                request.unauthorizedResponse()
            }
        }
    ),
    response = {
        GET {
            if(!data.annotations[pathVars.annotationId.toString()].isNullOrEmpty()) {
                request.successResponse(data.annotations[pathVars.annotationId.toString()]!!)
            } else {
                request.unauthorizedResponse()
            }
        }
    }
)

