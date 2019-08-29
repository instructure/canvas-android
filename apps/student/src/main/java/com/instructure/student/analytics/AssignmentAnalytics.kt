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
package com.instructure.student.analytics

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.mobius.assignmentDetails.AssignmentDetailsEffect

fun logAssignmentAnalytics(effect: AssignmentDetailsEffect.LoadData, quizResult: DataResult<Quiz>?, assignmentResult: DataResult<Assignment>) {
    val analyticString = when {
        quizResult != null -> AnalyticsEventConstants.ASSIGNMENT_DETAIL_QUIZ
        assignmentResult.dataOrNull?.discussionTopicHeader != null -> AnalyticsEventConstants.ASSIGNMENT_DETAIL_DISCUSSION
        else -> AnalyticsEventConstants.ASSIGNMENT_DETAIL_ASSIGNMENT
    }

    Analytics.logEvent(
        analyticString,
        Analytics.createAssignmentAnalyticsBundle(
            domain = ApiPrefs.fullDomain,
            userId = ApiPrefs.user!!.contextId,
            canvasContextId = CanvasContext.makeContextId(CanvasContext.Type.COURSE, effect.courseId),
            assignmentId = effect.assignmentId
        )
    )
}

fun logAssignmentQuizAnalytics(effect: AssignmentDetailsEffect.ShowQuizStartView) {
    Analytics.logEvent(
        AnalyticsEventConstants.ASSIGNMENT_DETAIL_QUIZLAUNCH,
        Analytics.createAssignmentAnalyticsBundle(
            domain = ApiPrefs.fullDomain,
            userId = ApiPrefs.user!!.contextId,
            canvasContextId = effect.course.contextId,
            assignmentId = effect.quiz.assignmentId
        )
    )
}

fun logAssignmentDiscussionAnalytics(effect: AssignmentDetailsEffect.ShowDiscussionDetailView, assignmentId: Long) {
    Analytics.logEvent(
        AnalyticsEventConstants.ASSIGNMENT_DETAIL_DISCUSSIONLAUNCH,
        Analytics.createAssignmentAnalyticsBundle(
            domain = ApiPrefs.fullDomain,
            userId = ApiPrefs.user!!.contextId,
            canvasContextId = effect.course.contextId,
            assignmentId = assignmentId
        )
    )
}

fun logAssignmentSubmitClicked(effect: AssignmentDetailsEffect.ShowSubmitDialogView) {
    Analytics.logEvent(
            AnalyticsEventConstants.ASSIGNMENT_SUBMIT_SELECTED,
            Analytics.createAssignmentAnalyticsBundle(
                    domain = ApiPrefs.fullDomain,
                    userId = ApiPrefs.user!!.contextId,
                    canvasContextId = effect.course.contextId,
                    assignmentId = effect.assignment.id
            )
    )
}
