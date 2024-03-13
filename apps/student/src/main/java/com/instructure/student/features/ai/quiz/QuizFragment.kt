/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.student.features.ai.quiz

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.router.Route
import com.instructure.pandautils.features.progress.ProgressViewModelAction
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.backgroundColor
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.features.ai.model.SummaryQuestions
import com.instructure.student.router.RouteMatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class QuizFragment : Fragment() {

    private val viewModel: QuizViewModel by viewModels()

    private var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val color = ThemePrefs.darker(canvasContext.backgroundColor)
        ViewStyler.setStatusBarDark(requireActivity(), color)
        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                QuizScreen(uiState, viewModel::handleAction, ThemePrefs.darker(color)) {
                    requireActivity().onBackPressed()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            withContext(Dispatchers.Main.immediate) {
                viewModel.events.collect { action ->
                    when (action) {
                        is QuizViewModelAction.QuizFinished -> {
                            QuizSummaryFragment.makeRoute(action.questions, canvasContext).let {
                                RouteMatcher.route(requireActivity(), it)
                            }
                            Log.d("asdasd", "Quiz finished, ${action.questions}")
                        }
                    }
                }
            }
        }
    }

    companion object {
        internal const val QUESTIONS = "QUESTIONS"

        fun newInstance(route: Route) = QuizFragment().withArgs(route.arguments)

        fun makeRoute(questions: List<SummaryQuestions>, canvasContext: CanvasContext): Route {
            val bundle = canvasContext.makeBundle {
                putParcelableArray(QuizSummaryFragment.QUESTIONS, questions.toTypedArray())
            }
            return Route(QuizFragment::class.java, null, bundle)
        }
    }
}