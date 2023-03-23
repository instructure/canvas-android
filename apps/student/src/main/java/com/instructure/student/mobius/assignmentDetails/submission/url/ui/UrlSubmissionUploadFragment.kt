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
package com.instructure.student.mobius.assignmentDetails.submission.url.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_URL_SUBMISSION_UPLOAD
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.student.databinding.FragmentUrlSubmissionUploadBinding
import com.instructure.student.mobius.assignmentDetails.submission.url.*
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.mobius.common.ui.MobiusFragment
import com.instructure.student.mobius.common.ui.Presenter
import com.instructure.student.mobius.common.ui.UpdateInit

@ScreenView(SCREEN_VIEW_URL_SUBMISSION_UPLOAD)
class UrlSubmissionUploadFragment : MobiusFragment<UrlSubmissionUploadModel, UrlSubmissionUploadEvent, UrlSubmissionUploadEffect, UrlSubmissionUploadView, UrlSubmissionUploadViewState, FragmentUrlSubmissionUploadBinding>() {

    private val course by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)
    private val assignmentId by LongArg(key = Const.ASSIGNMENT_ID)
    private val initialUrl by StringArg(key = Const.URL)
    private val assignmentName by StringArg(key = Const.ASSIGNMENT_NAME)
    private val isFailure by BooleanArg(key = Const.IS_FAILURE)

    override fun makeEffectHandler(): EffectHandler<UrlSubmissionUploadView, UrlSubmissionUploadEvent, UrlSubmissionUploadEffect> = UrlSubmissionUploadEffectHandler()

    override fun makeUpdate(): UpdateInit<UrlSubmissionUploadModel, UrlSubmissionUploadEvent, UrlSubmissionUploadEffect> = UrlSubmissionUploadUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup): UrlSubmissionUploadView = UrlSubmissionUploadView(inflater, parent)

    override fun makePresenter(): Presenter<UrlSubmissionUploadModel, UrlSubmissionUploadViewState> = UrlSubmissionUploadPresenter

    override fun makeInitModel(): UrlSubmissionUploadModel = UrlSubmissionUploadModel(course, assignmentId, assignmentName, initialUrl, isFailure)

    companion object {

        fun makeRoute(course: CanvasContext, assignmentId: Long, assignmentName: String? = "", initialUrl: String?, isFailure: Boolean = false): Route {
            val bundle = course.makeBundle{
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putString(Const.ASSIGNMENT_NAME, assignmentName)
                putString(Const.URL, initialUrl)
                putBoolean(Const.IS_FAILURE, isFailure)
            }

            return Route(null, UrlSubmissionUploadFragment::class.java, course, bundle)
        }

        fun validRoute(route: Route): Boolean {
            return route.canvasContext is Course &&
                    route.arguments.containsKey(Const.ASSIGNMENT_ID)
        }

        fun newInstance(route: Route): UrlSubmissionUploadFragment? {
            if (!validRoute(route)) return null
            return UrlSubmissionUploadFragment().withArgs(route.arguments)
        }
    }
}
