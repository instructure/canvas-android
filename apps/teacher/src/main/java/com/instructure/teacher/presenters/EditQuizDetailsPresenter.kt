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

import com.instructure.canvasapi2.managers.*
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.postmodels.AssignmentPostBody
import com.instructure.canvasapi2.models.postmodels.QuizPostBody
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.teacher.events.QuizUpdatedEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.models.DueDateGroup
import com.instructure.teacher.viewinterface.EditQuizDetailsView
import com.instructure.pandautils.blueprint.FragmentPresenter
import kotlinx.coroutines.Job

class EditQuizDetailsPresenter(var mQuiz: Quiz, var mAssignment: Assignment, val canvasContext: CanvasContext) : FragmentPresenter<EditQuizDetailsView>() {

    private var mApiCalls: Job? = null
    private var mDueDateApiCalls: Job? = null
    private var mSaveQuizCall: Job? = null
    private var mGetAssignmentCall: Job? = null
    private var mGetQuizCall: Job? = null

    val groupsMapped = hashMapOf<Long, Group>()
    val sectionsMapped = hashMapOf<Long, Section>()
    val studentsMapped = hashMapOf<Long, User>()

    // We maintain a copy of the groupedDueDates to manipulate and use to display
    // overrides. When pushing changes, we update the original quiz object
    // with the changes in the copy.
    var mEditDateGroups: ArrayList<DueDateGroup> = arrayListOf()

    override fun refresh(forceNetwork: Boolean) = Unit

    override fun loadData(forceNetwork: Boolean) {
        if (forceNetwork) {
            if (mQuiz.assignmentId != 0L) {
                // Graded survey or quiz
                mApiCalls = weave {
                    mAssignment = awaitApi { AssignmentManager.getAssignment(mQuiz.assignmentId, canvasContext.id, true, it) }
                    mQuiz = awaitApi { QuizManager.getQuiz(canvasContext.id, mQuiz.id, true, it) }
                }
            } else {
                // Ungraded survey or practice quiz
                mApiCalls = weave {
                    mQuiz = awaitApi { QuizManager.getQuiz(canvasContext.id, mQuiz.id, true, it) }
                }
            }
        }

        viewCallback?.populateQuizDetails()
    }

    fun getStudentsGroupsAndSections() {
        mDueDateApiCalls = weave {
            try {
                if (groupsMapped.isEmpty() && sectionsMapped.isEmpty() && studentsMapped.isEmpty()) {

                    val sections = awaitApi<List<Section>> { SectionManager.getAllSectionsForCourse(canvasContext.id, it, false) }
                    val groups = if (mAssignment.groupCategoryId > 0L) awaitApi<List<Group>> { GroupCategoriesManager.getAllGroupsForCategory(mAssignment.groupCategoryId, it, false) } else emptyList()
                    val students = awaitApi<List<User>> { UserManager.getAllPeopleList(canvasContext, it, false) }
                    groupsMapped += groups.associateBy { it.id }
                    sectionsMapped += sections.associateBy { it.id }
                    studentsMapped += students.associateBy { it.id }
                }

                viewCallback?.setupAddOverridesButton()
                viewCallback?.setupOverrides()
                viewCallback?.scrollCheck()

            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun saveQuiz(quizPostData: QuizPostBody, assignmentPostData: AssignmentPostBody) {
        mSaveQuizCall = weave {
            try {
                // Save the quiz data
                mQuiz = awaitApi { QuizManager.editQuiz(canvasContext.id, mQuiz.id, quizPostData, it) }

                if (mQuiz.assignmentId != 0L) {
                    // This quiz has an assignment - update the overrides on the assignment
                    mAssignment = awaitApi { AssignmentManager.editAssignmentAllowNullValues(canvasContext.id, mQuiz.assignmentId, assignmentPostData, it) }
                }

                QuizUpdatedEvent(mQuiz.id).post() // Post bus event
                viewCallback?.quizSavedSuccessfully()

            } catch (e: Throwable) {
                e.printStackTrace()
                viewCallback?.errorSavingQuiz()
            }
        }
    }

    fun getAssignment(assignmentId: Long, courseId: Long) {
        mGetAssignmentCall = weave {
            mAssignment = awaitApi { AssignmentManager.getAssignment(assignmentId, courseId, true, it) }
            loadData(false)
        }
    }

    fun getQuiz(quizId: Long, courseId: Long) {
        mGetQuizCall = weave {
            mQuiz = awaitApi { QuizManager.getQuiz(courseId, quizId, true, it) }
            mAssignment = awaitApi { AssignmentManager.getAssignment(mQuiz.assignmentId, courseId, true, it) }
            loadData(false)
        }
    }

    override fun onDestroyed() {
        super.onDestroyed()
        mDueDateApiCalls?.cancel()
        mSaveQuizCall?.cancel()
        mGetAssignmentCall?.cancel()
    }
}
