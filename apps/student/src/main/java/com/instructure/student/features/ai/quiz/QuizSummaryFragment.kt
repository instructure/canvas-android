/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.student.features.ai.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.features.ai.model.SummaryQuestions
import com.instructure.student.features.ai.quiz.composables.QuizSummaryScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuizSummaryFragment : Fragment() {

    private val viewModel: QuizSummaryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                QuizSummaryScreen(uiState)
            }
        }
    }

    companion object {
        internal const val QUESTIONS = "QUESTIONS"

        fun newInstance(route: Route) = QuizSummaryFragment().withArgs(route.arguments)

        fun makeRoute(questions: List<SummaryQuestions>): Route {
            val bundle = bundleOf(QUESTIONS to questions.toTypedArray())
            return Route(QuizSummaryFragment::class.java, null, bundle)
        }
    }
}
