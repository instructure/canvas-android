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

package com.instructure.pandautils.features.grades

import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.pandautils.utils.orDefault
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class GradeCalculator @Inject constructor() {

    /**
     * Calculates the grade using what-if scores if provided.
     *
     * @param groups List of assignment groups with domain models
     * @param whatIfScores Map of assignment ID to what-if score
     * @param applyGroupWeights Whether to apply assignment group weights
     * @param onlyGraded Whether to calculate based only on graded assignments
     * @return The calculated grade as a percentage
     */
    fun calculateGrade(
        groups: List<AssignmentGroup>,
        whatIfScores: Map<Long, Double>,
        applyGroupWeights: Boolean,
        onlyGraded: Boolean
    ): Double {
        return when {
            applyGroupWeights && onlyGraded -> calcGradesGraded(groups, whatIfScores)
            applyGroupWeights && !onlyGraded -> calcGradesTotal(groups, whatIfScores)
            !applyGroupWeights && onlyGraded -> calcGradesGradedNoWeight(groups, whatIfScores)
            else -> calcGradesTotalNoWeight(groups, whatIfScores)
        }
    }

    /**
     * Calculates a course's total grade based on all assignments.
     * Maps to the "Calculate based only on graded assignments" checkbox in UNCHECKED state.
     * Uses assignment group weights.
     *
     * @param groups List of assignment groups for the course
     * @param whatIfScores Map of assignment ID to what-if score
     * @return The grade as a rounded double (e.g., 85.6)
     */
    private fun calcGradesTotal(
        groups: List<AssignmentGroup>,
        whatIfScores: Map<Long, Double>
    ): Double {
        var earnedScore = 0.0

        for (group in groups) {
            var earnedPoints = 0.0
            var totalPoints = 0.0
            val weight = group.groupWeight

            for (assignment in group.assignments) {
                if (assignment.omitFromFinalGrade.orDefault()) continue

                val scoreToUse = whatIfScores[assignment.id] ?: assignment.submission?.score

                if (scoreToUse != null && assignment.submissionTypesRaw.isNotEmpty()) {
                    earnedPoints += scoreToUse
                }

                totalPoints += assignment.pointsPossible
            }

            if (totalPoints != 0.0 && earnedPoints != 0.0) {
                earnedScore += earnedPoints / totalPoints * weight
            }
        }

        return round(earnedScore)
    }

    /**
     * Calculates a course's grade based only on graded assignments.
     * Maps to the "Calculate based only on graded assignments" checkbox in CHECKED state.
     * Uses assignment group weights.
     *
     * @param groups List of assignment groups for the course
     * @param whatIfScores Map of assignment ID to what-if score
     * @return The grade as a rounded double (e.g., 85.6)
     */
    private fun calcGradesGraded(
        groups: List<AssignmentGroup>,
        whatIfScores: Map<Long, Double>
    ): Double {
        var totalWeight = 0.0
        var earnedScore = 0.0

        for (group in groups) {
            var totalPoints = 0.0
            var earnedPoints = 0.0
            val weight = group.groupWeight
            var assignCount = 0

            for (assignment in group.assignments) {
                if (assignment.omitFromFinalGrade.orDefault()) continue

                val scoreToUse = whatIfScores[assignment.id] ?: assignment.submission?.score
                val isPendingReview = assignment.submission?.workflowState == "pending_review"

                if (scoreToUse != null &&
                    assignment.submissionTypesRaw.isNotEmpty() &&
                    !isPendingReview) {
                    assignCount++
                    totalPoints += assignment.pointsPossible
                    earnedPoints += scoreToUse
                }
            }

            if (totalPoints != 0.0) {
                earnedScore += earnedPoints / totalPoints * weight
            }

            // Track total weight from groups that have graded assignments
            if (assignCount != 0) {
                totalWeight += weight
            }
        }

        // Normalize if total weight is less than 100
        if (totalWeight < 100 && earnedScore != 0.0) {
            earnedScore = earnedScore / totalWeight * 100
        }

        return round(earnedScore)
    }

    /**
     * Calculates a course's total grade based on all assignments.
     * Maps to checkbox UNCHECKED state when course has no assignment group weights.
     *
     * @param groups List of assignment groups for the course
     * @param whatIfScores Map of assignment ID to what-if score
     * @return The grade as a rounded double (e.g., 85.6)
     */
    private fun calcGradesTotalNoWeight(
        groups: List<AssignmentGroup>,
        whatIfScores: Map<Long, Double>
    ): Double {
        var earnedScore = 0.0
        var earnedPoints = 0.0
        var totalPoints = 0.0

        for (group in groups) {
            for (assignment in group.assignments) {
                if (assignment.omitFromFinalGrade.orDefault()) continue

                val scoreToUse = whatIfScores[assignment.id] ?: assignment.submission?.score
                val isPendingReview = assignment.submission?.workflowState == "pending_review"

                if (scoreToUse != null &&
                    assignment.submissionTypesRaw.isNotEmpty() &&
                    !isPendingReview) {
                    earnedPoints += scoreToUse
                }

                totalPoints += assignment.pointsPossible
            }
        }

        if (totalPoints != 0.0 && earnedPoints != 0.0) {
            earnedScore = earnedPoints / totalPoints * 100
        }

        return round(earnedScore)
    }

    /**
     * Calculates a course's grade based only on graded assignments.
     * Maps to checkbox CHECKED state when course has no assignment group weights.
     *
     * @param groups List of assignment groups for the course
     * @param whatIfScores Map of assignment ID to what-if score
     * @return The grade as a rounded double (e.g., 85.6)
     */
    private fun calcGradesGradedNoWeight(
        groups: List<AssignmentGroup>,
        whatIfScores: Map<Long, Double>
    ): Double {
        var earnedScore = 0.0
        var totalPoints = 0.0
        var earnedPoints = 0.0

        for (group in groups) {
            for (assignment in group.assignments) {
                if (assignment.omitFromFinalGrade.orDefault()) continue

                val scoreToUse = whatIfScores[assignment.id] ?: assignment.submission?.score

                if (scoreToUse != null && assignment.submissionTypesRaw.isNotEmpty()) {
                    totalPoints += assignment.pointsPossible
                    earnedPoints += scoreToUse
                }
            }
        }

        if (totalPoints != 0.0) {
            earnedScore = earnedPoints / totalPoints * 100
        }

        return round(earnedScore)
    }

    private fun round(value: Double, places: Int = 2): Double {
        if (places < 0) throw IllegalArgumentException()

        var bd = BigDecimal(value)
        bd = bd.setScale(places, RoundingMode.HALF_UP)
        return bd.toDouble()
    }
}