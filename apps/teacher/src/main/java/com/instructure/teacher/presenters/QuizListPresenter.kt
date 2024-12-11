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
package com.instructure.teacher.presenters

import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.QuizManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.filterWithQuery
import com.instructure.canvasapi2.utils.weave.awaitApis
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.teacher.viewinterface.QuizListView
import com.instructure.pandautils.blueprint.SyncExpandablePresenter
import kotlinx.coroutines.Job

class QuizListPresenter(private val mCanvasContext: CanvasContext) :
    SyncExpandablePresenter<String, Quiz, QuizListView>(String::class.java, Quiz::class.java) {

    var apiCalls: Job? = null

    private var assignmentsByQuizId: Map<Long, Assignment> = emptyMap()
    private var quizList: List<Quiz>? = null

    var searchQuery = ""
        set(value) {
            field = value
            clearData()
            populateData()
        }

    override fun loadData(forceNetwork: Boolean) {
        if (data.size() > 0 && !forceNetwork) return
        // it's possible for a teacher to change the quiz type and it could create an empty group.
        // This will fix that
        if(forceNetwork) {
            clearData()
        }
        if (apiCalls?.isActive ?: false) {
            apiCalls?.invokeOnCompletion { performLoad(forceNetwork) }
        } else {
            performLoad(forceNetwork)
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun performLoad(forceNetwork: Boolean) {
        apiCalls = tryWeave {
            onRefreshStarted()
            // Get assignments and quizzes
            val (assignments, quizzes) = awaitApis<List<Assignment>, List<Quiz>>(
                { AssignmentManager.getAllAssignments(mCanvasContext.id, forceNetwork, it) },
                { QuizManager.getAllQuizzes(mCanvasContext.id, forceNetwork, it) }
            )
            assignmentsByQuizId = assignments.filter { it.quizId > 0 }.associateBy { it.quizId }
            quizList = quizzes
            populateData()
        } catch {
            viewCallback?.onRefreshFinished()
            viewCallback?.checkIfEmpty()
            viewCallback?.displayLoadingError()
        }
    }

    override fun refresh(forceNetwork: Boolean) {
        apiCalls?.cancel()
        clearData()
        loadData(forceNetwork)
    }

    private fun populateData() {
        val quizzes = quizList ?: return
        quizzes.filterWithQuery(searchQuery, Quiz::title)
            .onEach { it._assignment = assignmentsByQuizId[it.id] }
            .map { mapNewQuizzes(it) }
            .groupBy { it.quizType }
            .forEach { (quizType, quizList) ->
                data.addOrUpdateAllItems(quizType!!, quizList)
            }
        viewCallback?.onRefreshFinished()
        viewCallback?.checkIfEmpty()
    }

    private fun mapNewQuizzes(quiz: Quiz): Quiz {
        return if (quiz.quizType == Quiz.TYPE_NEW_QUIZZES) {
            quiz.copy(quizType = Quiz.TYPE_ASSIGNMENT)
        } else {
            quiz
        }
    }

    override fun onDestroyed() {
        super.onDestroyed()
        apiCalls?.cancel()
    }

    override fun compare(group: String, quiz1: Quiz, quiz2: Quiz) = quiz1.compareTo(quiz2)
}
