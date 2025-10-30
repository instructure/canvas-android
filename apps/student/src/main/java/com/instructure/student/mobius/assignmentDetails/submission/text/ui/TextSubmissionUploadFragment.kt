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
 */package com.instructure.student.mobius.assignmentDetails.submission.text.ui

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionUploadEffectHandler
import com.instructure.student.mobius.common.ui.SubmissionHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TextSubmissionUploadFragment : BaseTextSubmissionUploadFragment() {

    @Inject
    lateinit var submissionHelper: SubmissionHelper

    override fun makeEffectHandler() = TextSubmissionUploadEffectHandler(submissionHelper)

    companion object {

        fun makeRoute(course: CanvasContext, assignmentId: Long, assignmentName: String? = "", initialText: String? = null, isFailure: Boolean = false, attempt: Long = 1L): Route {
            val bundle = course.makeBundle{
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putString(Const.ASSIGNMENT_NAME, assignmentName)
                putString(Const.TEXT, initialText)
                putBoolean(Const.IS_FAILURE, isFailure)
                putLong(Const.SUBMISSION_ATTEMPT, attempt)
            }

            return Route(null, TextSubmissionUploadFragment::class.java, course, bundle)
        }

        fun validRoute(route: Route): Boolean {
            return route.canvasContext is Course &&
                    route.arguments.containsKey(Const.ASSIGNMENT_ID)
        }

        fun newInstance(route: Route): TextSubmissionUploadFragment? {
            if (!validRoute(route)) return null
            return TextSubmissionUploadFragment().withArgs(route.arguments)
        }
    }
}