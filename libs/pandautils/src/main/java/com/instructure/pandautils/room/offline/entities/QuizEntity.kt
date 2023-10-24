/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.Quiz

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class QuizEntity(
    @PrimaryKey
    val id: Long,
    val title: String?,
    val mobileUrl: String?,
    val htmlUrl: String?,
    val description: String?,
    val quizType: String?,
    val assignmentGroupId: Long,
    // TODO val lockInfo: LockInfo?,
    // TODO val permissions: QuizPermission?,
    val allowedAttempts: Int,
    val questionCount: Int,
    val pointsPossible: String?,
    val isLockQuestionsAfterAnswering: Boolean,
    val dueAt: String?,
    val timeLimit: Int,
    val shuffleAnswers: Boolean,
    val showCorrectAnswers: Boolean,
    val scoringPolicy: String?,
    val accessCode: String?,
    val ipFilter: String?,
    val lockedForUser: Boolean,
    val lockExplanation: String?,
    val hideResults: String?,
    val showCorrectAnswersAt: String?,
    val hideCorrectAnswersAt: String?,
    val unlockAt: String?,
    val oneTimeResults: Boolean,
    val lockAt: String?,
    val questionTypes: List<String>,
    val hasAccessCode: Boolean,
    val oneQuestionAtATime: Boolean,
    val requireLockdownBrowser: Boolean,
    val requireLockdownBrowserForResults: Boolean,
    val allowAnonymousSubmissions: Boolean,
    val published: Boolean,
    val assignmentId: Long,
    val isOnlyVisibleToOverrides: Boolean,
    val unpublishable: Boolean,
    val courseId: Long
) {
    constructor(quiz: Quiz, courseId: Long) : this(
        id = quiz.id,
        title = quiz.title,
        mobileUrl = quiz.mobileUrl,
        htmlUrl = quiz.htmlUrl,
        description = quiz.description,
        quizType = quiz.quizType,
        assignmentGroupId = quiz.assignmentGroupId,
        allowedAttempts = quiz.allowedAttempts,
        questionCount = quiz.questionCount,
        pointsPossible = quiz.pointsPossible,
        isLockQuestionsAfterAnswering = quiz.isLockQuestionsAfterAnswering,
        dueAt = quiz.dueAt,
        timeLimit = quiz.timeLimit,
        shuffleAnswers = quiz.shuffleAnswers,
        showCorrectAnswers = quiz.showCorrectAnswers,
        scoringPolicy = quiz.scoringPolicy,
        accessCode = quiz.accessCode,
        ipFilter = quiz.ipFilter,
        lockedForUser = quiz.lockedForUser,
        lockExplanation = quiz.lockExplanation,
        hideResults = quiz.hideResults,
        showCorrectAnswersAt = quiz.showCorrectAnswersAt,
        hideCorrectAnswersAt = quiz.hideCorrectAnswersAt,
        unlockAt = quiz.unlockAt,
        oneTimeResults = quiz.oneTimeResults,
        lockAt = quiz.lockAt,
        questionTypes = quiz.questionTypes,
        hasAccessCode = quiz.hasAccessCode,
        oneQuestionAtATime = quiz.oneQuestionAtATime,
        requireLockdownBrowser = quiz.requireLockdownBrowser,
        requireLockdownBrowserForResults = quiz.requireLockdownBrowserForResults,
        allowAnonymousSubmissions = quiz.allowAnonymousSubmissions,
        published = quiz.published,
        assignmentId = quiz.assignmentId,
        isOnlyVisibleToOverrides = quiz.isOnlyVisibleToOverrides,
        unpublishable = quiz.unpublishable,
        courseId = courseId,
    )

    fun toApiModel() = Quiz(
        id = id,
        title = title,
        mobileUrl = mobileUrl,
        htmlUrl = htmlUrl,
        description = description,
        quizType = quizType,
        assignmentGroupId = assignmentGroupId,
        //TODO
        lockInfo = null,
        //TODO
        permissions = null,
        allowedAttempts = allowedAttempts,
        questionCount = questionCount,
        pointsPossible = pointsPossible,
        isLockQuestionsAfterAnswering = isLockQuestionsAfterAnswering,
        dueAt = dueAt,
        timeLimit = timeLimit,
        shuffleAnswers = shuffleAnswers,
        showCorrectAnswers = showCorrectAnswers,
        scoringPolicy = scoringPolicy,
        accessCode = accessCode,
        ipFilter = ipFilter,
        lockedForUser = lockedForUser,
        lockExplanation = lockExplanation,
        hideResults = hideResults,
        showCorrectAnswersAt = showCorrectAnswersAt,
        hideCorrectAnswersAt = hideCorrectAnswersAt,
        unlockAt = unlockAt,
        oneTimeResults = oneTimeResults,
        lockAt = lockAt,
        questionTypes = questionTypes,
        hasAccessCode = hasAccessCode,
        oneQuestionAtATime = oneQuestionAtATime,
        requireLockdownBrowser = requireLockdownBrowser,
        requireLockdownBrowserForResults = requireLockdownBrowserForResults,
        allowAnonymousSubmissions = allowAnonymousSubmissions,
        published = published,
        assignmentId = assignmentId,
        isOnlyVisibleToOverrides = isOnlyVisibleToOverrides,
        unpublishable = unpublishable
    )
}
