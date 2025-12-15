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

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.GradingRule
import com.instructure.canvasapi2.models.Submission
import com.instructure.pandautils.utils.orDefault
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.math.min

class GradeCalculator @Inject constructor() {

    /**
     * Data class to hold submission information for grade calculations and drop rules.
     */
    private data class SubmissionData(
        val assignment: Assignment,
        val submission: Submission?,
        val score: Double,
        val total: Double,
        val submitted: Boolean,
        val pendingReview: Boolean,
        var drop: Boolean = false
    )

    /**
     * Calculates the grade using what-if scores if provided.
     *
     * @param groups List of assignment groups with domain models
     * @param whatIfScores Map of assignment ID to what-if score
     * @param applyGroupWeights Whether to apply assignment group weights
     * @param onlyGraded Whether to calculate based only on graded assignments (true = current grade, false = final grade)
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
     * Calculates a course's total grade based on all assignments (final grade).
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
            val gradeableAssignments = getGradeableAssignments(group.assignments)
            val submissionData = buildSubmissionData(
                gradeableAssignments,
                whatIfScores,
                includeUngraded = true
            )

            val submissionsToKeep = dropAssignments(submissionData, group.rules)

            val earnedPoints = submissionsToKeep.sumOf { it.score }
            val totalPoints = submissionsToKeep.sumOf { it.total }
            val weight = group.groupWeight

            if (totalPoints != 0.0 && earnedPoints != 0.0) {
                earnedScore += earnedPoints / totalPoints * weight
            }
        }

        return round(earnedScore)
    }

    /**
     * Calculates a course's grade based only on graded assignments (current grade).
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
            val gradeableAssignments = getGradeableAssignments(group.assignments)
            val submissionData = buildSubmissionData(
                gradeableAssignments,
                whatIfScores,
                includeUngraded = false
            )

            val submissionsToKeep = dropAssignments(submissionData, group.rules)

            val earnedPoints = submissionsToKeep.sumOf { it.score }
            val totalPoints = submissionsToKeep.sumOf { it.total }
            val weight = group.groupWeight
            val assignCount = submissionsToKeep.size

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
     * Calculates a course's total grade based on all assignments (final grade).
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
            val gradeableAssignments = getGradeableAssignments(group.assignments)
            val submissionData = buildSubmissionData(
                gradeableAssignments,
                whatIfScores,
                includeUngraded = true
            )

            val submissionsToKeep = dropAssignments(submissionData, group.rules)

            earnedPoints += submissionsToKeep.sumOf { it.score }
            totalPoints += submissionsToKeep.sumOf { it.total }
        }

        if (totalPoints != 0.0 && earnedPoints != 0.0) {
            earnedScore = earnedPoints / totalPoints * 100
        }

        return round(earnedScore)
    }

    /**
     * Calculates a course's grade based only on graded assignments (current grade).
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
        var earnedPoints = 0.0
        var totalPoints = 0.0

        for (group in groups) {
            val gradeableAssignments = getGradeableAssignments(group.assignments)
            val submissionData = buildSubmissionData(
                gradeableAssignments,
                whatIfScores,
                includeUngraded = false
            )

            val submissionsToKeep = dropAssignments(submissionData, group.rules)

            earnedPoints += submissionsToKeep.sumOf { it.score }
            totalPoints += submissionsToKeep.sumOf { it.total }
        }

        if (totalPoints != 0.0) {
            earnedScore = earnedPoints / totalPoints * 100
        }

        return round(earnedScore)
    }

    /**
     * Filters assignments to only include gradeable ones.
     * Excludes:
     * - Assignments with omitFromFinalGrade = true
     * - Unpublished assignments
     * - Assignments with only "not_graded" submission type
     * - Anonymous assignments (for safety, though should be handled by posted status)
     */
    private fun getGradeableAssignments(assignments: List<Assignment>): List<Assignment> {
        return assignments.filter { assignment ->
            !assignment.omitFromFinalGrade &&
                    assignment.published &&
                    !assignment.submissionTypesRaw.contains("not_graded") &&
                    assignment.submissionTypesRaw.isNotEmpty()
        }
    }

    /**
     * Builds submission data for calculations.
     * Handles:
     * - Excused submissions (filtered out)
     * - Unposted submissions (filtered out unless what-if score)
     * - What-if scores (always included)
     * - Pending review (excluded for current grade, included for final grade)
     */
    private fun buildSubmissionData(
        assignments: List<Assignment>,
        whatIfScores: Map<Long, Double>,
        includeUngraded: Boolean
    ): List<SubmissionData> {
        return assignments.mapNotNull { assignment ->
            val submission = assignment.submission
            val whatIfScore = whatIfScores[assignment.id]

            // Filter excused submissions (always, even with what-if scores)
            if (submission?.excused == true) {
                return@mapNotNull null
            }

            // Filter unposted submissions for current grade (unless there's a what-if score)
            // For final grade (includeUngraded=true), include all assignments even if unposted
            if (!includeUngraded && submission?.postedAt == null && whatIfScore == null) {
                return@mapNotNull null
            }

            val hasGrade = submission?.grade != null
            val isPendingReview = submission?.workflowState == "pending_review"
            val score = whatIfScore ?: submission?.score.orDefault()

            // Determine if this submission should be included
            val submitted = if (whatIfScore != null) {
                // What-if scores always count as submitted
                true
            } else {
                // For actual submissions, check if graded
                hasGrade && if (includeUngraded) {
                    // For final grade: include all graded (even pending review)
                    true
                } else {
                    // For current grade: exclude pending review
                    !isPendingReview
                }
            }

            // For includeUngraded mode, include all assignments
            // For graded-only mode, include only submitted ones
            if (!includeUngraded && !submitted) {
                return@mapNotNull null
            }

            SubmissionData(
                assignment = assignment,
                submission = submission,
                score = score,
                total = assignment.pointsPossible,
                submitted = submitted,
                pendingReview = isPendingReview
            )
        }
    }

    /**
     * Applies drop rules to submissions using a simplified algorithm.
     * This is a simpler version than the Kane & Kane binary search algorithm used in Canvas web,
     * but should produce very similar results for most cases.
     *
     * Algorithm:
     * 1. Separate never-drop submissions
     * 2. Sort droppable submissions by percentage (score/total)
     * 3. Drop lowest: remove the lowest-scoring submissions
     * 4. Drop highest: remove the highest-scoring submissions from what remains
     */
    private fun dropAssignments(
        allSubmissionData: List<SubmissionData>,
        rules: GradingRule?
    ): List<SubmissionData> {
        val dropLowest = rules?.dropLowest ?: 0
        val dropHighest = rules?.dropHighest ?: 0
        val neverDropIds = rules?.neverDrop ?: emptyList()

        // No dropping needed
        if (dropLowest == 0 && dropHighest == 0) {
            return allSubmissionData
        }

        // Partition into droppable and never-drop
        val (cannotDrop, droppable) = allSubmissionData.partition { submission ->
            neverDropIds.contains(submission.assignment.id)
        }

        if (droppable.isEmpty()) {
            return cannotDrop
        }

        // Adjust drop rules if not enough assignments
        val adjustedDropLowest = min(dropLowest, droppable.size - 1)
        val adjustedDropHighest = if (adjustedDropLowest + dropHighest >= droppable.size) {
            0
        } else {
            dropHighest
        }

        val keepHighest = droppable.size - adjustedDropLowest
        val keepLowest = keepHighest - adjustedDropHighest

        // Separate pointed and unpointed assignments
        val hasPointed = droppable.any { it.total > 0 }

        val keptSubmissions = if (hasPointed) {
            dropPointed(droppable, keepHighest, keepLowest)
        } else {
            dropUnpointed(droppable, keepHighest, keepLowest)
        }

        // Mark dropped submissions
        for (submission in droppable) {
            if (!keptSubmissions.contains(submission)) {
                submission.drop = true
            }
        }

        return keptSubmissions + cannotDrop
    }

    /**
     * Drop algorithm for assignments with 0 points possible.
     * Just sort by raw score and keep the middle ones.
     */
    private fun dropUnpointed(
        submissions: List<SubmissionData>,
        keepHighest: Int,
        keepLowest: Int
    ): List<SubmissionData> {
        return submissions
            .sortedBy { it.score }
            .takeLast(keepHighest)
            .take(keepLowest)
    }

    /**
     * Simplified drop algorithm for assignments with points possible.
     * Sorts by percentage and keeps the best/worst based on requirements.
     *
     * This is simpler than the Kane & Kane algorithm but works well for most cases:
     * 1. Sort by percentage (score/total)
     * 2. Drop lowest: remove from bottom of sorted list
     * 3. Drop highest: remove from top of what remains
     */
    private fun dropPointed(
        submissions: List<SubmissionData>,
        keepHighest: Int,
        keepLowest: Int
    ): List<SubmissionData> {
        // Sort by percentage, with stable sorting using assignment ID as tiebreaker
        val sorted = submissions.sortedWith(
            compareBy(
            { it.score / it.total.coerceAtLeast(0.001) },  // Avoid division by zero
            { it.assignment.id }
        ))

        // Drop lowest: keep the highest scoring ones
        val afterDroppingLowest = sorted.takeLast(keepHighest)

        // Drop highest: from what remains, keep the lowest scoring ones
        return afterDroppingLowest.take(keepLowest)
    }

    private fun round(value: Double, places: Int = 2): Double {
        if (places < 0) throw IllegalArgumentException()

        var bd = BigDecimal(value)
        bd = bd.setScale(places, RoundingMode.HALF_UP)
        return bd.toDouble()
    }
}
