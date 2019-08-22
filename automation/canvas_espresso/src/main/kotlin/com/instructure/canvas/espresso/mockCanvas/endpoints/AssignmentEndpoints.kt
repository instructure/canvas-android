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
import com.instructure.canvas.espresso.mockCanvas.utils.LongId
import com.instructure.canvas.espresso.mockCanvas.utils.PathVars
import com.instructure.canvas.espresso.mockCanvas.utils.Segment
import com.instructure.canvas.espresso.mockCanvas.utils.successResponse
import com.instructure.canvasapi2.models.Assignment

/**
 * Endpoint for assignment index, for a course
 *
 * ROUTES:
 * - `{assignmentId}` -> [Assignment]
 */
object AssignmentIndexEndpoint : Endpoint(
    LongId(PathVars::assignmentId) to AssignmentEndpoint,
    response = {
        GET {
            //TODO
            request.successResponse(listOf(Assignment()))
        }
    }
)

/**
 * Endpoint for a specific assignment for a course
 *
 * ROUTES:
 * - `submissions` -> [SubmissionIndexEndpoint]
 */
object AssignmentEndpoint : Endpoint(
    Segment("submissions") to SubmissionIndexEndpoint,
    response = {
        GET {
            //TODO
            request.successResponse(Assignment())
        }
    }
)

/**
 * Endpoint for assignment groups for a course
 */
object AssignmentGroupListEndpoint : Endpoint(
    LongId(PathVars::assignmentId) to AssignmentEndpoint,
    response = {
        GET {
            //TODO
            request.successResponse(listOf(Assignment()))
        }
    }
)
