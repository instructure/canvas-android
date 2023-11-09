/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.teacher.fragments

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.webkit.WebView
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.interactions.router.RouteContext.FILE
import com.instructure.pandautils.analytics.SCREEN_VIEW_SPEED_GRADER_QUIZ_WEB_VIEW
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.events.SubmissionUpdatedEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.transformForQuizGrading
import com.instructure.teacher.view.QuizSubmissionGradedEvent
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus

@ScreenView(SCREEN_VIEW_SPEED_GRADER_QUIZ_WEB_VIEW)
class SpeedGraderQuizWebViewFragment : InternalWebViewFragment() {

    private var mCourseId by LongArg()
    private var mAssignmentId by LongArg()
    private var mStudentId by LongArg()

    private var apiCall: Job? = null

    private var mPostedUpdate = false

    override fun onActivityCreated(savedInstanceState: Bundle?) = with(binding) {
        // Lock to portrait orientation due to the WebView not saving state
        (requireContext() as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

        setShouldAuthenticateUponLoad(true)
        shouldRouteInternally = false
        setShouldLoadUrl(false)
        canvasWebView.setInitialScale(100)
        super.onActivityCreated(savedInstanceState)
        canvasWebView.enableAlgorithmicDarkening()

        val originalCallback = canvasWebView.canvasWebViewClientCallback!!
        canvasWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback by originalCallback {
            override fun canRouteInternallyDelegate(url: String): Boolean {
                // Allow internal routing for canvas file links, otherwise openMediaFromWebView will be called
                // with the redirect URL and we won't have the metadata needed to properly view the file
                val isFileRoute = RouteMatcher.getInternalRoute(url, ApiPrefs.domain)?.routeContext == FILE
                return isFileRoute || originalCallback.canRouteInternallyDelegate(url)
            }

            override fun onPageFinishedCallback(webView: WebView, url: String) {
                originalCallback.onPageFinishedCallback(webView, url)
                if ("score_updated=1" in url) {
                    webView.clearHistory()
                    webView.setInvisible()
                    loading.setVisible()
                    if (webView.progress == 100 && !mPostedUpdate) {
                        mPostedUpdate = true
                        getUpdatedSubmission()
                    }
                }
            }
        }

        loadUrl(url)
    }

    private fun getUpdatedSubmission() {
        apiCall = weave {
            try {
                val updatedSubmission = awaitApi<Submission> { SubmissionManager.getSingleSubmission(mCourseId, mAssignmentId, mStudentId, it, true) }
                updatedSubmission.transformForQuizGrading()
                EventBus.getDefault().postSticky(QuizSubmissionGradedEvent(updatedSubmission))
                SubmissionUpdatedEvent(updatedSubmission).post()
            } catch (e: StatusCallbackError) {
                toast(R.string.error_saving_quiz)
            }
            activity?.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        apiCall?.cancel()
    }

    companion object {
        fun newInstance(courseId: Long, assignmentId: Long, studentId: Long, url: String)= SpeedGraderQuizWebViewFragment().apply {
            mCourseId = courseId
            mAssignmentId = assignmentId
            mStudentId = studentId
            this.url = url
        }

        fun newInstance(bundle: Bundle) = SpeedGraderQuizWebViewFragment().apply { arguments = bundle }
    }
}
