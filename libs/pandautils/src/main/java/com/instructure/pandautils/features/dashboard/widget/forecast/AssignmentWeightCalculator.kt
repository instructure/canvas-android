/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.forecast

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingRule
import javax.inject.Inject

class AssignmentWeightCalculator @Inject constructor() {

    /**
     * Calculates the weight percentage for an assignment based on:
     * - Whether the course uses weighted assignment groups
     * - The assignment group's weight
     * - The assignment's points possible relative to other assignments in the group
     * - Drop rules applied only to graded assignments
     *
     * Returns null if weight cannot be calculated or is not applicable
     */
    fun calculateWeight(
        assignment: Assignment,
        course: Course,
        assignmentGroups: List<AssignmentGroup>
    ): Double? {
        // If course doesn't use weighted assignment groups, return null
        if (!course.isApplyAssignmentGroupWeights) return null

        // Find the assignment group for this assignment
        val assignmentGroup = assignmentGroups.find { it.id == assignment.assignmentGroupId } ?: return null

        // If assignment is omitted from final grade, return null
        if (assignment.omitFromFinalGrade) return null

        // Get the group weight (percentage of total grade)
        val groupWeight = assignmentGroup.groupWeight
        if (groupWeight == 0.0) return null

        // Get all assignments in the group that count towards the grade
        val groupAssignments = assignmentGroup.assignments
            .filter { !it.omitFromFinalGrade }
            .filter { (it.pointsPossible ?: 0.0) > 0 }

        if (groupAssignments.isEmpty()) return null

        // Separate graded and ungraded assignments
        val gradedAssignments = groupAssignments.filter { it.submission?.isGraded == true }
        val ungradedAssignments = groupAssignments.filter { it.submission?.isGraded != true }

        // Apply drop rules only to graded assignments
        val countingGradedAssignments = if (gradedAssignments.isNotEmpty()) {
            applyDropRules(gradedAssignments, assignmentGroup.rules)
        } else {
            emptyList()
        }

        // Combine: graded assignments that weren't dropped + all ungraded assignments
        val countingAssignments = countingGradedAssignments + ungradedAssignments

        // Calculate total points possible for counting assignments
        val totalPointsPossible = countingAssignments.sumOf { it.pointsPossible ?: 0.0 }
        if (totalPointsPossible == 0.0) return null

        // Calculate the assignment's weight within its group
        val pointsWeight = (assignment.pointsPossible ?: 0.0) / totalPointsPossible

        // Calculate final weight as: (group weight) * (assignment's weight within group)
        return groupWeight * pointsWeight
    }

    /**
     * Applies drop rules (drop_lowest, drop_highest, never_drop) to graded assignments.
     * Uses actual scores to determine which to drop.
     */
    private fun applyDropRules(
        assignments: List<Assignment>,
        rules: GradingRule?
    ): List<Assignment> {
        if (rules == null || !rules.hasValidRule()) {
            return assignments
        }

        // Never drop these assignments
        val neverDropIds = rules.neverDrop.map { it.toLong() }.toSet()

        // Get assignments eligible for dropping (not in never-drop list)
        val eligibleForDrop = assignments.filter { it.id !in neverDropIds }

        // Sort by score percentage to apply drop rules
        val sortedByPercentage = eligibleForDrop.sortedBy { assignment ->
            val score = assignment.submission?.score ?: 0.0
            val pointsPossible = assignment.pointsPossible ?: 1.0
            score / pointsPossible
        }

        // Determine which assignments to drop
        val toDrop = mutableSetOf<Long>()

        // Drop lowest
        if (rules.dropLowest > 0 && sortedByPercentage.isNotEmpty()) {
            sortedByPercentage.take(rules.dropLowest.coerceAtMost(sortedByPercentage.size))
                .forEach { toDrop.add(it.id) }
        }

        // Drop highest
        if (rules.dropHighest > 0 && sortedByPercentage.isNotEmpty()) {
            sortedByPercentage.reversed().take(rules.dropHighest.coerceAtMost(sortedByPercentage.size))
                .forEach { toDrop.add(it.id) }
        }

        // Return assignments that weren't dropped
        return assignments.filter { it.id !in toDrop }
    }
}