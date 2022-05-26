/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.managers.QuizManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_MODULE_QUIZ_DECIDER
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import com.instructure.student.router.RouteMatcher
import kotlinx.android.synthetic.main.fragment_module_quiz_decider.*

@ScreenView(SCREEN_VIEW_MODULE_QUIZ_DECIDER)
class ModuleQuizDecider : ParentFragment() {

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var baseURL by StringArg(key = Const.URL)
    private var apiURL by StringArg(key = Const.API_URL)

    private lateinit var quiz: Quiz

    private var obtainQuizJob: WeaveJob? = null

    private var webViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
        override fun openMediaFromWebView(mime: String, url: String, filename: String) {
            openMedia(mime, url, filename, canvasContext)
        }

        override fun onPageFinishedCallback(webView: WebView, url: String) = Unit

        override fun onPageStartedCallback(webView: WebView, url: String) = Unit

        override fun canRouteInternallyDelegate(url: String): Boolean {
            return RouteMatcher.canRouteInternally(requireContext(), url, ApiPrefs.domain, false)
        }

        override fun routeInternallyCallback(url: String) {
            RouteMatcher.canRouteInternally(requireContext(), url, ApiPrefs.domain, true)
        }
    }

    private var embeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
        override fun launchInternalWebViewFragment(url: String) {
            InternalWebviewFragment.loadInternalWebView(
                activity,
                InternalWebviewFragment.makeRoute(canvasContext, url, false)
            )
        }

        override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
    }

    override fun title(): String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_module_quiz_decider, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        obtainQuiz()
    }

    override fun onDestroyView() {
        obtainQuizJob?.cancel()
        super.onDestroyView()
    }

    override fun applyTheme() {
        toolbar.title = if (this::quiz.isInitialized) quiz.title else getString(R.string.quizzes)
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbarColored(requireActivity(), toolbar, canvasContext)
    }

    private fun obtainQuiz() {
        tryWeave {
            quizInfoContainer.setGone()
            progressBar.setVisible()
            quiz = awaitApi { QuizManager.getDetailedQuizByUrl(apiURL, true, it) }
            quizInfoContainer.setVisible()
            progressBar.setGone()
            quizTitle.text = quiz.title
            if (quiz.dueAt != null) {
                quizDueDetails.text = DateHelper.getDateTimeString(activity, quiz.dueDate)
            } else {
                quizDue.setGone()
                quizDueDetails.text = getString(R.string.toDoNoDueDate)
            }
            quizDetails.loadHtml(quiz.description.orEmpty(), "")
            quizDetails.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.transparent))

            // Set some callbacks in case there is a link in the quiz description. We want it to open up in a new InternalWebViewFragment
            quizDetails.canvasEmbeddedWebViewCallback = embeddedWebViewCallback
            quizDetails.canvasWebViewClientCallback = webViewClientCallback

            setupViews()
        } catch {
            toast(R.string.errorOccurred)
            activity?.finish()
        }
    }

    private fun setupViews() {
        toolbar.title = quiz.title.validOrNull() ?: getString(R.string.quizzes)

        ViewStyler.themeButton(goToQuiz)
        goToQuiz.onClick {
            val route = BasicQuizViewFragment.makeRoute(canvasContext, quiz, baseURL)
            route.ignoreDebounce = true
            RouteMatcher.route(requireContext(), route)
        }
    }

    companion object {

        fun makeRoute(canvasContext: CanvasContext, url: String, apiURL: String): Route {
            val bundle = Bundle().apply {
                putString(Const.URL, url)
                putString(Const.API_URL, apiURL)
            }
            return Route(ModuleQuizDecider::class.java, canvasContext, bundle)
        }

        private fun validateRoute(route: Route): Boolean {
            return route.canvasContext != null
                    && route.arguments.containsKey(Const.URL)
                    && route.arguments.containsKey(Const.API_URL)
        }

        fun newInstance(route: Route): ModuleQuizDecider? {
            if (!validateRoute(route)) return null
            return ModuleQuizDecider().withArgs(route.argsWithContext)
        }

    }
}
