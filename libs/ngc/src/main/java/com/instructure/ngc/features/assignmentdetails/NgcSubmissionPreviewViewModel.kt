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
package com.instructure.ngc.features.assignmentdetails

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.utils.filecache.FileCache
import com.instructure.pandautils.utils.filecache.awaitFileDownload
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Resolves the inline submission preview for the currently selected attempt on the NGC Assignment
 * Details screen. Most preview types render straight from the submission (text/URL) or stream from a
 * remote URL (image/media), so they resolve synchronously; PDFs and text files are downloaded via
 * [FileCache] first, showing [NgcSubmissionPreviewUiState.Loading] meanwhile.
 */
@HiltViewModel
class NgcSubmissionPreviewViewModel @Inject constructor(
    private val fileCache: FileCache,
    private val annotatedPdfUseCase: NgcAnnotatedPdfUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<NgcSubmissionPreviewUiState>(NgcSubmissionPreviewUiState.NoPreview)
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null

    /**
     * Resolves the preview for [submission]. Cancels any in-flight load for a previous attempt.
     * [courseId] is only needed to resolve the CanvaDoc domain for annotated-PDF submissions.
     */
    fun load(submission: Submission?, baseUrl: String?, courseId: Long) {
        loadJob?.cancel()
        when (val plan = mapSubmissionPreviewPlan(submission, baseUrl)) {
            is NgcSubmissionPreviewPlan.NoPreview -> emit(NgcSubmissionPreviewUiState.NoPreview)
            is NgcSubmissionPreviewPlan.NoFilePreview -> emit(NgcSubmissionPreviewUiState.NoFilePreview(plan.fileNames))
            is NgcSubmissionPreviewPlan.Image -> emit(
                NgcSubmissionPreviewUiState.Image(plan.url.toUri(), plan.displayName, plan.additionalFileCount)
            )
            is NgcSubmissionPreviewPlan.Media -> emit(
                NgcSubmissionPreviewUiState.Media(
                    plan.url.toUri(), plan.thumbnailUrl, plan.contentType, plan.displayName, plan.additionalFileCount,
                )
            )

            is NgcSubmissionPreviewPlan.TextEntry -> emit(
                NgcSubmissionPreviewUiState.Text(
                    content = plan.html,
                    contentType = "text/html",
                    useInAppFormatting = true,
                    baseUrl = plan.baseUrl,
                )
            )

            is NgcSubmissionPreviewPlan.Url -> emit(NgcSubmissionPreviewUiState.Url(plan.url))
            is NgcSubmissionPreviewPlan.Pdf -> loadPdf(plan.url, plan.additionalFileCount)
            is NgcSubmissionPreviewPlan.TextFile -> loadTextFile(plan.url, plan.contentType, plan.additionalFileCount)
            is NgcSubmissionPreviewPlan.AnnotatedPdf -> loadAnnotatedPdf(plan.submissionId, plan.attempt, courseId)
        }
    }

    private fun loadAnnotatedPdf(submissionId: Long, attempt: Long, courseId: Long) {
        emit(NgcSubmissionPreviewUiState.Loading)
        loadJob = viewModelScope.tryLaunch {
            val result = annotatedPdfUseCase(NgcAnnotatedPdfUseCase.Params(submissionId, attempt, courseId))
            emit(
                if (result == null) NgcSubmissionPreviewUiState.NoPreview
                else NgcSubmissionPreviewUiState.AnnotatedPdf(result.uri, result.annotations)
            )
        } catch {
            emit(NgcSubmissionPreviewUiState.NoPreview)
        }
    }

    private fun loadPdf(url: String, additionalFileCount: Int) {
        emit(NgcSubmissionPreviewUiState.Loading)
        loadJob = viewModelScope.tryLaunch {
            val file = fileCache.awaitFileDownload(url)
            emit(
                if (file == null) NgcSubmissionPreviewUiState.NoPreview
                else NgcSubmissionPreviewUiState.Pdf(Uri.fromFile(file), additionalFileCount)
            )
        } catch {
            emit(NgcSubmissionPreviewUiState.NoPreview)
        }
    }

    private fun loadTextFile(url: String, contentType: String, additionalFileCount: Int) {
        emit(NgcSubmissionPreviewUiState.Loading)
        loadJob = viewModelScope.tryLaunch {
            val file = fileCache.awaitFileDownload(url)
            emit(
                if (file == null) {
                    NgcSubmissionPreviewUiState.NoPreview
                } else {
                    NgcSubmissionPreviewUiState.Text(
                        content = file.readText(),
                        contentType = contentType,
                        useInAppFormatting = false,
                        baseUrl = null,
                        additionalFileCount = additionalFileCount,
                    )
                }
            )
        } catch {
            emit(NgcSubmissionPreviewUiState.NoPreview)
        }
    }

    private fun emit(state: NgcSubmissionPreviewUiState) {
        _uiState.value = state
    }
}
