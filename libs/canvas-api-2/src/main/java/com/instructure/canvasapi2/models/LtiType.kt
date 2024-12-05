/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.canvasapi2.models

import com.instructure.canvasapi2.R

enum class LtiType(
    val openInternally: Boolean,
    val assignmentIconRes: Int,
    val openButtonRes: Int,
    val ltiTitleRes: Int,
    val ltiDescriptionRes: Int,
    val submissionTypeRes: Int,
    val id: String?
) {
    NEW_QUIZZES_LTI(
        openInternally = true,
        assignmentIconRes = R.drawable.ic_quiz,
        openButtonRes = R.string.openTheQuizButton,
        ltiTitleRes = R.string.newQuizSubmissionTitle,
        ltiDescriptionRes = R.string.newQuizSubmissionSubtitle,
        submissionTypeRes = R.string.canvasAPI_quiz,
        id = "quiz-lti"
    ),
    EXTERNAL_TOOL(
        openInternally = false,
        assignmentIconRes = R.drawable.ic_assignment,
        openButtonRes = R.string.openTool,
        ltiTitleRes = R.string.commentSubmissionTypeExternalTool,
        ltiDescriptionRes = R.string.speedGraderExternalToolMessage,
        submissionTypeRes = R.string.canvasAPI_externalTool,
        id = null
    )
}