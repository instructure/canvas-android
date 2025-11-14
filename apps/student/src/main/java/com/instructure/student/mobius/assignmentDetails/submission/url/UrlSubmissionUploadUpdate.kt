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

import androidx.core.util.PatternsCompat
import com.instructure.student.mobius.common.ui.UpdateInit
import com.spotify.mobius.Effects
import com.spotify.mobius.First
import com.spotify.mobius.Next

class UrlSubmissionUploadUpdate : UpdateInit<UrlSubmissionUploadModel, UrlSubmissionUploadEvent, UrlSubmissionUploadEffect>() {
    override fun performInit(model: UrlSubmissionUploadModel): First<UrlSubmissionUploadModel, UrlSubmissionUploadEffect> {
        return First.first(model, setOf<UrlSubmissionUploadEffect>(UrlSubmissionUploadEffect.InitializeUrl(model.initialUrl)))
    }

    override fun update(model: UrlSubmissionUploadModel, event: UrlSubmissionUploadEvent): Next<UrlSubmissionUploadModel, UrlSubmissionUploadEffect> {
        val showEmptyPreviewEffect = UrlSubmissionUploadEffect.ShowUrlPreview("")
        return when(event) {
            is UrlSubmissionUploadEvent.UrlChanged -> {
                var url = event.url

                if (url.contains("http://", true)) {
                    // Cleartext URLs won't render in a WebView (unless it redirects to https) - let's just show an error and clear the preview
                    return Next.next(model.copy(isSubmittable = true, urlError = MalformedUrlError.CLEARTEXT), setOf(showEmptyPreviewEffect))
                }

                if (!isValidUrl(url)) {
                    // Url is not valid
                    return Next.next(model.copy(isSubmittable = false, urlError = MalformedUrlError.NONE), setOf(showEmptyPreviewEffect))
                }

                // Prepend 'https://' if the URL doesn't already have it
                if (!url.contains("https://", true)) {
                    // Adding https to the front if it's not there
                    url = "https://$url"
                }

                Next.next(model.copy(isSubmittable = true, urlError = MalformedUrlError.NONE), setOf(UrlSubmissionUploadEffect.ShowUrlPreview(url)))
            }
            is UrlSubmissionUploadEvent.SubmitClicked -> {
                Next.dispatch(Effects.effects((UrlSubmissionUploadEffect.SubmitUrl(event.url, model.course, model.assignmentId, model.assignmentName, model.attempt)) as UrlSubmissionUploadEffect))
            }
        }
    }

    private fun isValidUrl(url: String?) = !(url.isNullOrBlank() || !PatternsCompat.WEB_URL.matcher(url).matches())
}