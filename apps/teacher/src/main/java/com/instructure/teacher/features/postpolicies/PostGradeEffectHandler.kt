/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.teacher.features.postpolicies

import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.PostPolicyManager
import com.instructure.canvasapi2.managers.ProgressManager
import com.instructure.canvasapi2.managers.SectionManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Progress
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.pandautils.features.speedgrader.grade.GradingEvent
import com.instructure.pandautils.features.speedgrader.grade.SpeedGraderGradingEventHandler
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.features.postpolicies.ui.PostGradeView
import com.instructure.teacher.mobius.common.ui.EffectHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PostGradeEffectHandler(private val speedGraderGradingEffectHandler: SpeedGraderGradingEventHandler, private val postPolicyManager: PostPolicyManager) : EffectHandler<PostGradeView, PostGradeEvent, PostGradeEffect>() {
    override fun accept(effect: PostGradeEffect) {
        launch {
            when (effect) {
                is PostGradeEffect.LoadData -> loadData(effect.assignment)
                is PostGradeEffect.HideGrades -> hideGrades(effect.assignmentId, effect.sectionIds)
                is PostGradeEffect.PostGrades -> postGrades(effect.assignmentId, effect.sectionIds, effect.gradedOnly)
                is PostGradeEffect.ShowGradesPosted -> {
                    launch {
                        speedGraderGradingEffectHandler.postEvent(GradingEvent.PostPolicyUpdated)
                    }
                    view?.showGradesPosted(effect.isHidingGrades, effect.assignmentId)
                }
                is PostGradeEffect.ShowPostFailed -> view?.showPostFailed(effect.isHidingGrades)
                is PostGradeEffect.WatchForProgress -> watchProgress(effect.progressId)
            }.exhaustive
        }
    }

    private suspend fun loadData(assignment: Assignment) {
        val sections = SectionManager.getAllSectionsForCourseAsync(assignment.courseId, true)
        val submissions = AssignmentManager.getAllSubmissionsForAssignmentAsync(assignment.courseId, assignment.id, true)

        consumer.accept(PostGradeEvent.DataLoaded(sections.await().dataOrNull ?: emptyList(), submissions.await().dataOrNull ?: emptyList()))
    }

    private suspend fun hideGrades(assignmentId: Long, sections: List<String>) {
        try {
            val progressId = if (sections.isEmpty()) {
                postPolicyManager.hideGradesAsync(assignmentId).hideAssignmentGrades?.progress?._id
            } else {
                postPolicyManager.hideGradesForSectionsAsync(assignmentId, sections).hideAssignmentGradesForSections?.progress?._id
            }

            consumer.accept(PostGradeEvent.PostStarted(progressId))
        } catch (_: Throwable) {
            consumer.accept(PostGradeEvent.PostFailed)
        }
    }

    private suspend fun postGrades(assignmentId: Long, sections: List<String>, gradedOnly: Boolean) {
        try {
            val progressId = if (sections.isEmpty()) {
                postPolicyManager.postGradesAsync(assignmentId, gradedOnly).postAssignmentGrades?.progress?._id
            } else {
                postPolicyManager.postGradesForSectionsAsync(assignmentId, gradedOnly, sections).postAssignmentGradesForSections?.progress?._id
            }

            consumer.accept(PostGradeEvent.PostStarted(progressId))
        } catch (_: Throwable) {
            consumer.accept(PostGradeEvent.PostFailed)
        }
    }

    private suspend fun watchProgress(progressId: String?) {
        if (progressId == null) { // If we didn't get a progress ID back, then it failed to post
            consumer.accept(PostGradeEvent.PostFailed)
            return
        }

        lateinit var progress: Progress
        do {
            delay(getProgressDelay()) // Don't hit the API too hard while monitoring progress

            progress = ProgressManager.getProgressAsync(progressId).await().dataOrNull ?: run {
                consumer.accept(PostGradeEvent.PostFailed)
                return
            }
        } while (!progress.hasRun) // Keep checking status until it's finished running

        if (progress.isCompleted) {
            consumer.accept(PostGradeEvent.GradesPosted)
        } else {
            consumer.accept(PostGradeEvent.PostFailed)
        }
    }

    // Don't inflate test times, only do a second delay if not testing
    private fun getProgressDelay() = if (BuildConfig.IS_TESTING) 0L else 1000L
}
