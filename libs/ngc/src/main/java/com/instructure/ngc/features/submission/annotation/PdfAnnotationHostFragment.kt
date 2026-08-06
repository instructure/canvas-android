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
package com.instructure.ngc.features.submission.annotation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Lightweight host [Fragment] whose sole purpose is to own a [childFragmentManager] scoped to this
 * feature, so the PSPDFKit `PdfFragment` hosted inline by [PdfAnnotationSubmissionView] is committed
 * into a FragmentManager that belongs to THIS fragment rather than the host Activity's
 * `supportFragmentManager` (see MBL-19937).
 *
 * Why this matters: committing the `PdfFragment` directly into the Activity FragmentManager means a
 * configuration change auto-restores it before the Compose-hosted container is guaranteed to be laid
 * out, risking a "No view found for id ..." crash or an orphaned fragment. The Fragment framework only
 * restores a fragment's *children* after that fragment's own view has been (re)created, so nesting the
 * `PdfFragment` one level down — inside this host fragment's [childFragmentManager] — means it can never
 * be restored ahead of its container: [onCreateView] (and therefore [PdfAnnotationSubmissionView]'s own
 * `fragmentContainer`) always runs first.
 *
 * Hosted from Compose via the [androidx.fragment.compose.AndroidFragment] composable
 * (`androidx.fragment:fragment-compose`), the actively-maintained, non-deprecated mechanism for
 * embedding a Fragment in a Compose tree; it already handles the container id / saved-state bookkeeping
 * for this fragment itself across configuration changes, so only the grandchild `PdfFragment`
 * relationship needed hardening here.
 */
class PdfAnnotationHostFragment : Fragment() {

    private var pdfAnnotationSubmissionView: PdfAnnotationSubmissionView? = null

    // Held independently of pdfAnnotationSubmissionView so a call to setOnShowAnnotationComments()
    // that arrives BEFORE onCreateView() (e.g. the very first Compose composition/recomposition racing
    // AndroidFragment's own inflation) is not lost — it's applied to the view as soon as it's created.
    private var pendingOnShowAnnotationComments: ((CommentThreadRequest) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val courseId = requireArguments().getLong(ARG_COURSE_ID)
        val sessionUrl = requireArguments().getString(ARG_SESSION_URL).orEmpty()
        return PdfAnnotationSubmissionView(
            context = requireActivity(),
            fragmentManager = childFragmentManager,
            courseId = courseId,
            sessionUrl = sessionUrl,
        ).also {
            it.onShowAnnotationComments = pendingOnShowAnnotationComments
            pdfAnnotationSubmissionView = it
        }
    }

    override fun onDestroyView() {
        pdfAnnotationSubmissionView = null
        super.onDestroyView()
    }

    /** Awaits any in-flight annotation writes on the hosted view; `true`/no-op success if no view is attached. */
    suspend fun flushAnnotations(): Boolean = pdfAnnotationSubmissionView?.flushAnnotations() ?: true

    /**
     * Registers (or clears, when `null`) the callback invoked when the user opens a multi-reply comment
     * thread (see [PdfAnnotationSubmissionView.onShowAnnotationComments]). Safe to call whether or not
     * the hosted view has been created yet (see [pendingOnShowAnnotationComments]); reapplies on every
     * call so the Compose layer can freely re-register a fresh callback (e.g. capturing a new
     * `NavController` reference) on every recomposition, mirroring [flushAnnotations]'s registration
     * pattern in [StudentAnnotationContent].
     */
    fun setOnShowAnnotationComments(callback: ((CommentThreadRequest) -> Unit)?) {
        pendingOnShowAnnotationComments = callback
        pdfAnnotationSubmissionView?.onShowAnnotationComments = callback
    }

    /**
     * Delivers the outcome of a Compose-native comment-thread visit to the hosted view (see
     * [PdfAnnotationSubmissionView.onCommentThreadResult]); a no-op if no view is currently attached
     * (e.g. the host fragment/view was recreated while the thread screen was open).
     */
    fun deliverCommentThreadResult(result: CommentThreadResult) {
        pdfAnnotationSubmissionView?.onCommentThreadResult(result)
    }

    companion object {
        private const val ARG_COURSE_ID = "courseId"
        private const val ARG_SESSION_URL = "sessionUrl"

        fun createArguments(courseId: Long, sessionUrl: String): Bundle = Bundle().apply {
            putLong(ARG_COURSE_ID, courseId)
            putString(ARG_SESSION_URL, sessionUrl)
        }
    }
}
