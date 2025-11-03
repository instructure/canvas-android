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
package com.instructure.student.mobius.assignmentDetails.submission.text.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.analytics.SCREEN_VIEW_TEXT_SUBMISSION_UPLOAD
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.student.databinding.FragmentTextSubmissionUploadBinding
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionUploadEffect
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionUploadEvent
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionUploadEventBusSource
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionUploadModel
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionUploadPresenter
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionUploadUpdate
import com.instructure.student.mobius.common.ui.MobiusFragment

@ScreenView(SCREEN_VIEW_TEXT_SUBMISSION_UPLOAD)
abstract class BaseTextSubmissionUploadFragment : MobiusFragment<TextSubmissionUploadModel, TextSubmissionUploadEvent, TextSubmissionUploadEffect, TextSubmissionUploadView, TextSubmissionUploadViewState, FragmentTextSubmissionUploadBinding>() {

    private val course by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)
    private val assignmentId by LongArg(key = Const.ASSIGNMENT_ID)
    private val initialText by StringArg(key = Const.TEXT)
    private val isFailure by BooleanArg(key = Const.IS_FAILURE)
    private val assignmentName by StringArg(key = Const.ASSIGNMENT_NAME)
    private val attempt by LongArg(key = Const.SUBMISSION_ATTEMPT, default = 1L)

    override fun makeUpdate() = TextSubmissionUploadUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = TextSubmissionUploadView(inflater, parent)

    override fun makePresenter() = TextSubmissionUploadPresenter

    override fun makeInitModel(): TextSubmissionUploadModel {
        return TextSubmissionUploadModel(course, assignmentId, assignmentName, initialText, isFailure, attempt = attempt)
    }

    override fun getExternalEventSources() = listOf(TextSubmissionUploadEventBusSource())

    override fun handleBackPressed(): Boolean {
        return view.onBackPressed()
    }
}
