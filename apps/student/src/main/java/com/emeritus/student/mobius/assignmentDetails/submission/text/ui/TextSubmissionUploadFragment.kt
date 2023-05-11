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
package com.emeritus.student.mobius.assignmentDetails.submission.text.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.emeritus.student.mobius.assignmentDetails.submission.text.*
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_TEXT_SUBMISSION_UPLOAD
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.emeritus.student.mobius.assignmentDetails.submission.text.*
import com.emeritus.student.mobius.common.ui.MobiusFragment

@ScreenView(SCREEN_VIEW_TEXT_SUBMISSION_UPLOAD)
class TextSubmissionUploadFragment : MobiusFragment<TextSubmissionUploadModel, TextSubmissionUploadEvent, TextSubmissionUploadEffect, TextSubmissionUploadView, TextSubmissionUploadViewState>() {

    private val course by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)
    private val assignmentId by LongArg(key = Const.ASSIGNMENT_ID)
    private val initialText by StringArg(key = Const.TEXT)
    private val isFailure by BooleanArg(key = Const.IS_FAILURE)
    private val assignmentName by StringArg(key = Const.ASSIGNMENT_NAME)

    override fun makeEffectHandler() = TextSubmissionUploadEffectHandler()

    override fun makeUpdate() = TextSubmissionUploadUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = TextSubmissionUploadView(inflater, parent)

    override fun makePresenter() = TextSubmissionUploadPresenter

    override fun makeInitModel(): TextSubmissionUploadModel {
        return TextSubmissionUploadModel(course, assignmentId, assignmentName, initialText, isFailure)
    }

    override fun getExternalEventSources() = listOf(TextSubmissionUploadEventBusSource())

    override fun handleBackPressed(): Boolean {
        return view.onBackPressed()
    }

    companion object {

        fun makeRoute(course: CanvasContext, assignmentId: Long, assignmentName: String? = "", initialText: String? = null, isFailure: Boolean = false): Route {
            val bundle = course.makeBundle{
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putString(Const.ASSIGNMENT_NAME, assignmentName)
                putString(Const.TEXT, initialText)
                putBoolean(Const.IS_FAILURE, isFailure)
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
