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
package com.instructure.student.mobius.assignmentDetails.submission.url

import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.student.mobius.assignmentDetails.submission.url.ui.UrlSubmissionUploadView
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.mobius.common.ui.SubmissionHelper


class UrlSubmissionUploadEffectHandler(private val submissionHelper: SubmissionHelper) : EffectHandler<UrlSubmissionUploadView, UrlSubmissionUploadEvent, UrlSubmissionUploadEffect>() {
    override fun accept(effect: UrlSubmissionUploadEffect) {
        when (effect) {
            is UrlSubmissionUploadEffect.ShowUrlPreview -> {
                view?.showPreviewUrl(effect.url)
            }
            is UrlSubmissionUploadEffect.SubmitUrl -> {
                submissionHelper.startUrlSubmission(effect.course, effect.assignmentId, effect.assignmentName, effect.url, effect.attempt)
                view?.goBack()
            }
            is UrlSubmissionUploadEffect.InitializeUrl -> {
                view?.setInitialUrl(effect.url)
            }
        }.exhaustive
    }
}