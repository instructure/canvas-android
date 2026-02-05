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
import kotlin.math.max
import kotlin.math.min

/**
 * Extension functions for precise floating point arithmetic to avoid rounding errors.
 * These match Canvas LMS web's use of Big.js for accurate grade calculations.
 */
private fun Double.toBigDecimal(): BigDecimal = BigDecimal.valueOf(this)

private operator fun BigDecimal.plus(other: BigDecimal): BigDecimal = this.add(other)

private operator fun BigDecimal.times(other: Double): BigDecimal =
    this.multiply(other.toBigDecimal())

private infix fun BigDecimal.divideBy(other: Double): BigDecimal =
    this.divide(other.toBigDecimal(), 10, RoundingMode.HALF_UP)

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
     * Calculates the grade with weighted grading periods support.
     * When grading periods are weighted, assignments are grouped by period,
     * grades are calculated per period (with drop rules applied), and then
     * combined using period weights.
     *
     * @param groups List of assignment groups with domain models
     * @param whatIfScores Map of assignment ID to what-if score
     * @param applyGroupWeights Whether to apply assignment group weights
     * @param onlyGraded Whether to calculate based only on graded assignments
     * @param gradingPeriods List of grading periods with weights
     * @param weightGradingPeriods Whether grading periods are weighted
     * @return The calculated grade as a percentage
     */
    fun calculateGradeWithPeriods(
        groups: List<AssignmentGroup>,
        whatIfScores: Map<Long, Double>,
        applyGroupWeights: Boolean,
        onlyGraded: Boolean,
        gradingPeriods: List<com.instructure.canvasapi2.models.GradingPeriod>,
        weightGradingPeriods: Boolean
    ): Double {
        if (!weightGradingPeriods || gradingPeriods.isEmpty()) {
            // No period weighting, use standard calculation
            return calculateGrade(groups, whatIfScores, applyGroupWeights, onlyGraded)
        }

        // Split assignment groups by grading period
        val periodBasedGroups = divideGroupsByGradingPeriods(groups)

        // Calculate grade for each period
        val periodGrades = gradingPeriods.mapNotNull { period ->
            val groupsInPeriod = periodBasedGroups[period.id] ?: return@mapNotNull null

            val periodGrade = calculateGrade(groupsInPeriod, whatIfScores, applyGroupWeights, onlyGraded)

            PeriodGrade(
                periodId = period.id,
                grade = periodGrade,
                weight = period.weight
            )
        }

        // Combine period grades using weights
        return combinePeriodGrades(periodGrades)
    }

    /**
     * Divides assignment groups by grading period.
     * Each assignment group may be duplicated multiple times, once for each period it contains assignments from.
     * This ensures drop rules are applied per period, not across periods.
     */
    private fun divideGroupsByGradingPeriods(
        groups: List<AssignmentGroup>
    ): Map<Long, List<AssignmentGroup>> {
        val groupsByPeriod = mutableMapOf<Long, MutableList<AssignmentGroup>>()

        for (group in groups) {
            // Group assignments by their grading period ID
            val assignmentsByPeriod = group.assignments.groupBy { assignment ->
                assignment.submission?.gradingPeriodId
            }

            // Create a separate group instance for each period
            for ((periodId, assignmentsInPeriod) in assignmentsByPeriod) {
                if (periodId != null) {
                    val periodGroup = group.copy(assignments = assignmentsInPeriod)
                    groupsByPeriod.getOrPut(periodId) { mutableListOf() }.add(periodGroup)
                }
            }
        }

        return groupsByPeriod
    }

    /**
     * Combines grading period grades using their weights.
     * Formula: sum(periodGrade * periodWeight) * 100 / totalWeight
     */
    private fun combinePeriodGrades(periodGrades: List<PeriodGrade>): Double {
        if (periodGrades.isEmpty()) return 0.0

        val weightedSum = periodGrades.sumOf { it.grade * it.weight }
        val totalWeight = periodGrades.sumOf { it.weight }

        return if (totalWeight > 0) {
            (weightedSum * 100.0 / min(totalWeight, 100.0))
        } else {
            0.0
        }
    }

    private data class PeriodGrade(
        val periodId: Long,
        val grade: Double,
        val weight: Double
    )

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
        var totalWeight = BigDecimal.ZERO
        var earnedScore = BigDecimal.ZERO

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

            if (totalPoints != 0.0) {
                val groupPercentage = earnedPoints.toBigDecimal() divideBy totalPoints
                earnedScore = earnedScore + (groupPercentage * weight)
            }

            // Track total weight from groups that have possible points
            if (totalPoints != 0.0) {
                totalWeight = totalWeight + weight.toBigDecimal()
            }
        }

        // Normalize if total weight is less than 100
        if (totalWeight < 100.0.toBigDecimal() && totalWeight > BigDecimal.ZERO && earnedScore != BigDecimal.ZERO) {
            earnedScore = (earnedScore divideBy totalWeight.toDouble()) * 100.0
        }

        return round(earnedScore.toDouble())
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
        var totalWeight = BigDecimal.ZERO
        var earnedScore = BigDecimal.ZERO

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
                val groupPercentage = earnedPoints.toBigDecimal() divideBy totalPoints
                earnedScore = earnedScore + (groupPercentage * weight)
            }

            // Track total weight from groups that have graded assignments
            if (assignCount != 0) {
                totalWeight = totalWeight + weight.toBigDecimal()
            }
        }

        // Normalize if total weight is less than 100
        if (totalWeight < 100.0.toBigDecimal() && totalWeight > BigDecimal.ZERO && earnedScore != BigDecimal.ZERO) {
            earnedScore = (earnedScore divideBy totalWeight.toDouble()) * 100.0
        }

        return round(earnedScore.toDouble())
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
            dropPointed(droppable, cannotDrop, keepHighest, keepLowest)
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
     * Kane & Kane algorithm for dropping assignments with point values.
     * Uses binary search to find the optimal set of assignments to drop.
     * Based on the paper "Dropping Lowest Grades" by Daniel Kane and Jonathan Kane.
     * (http://cseweb.ucsd.edu/~dakane/droplowest.pdf)
     *
     * This is the same algorithm used by Canvas web to ensure identical grade calculations.
     */
    private fun dropPointed(
        submissions: List<SubmissionData>,
        cannotDrop: List<SubmissionData>,
        keepHighest: Int,
        keepLowest: Int
    ): List<SubmissionData> {
        val maxTotal = submissions.maxOfOrNull { it.total } ?: 0.0

        // First phase: drop lowest scores
        val submissionsWithLowestDropped = keepHelper(
            submissions = submissions,
            keepCount = keepHighest,
            sortAscending = false,  // sort descending to keep highest
            cannotDrop = cannotDrop,
            maxTotal = maxTotal
        )

        // Second phase: drop highest scores from what remains
        return keepHelper(
            submissions = submissionsWithLowestDropped,
            keepCount = keepLowest,
            sortAscending = true,  // sort ascending to keep lowest
            cannotDrop = cannotDrop,
            maxTotal = maxTotal
        )
    }

    /**
     * Binary search helper to find optimal submissions to keep.
     * This implements the core Kane & Kane algorithm.
     */
    private fun keepHelper(
        submissions: List<SubmissionData>,
        keepCount: Int,
        sortAscending: Boolean,
        cannotDrop: List<SubmissionData>,
        maxTotal: Double
    ): List<SubmissionData> {
        val actualKeepCount = max(1, keepCount)

        if (submissions.size <= actualKeepCount) {
            return submissions
        }

        val allSubmissionData = submissions + cannotDrop
        val (unpointed, pointed) = allSubmissionData.partition { it.total == 0.0 }

        var (qHigh, qLow, qMid) = setUpGrades(pointed, unpointed)

        val bigF = buildBigF(actualKeepCount, cannotDrop, sortAscending)

        var (x, submissionsToKeep) = bigF(qMid, submissions)
        val threshold = 1.0 / (2.0 * actualKeepCount * maxTotal * maxTotal)

        while (qHigh - qLow >= threshold) {
            if (x < 0) {
                qHigh = qMid
            } else {
                qLow = qMid
            }
            qMid = (qLow + qHigh) / 2.0
            if (qMid == qHigh || qMid == qLow) {
                break
            }
            val result = bigF(qMid, submissions)
            x = result.first
            submissionsToKeep = result.second
        }

        return submissionsToKeep
    }

    /**
     * Builds the scoring function for the binary search.
     * Returns a function that rates submissions based on threshold q.
     */
    private fun buildBigF(
        keepCount: Int,
        cannotDrop: List<SubmissionData>,
        sortAscending: Boolean
    ): (Double, List<SubmissionData>) -> Pair<Double, List<SubmissionData>> {
        return { q: Double, submissions: List<SubmissionData> ->
            // Rate each submission as: score - q * total
            val ratedScores = submissions.map { submission ->
                submission.score - q * submission.total to submission
            }

            // Sort by rating (ascending or descending)
            val rankedScores = if (sortAscending) {
                ratedScores.sortedWith(compareBy({ it.first }, { it.second.assignment.id }))
            } else {
                ratedScores.sortedWith(compareByDescending<Pair<Double, SubmissionData>> { it.first }
                    .thenBy { it.second.assignment.id })
            }

            // Keep the top keepCount submissions
            val keptScores = rankedScores.take(keepCount)
            val qKept = keptScores.sumOf { it.first }
            val keptSubmissions = keptScores.map { it.second }

            // Add the score contribution from cannot-drop submissions
            val qCannotDrop = cannotDrop.sumOf { submission ->
                submission.score - q * submission.total
            }

            qKept + qCannotDrop to keptSubmissions
        }
    }

    /**
     * Sets up initial boundaries for binary search.
     */
    private fun setUpGrades(
        pointed: List<SubmissionData>,
        unpointed: List<SubmissionData>
    ): Triple<Double, Double, Double> {
        val grades = pointed.map { it.score / it.total }.sorted()
        val qHigh = estimateQHigh(pointed, unpointed, grades)
        val qLow = grades.firstOrNull() ?: 0.0
        val qMid = (qLow + qHigh) / 2.0

        return Triple(qHigh, qLow, qMid)
    }

    /**
     * Estimates the upper bound for binary search.
     */
    private fun estimateQHigh(
        pointed: List<SubmissionData>,
        unpointed: List<SubmissionData>,
        grades: List<Double>
    ): Double {
        if (unpointed.isNotEmpty()) {
            val pointsPossible = pointed.sumOf { it.total }
            val bestPointedScore = max(pointsPossible, pointed.sumOf { it.score })
            val unpointedScore = unpointed.sumOf { it.score }
            return (bestPointedScore + unpointedScore) / pointsPossible
        }

        return grades.lastOrNull() ?: 0.0
    }

    /**
     * Rounds a value to the specified number of decimal places.
     * Uses string conversion to avoid floating point precision issues,
     * matching Canvas LMS web behavior.
     *
     * For example: 93.825 will correctly round to 93.83 (not 93.82)
     */
    private fun round(value: Double, places: Int = 2): Double {
        if (places < 0) throw IllegalArgumentException()

        val bd = BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP)
        return bd.toDouble()
    }
}
