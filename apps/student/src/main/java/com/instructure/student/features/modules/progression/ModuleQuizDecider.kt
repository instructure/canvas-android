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

package com.instructure.student.features.modules.progression

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_MODULE_QUIZ_DECIDER
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.argsWithContext
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withArgs
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import com.instructure.student.databinding.FragmentModuleQuizDeciderBinding
import com.instructure.student.fragment.BasicQuizViewFragment
import com.instructure.student.fragment.InternalWebviewFragment
import com.instructure.student.fragment.ParentFragment
import com.instructure.student.router.RouteMatcher
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_MODULE_QUIZ_DECIDER)
@AndroidEntryPoint
class ModuleQuizDecider : ParentFragment() {

    private val binding by viewBinding(FragmentModuleQuizDeciderBinding::bind)

    @Inject
    lateinit var repository: ModuleProgressionRepository

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var baseURL by StringArg(key = Const.URL)
    private var apiURL by StringArg(key = Const.API_URL)
    private var quizId by LongArg(key = Const.ID)

    private lateinit var quiz: Quiz

    private var obtainQuizJob: WeaveJob? = null

    private var webViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
        override fun openMediaFromWebView(mime: String, url: String, filename: String) {
            openMedia(mime, url, filename, null, canvasContext)
        }

        override fun onPageFinishedCallback(webView: WebView, url: String) = Unit

        override fun onPageStartedCallback(webView: WebView, url: String) = Unit

        override fun canRouteInternallyDelegate(url: String): Boolean {
            return RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, false)
        }

        override fun routeInternallyCallback(url: String) {
            RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, true)
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
        with (binding) {
            toolbar.title = if (this@ModuleQuizDecider::quiz.isInitialized) quiz.title else getString(R.string.quizzes)
            toolbar.setupAsBackButton(this@ModuleQuizDecider)
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, canvasContext)
        }
    }

    private fun obtainQuiz() = with(binding) {
        lifecycleScope.tryLaunch {
            quizInfoContainer.setGone()
            progressBar.setVisible()
            quiz = repository.getDetailedQuiz(apiURL, quizId, true)
            quizInfoContainer.setVisible()
            progressBar.setGone()
            quizTitle.text = quiz.title
            if (quiz.dueAt != null) {
                quizDueDetails.text = DateHelper.getDateTimeString(activity, quiz.dueDate)
            } else {
                quizDue.setGone()
                quizDueDetails.text = getString(R.string.toDoNoDueDate)
            }
            quizDetailsWrapper.loadHtml(quiz.description.orEmpty(), "")
            quizDetailsWrapper.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.transparent))

            // Set some callbacks in case there is a link in the quiz description. We want it to open up in a new InternalWebViewFragment
            quizDetailsWrapper.webView.canvasEmbeddedWebViewCallback = embeddedWebViewCallback
            quizDetailsWrapper.webView.canvasWebViewClientCallback = webViewClientCallback

            setupViews()
        } catch {
            toast(R.string.errorOccurred)
        }
    }

    private fun setupViews() {
        binding.toolbar.title = quiz.title.validOrNull() ?: getString(R.string.quizzes)

        ViewStyler.themeButton(binding.goToQuiz)
        binding.goToQuiz.onClickWithRequireNetwork {
            val route = BasicQuizViewFragment.makeRoute(canvasContext, quiz, baseURL)
            route.ignoreDebounce = true
            RouteMatcher.route(requireActivity(), route)
        }
    }

    companion object {

        fun makeRoute(canvasContext: CanvasContext, url: String, apiURL: String, quizId: Long): Route {
            val bundle = Bundle().apply {
                putString(Const.URL, url)
                putString(Const.API_URL, apiURL)
                putLong(Const.ID, quizId)
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
