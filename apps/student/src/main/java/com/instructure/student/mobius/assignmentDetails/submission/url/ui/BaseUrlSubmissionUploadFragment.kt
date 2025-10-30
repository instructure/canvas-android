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
import com.instructure.pandautils.analytics.SCREEN_VIEW_URL_SUBMISSION_UPLOAD
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.student.databinding.FragmentUrlSubmissionUploadBinding
import com.instructure.student.mobius.assignmentDetails.submission.url.UrlSubmissionUploadEffect
import com.instructure.student.mobius.assignmentDetails.submission.url.UrlSubmissionUploadEvent
import com.instructure.student.mobius.assignmentDetails.submission.url.UrlSubmissionUploadModel
import com.instructure.student.mobius.assignmentDetails.submission.url.UrlSubmissionUploadPresenter
import com.instructure.student.mobius.assignmentDetails.submission.url.UrlSubmissionUploadUpdate
import com.instructure.student.mobius.common.ui.MobiusFragment
import com.instructure.student.mobius.common.ui.Presenter
import com.instructure.student.mobius.common.ui.UpdateInit

@ScreenView(SCREEN_VIEW_URL_SUBMISSION_UPLOAD)
abstract class BaseUrlSubmissionUploadFragment : MobiusFragment<UrlSubmissionUploadModel, UrlSubmissionUploadEvent, UrlSubmissionUploadEffect, UrlSubmissionUploadView, UrlSubmissionUploadViewState, FragmentUrlSubmissionUploadBinding>() {

    private val course by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)
    private val assignmentId by LongArg(key = Const.ASSIGNMENT_ID)
    private val initialUrl by StringArg(key = Const.URL)
    private val assignmentName by StringArg(key = Const.ASSIGNMENT_NAME)
    private val isFailure by BooleanArg(key = Const.IS_FAILURE)
    private val attempt by LongArg(key = Const.SUBMISSION_ATTEMPT, default = 1L)

    override fun makeUpdate(): UpdateInit<UrlSubmissionUploadModel, UrlSubmissionUploadEvent, UrlSubmissionUploadEffect> = UrlSubmissionUploadUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup): UrlSubmissionUploadView = UrlSubmissionUploadView(inflater, parent)

    override fun makePresenter(): Presenter<UrlSubmissionUploadModel, UrlSubmissionUploadViewState> = UrlSubmissionUploadPresenter

    override fun makeInitModel(): UrlSubmissionUploadModel = UrlSubmissionUploadModel(course, assignmentId, assignmentName, initialUrl, isFailure, attempt = attempt)
}
