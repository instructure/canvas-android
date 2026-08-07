/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.pandautils.features.lti

/**
 * The Return button of an external tool navigates back to the page the tool was launched from. Tab based tools return
 * to the course home page, while assignment based tools (e.g. New Quizzes) return to the assignment list. In both
 * cases the screen we came from is already on the back stack, so the tool should be closed instead of routing to a
 * new destination.
 */
object LtiReturnLinkHelper {

    fun isReturnLink(url: String, contextLink: String, isAssignmentLti: Boolean): Boolean {
        val target = url.normalized()
        val context = contextLink.normalized()
        if (context.isEmpty()) return false
        return target == context || (isAssignmentLti && target == "$context/assignments")
    }

    private fun String.normalized() = substringBefore("?").substringBefore("#").trimEnd('/')
}
