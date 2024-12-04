/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */
package com.instructure.teacher.presenters

import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.QuizManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.teacher.viewinterface.QuizDetailsView
import com.instructure.pandautils.blueprint.FragmentPresenter
import kotlinx.coroutines.Job

class QuizDetailsPresenter(val mCourse: Course, var mQuiz: Quiz) : FragmentPresenter<QuizDetailsView>() {

    var mApiCalls: Job? = null
    var mQuizCall: Job? = null

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    override fun loadData(forceNetwork: Boolean) {
        mApiCalls?.cancel()
        mApiCalls = weave {
            viewCallback?.onRefreshStarted()
            try {
                if (!forceNetwork) {
                    viewCallback?.populateQuizDetails(mQuiz)
                } else {
                    mQuiz = awaitApi<Quiz> { QuizManager.getQuiz(mCourse.id, mQuiz.id, forceNetwork, it) }
                }

                // check to see if we need to get the assignment
                if (mQuiz.isGradeable && mQuiz._assignment == null && mQuiz.assignmentId > 0) {
                    //now get the assignment
                    val assignment = awaitApi<Assignment> { AssignmentManager.getAssignment(mQuiz.assignmentId, mCourse.id, forceNetwork, it) }
                    mQuiz._assignment = assignment
                }

                if (mQuiz._assignmentGroup == null && mQuiz.assignmentGroupId != 0L) {
                    mQuiz._assignmentGroup = awaitApi<AssignmentGroup> {
                        AssignmentManager.getAssignmentGroup(mCourse.id, mQuiz.assignmentGroupId, forceNetwork, it)
                    }
                }
                viewCallback?.populateQuizDetails(mQuiz)

                val submissionSummary = awaitApi<SubmissionSummary> { SubmissionManager.getSubmissionSummary(mCourse.id, mQuiz.assignmentId, forceNetwork, it) }
                val totalStudents = submissionSummary.graded + submissionSummary.ungraded + submissionSummary.notSubmitted
                viewCallback?.updateSubmissionDonuts(totalStudents, submissionSummary.graded, submissionSummary.ungraded, submissionSummary.notSubmitted)
            } catch (e: Throwable) {
                Logger.e(e.message)
                viewCallback?.updateSubmissionDonuts(0, 0, 0, 0)
            }
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun getQuiz(quizId: Long, course: Course, forceNetwork: Boolean) {
        mQuizCall = tryWeave {
            mQuiz = awaitApi<Quiz> { QuizManager.getQuiz(course.id, quizId, forceNetwork, it) }
            if (mQuiz._assignmentGroup == null && mQuiz.assignmentGroupId != 0L) {
                mQuiz._assignmentGroup = awaitApi<AssignmentGroup> {
                    AssignmentManager.getAssignmentGroup(mCourse.id, mQuiz.assignmentGroupId, forceNetwork, it)
                }
            }
            loadData(forceNetwork)
        } catch {
            // Show error?
            it.printStackTrace()
        }
    }

    override fun refresh(forceNetwork: Boolean) {}

    override fun onDestroyed() {
        super.onDestroyed()
        mApiCalls?.cancel()
        mQuizCall?.cancel()
    }
}
