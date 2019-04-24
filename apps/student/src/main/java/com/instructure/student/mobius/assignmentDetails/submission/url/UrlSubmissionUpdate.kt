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

import com.instructure.student.mobius.common.ui.UpdateInit
import com.spotify.mobius.Effects
import com.spotify.mobius.First
import com.spotify.mobius.Next

class UrlSubmissionUpdate : UpdateInit<UrlSubmissionModel, UrlSubmissionEvent, UrlSubmissionEffect>() {
    override fun performInit(model: UrlSubmissionModel): First<UrlSubmissionModel, UrlSubmissionEffect> {
        return First.first(model, setOf<UrlSubmissionEffect>(UrlSubmissionEffect.InitializeUrl(model.initialUrl ?: "")))
    }

    override fun update(model: UrlSubmissionModel, event: UrlSubmissionEvent): Next<UrlSubmissionModel, UrlSubmissionEffect> {
        return when(event) {
            is UrlSubmissionEvent.UrlChanged -> {
                Next.next(model.copy(isSubmittable = event.url.isNotEmpty()))
            }
            is UrlSubmissionEvent.SubmitClicked -> {
                Next.dispatch(Effects.effects(UrlSubmissionEffect.SubmitUrl(event.url, model.courseId, model.assignmentId)))
            }
        }
    }
}