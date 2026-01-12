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
import javax.inject.Inject

class AssignmentWeightCalculator @Inject constructor() {

    /**
     * Calculates the weight percentage for an assignment based on:
     * - Whether the course uses weighted assignment groups
     * - The assignment group's weight
     * - The assignment's points possible relative to other assignments in the group
     * - Drop rules and omit_from_final_grade flags
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

        // Apply drop rules to determine which assignments actually count
        val countingAssignments = applyDropRules(groupAssignments, assignmentGroup.rules)

        // If this assignment is dropped, return null
        if (!countingAssignments.any { it.id == assignment.id }) return null

        // Calculate total points possible for counting assignments
        val totalPointsPossible = countingAssignments.sumOf { it.pointsPossible ?: 0.0 }
        if (totalPointsPossible == 0.0) return null

        // Calculate the assignment's weight within its group
        val pointsWeight = (assignment.pointsPossible ?: 0.0) / totalPointsPossible

        // Calculate final weight as: (group weight) * (assignment's weight within group)
        return groupWeight * pointsWeight
    }

    /**
     * Applies drop rules (drop_lowest, drop_highest, never_drop) to determine
     * which assignments actually count towards the grade.
     */
    private fun applyDropRules(
        assignments: List<Assignment>,
        rules: com.instructure.canvasapi2.models.GradingRule?
    ): List<Assignment> {
        if (rules == null || !rules.hasValidRule()) {
            return assignments
        }

        // Start with all assignments
        val mutableAssignments = assignments.toMutableList()

        // Never drop these assignments (keep them in the list no matter what)
        val neverDropIds = rules.neverDrop.map { it.toLong() }.toSet()

        // Sort by points earned to apply drop rules
        // For forecast (upcoming/missing), we can't know the score, so we drop by points possible
        val sortedByPoints = mutableAssignments
            .filter { it.id !in neverDropIds }
            .sortedBy { it.pointsPossible ?: 0.0 }

        // Drop lowest
        val toDrop = mutableSetOf<Long>()
        if (rules.dropLowest > 0 && sortedByPoints.isNotEmpty()) {
            sortedByPoints.take(rules.dropLowest.coerceAtMost(sortedByPoints.size))
                .forEach { toDrop.add(it.id) }
        }

        // Drop highest
        if (rules.dropHighest > 0 && sortedByPoints.isNotEmpty()) {
            sortedByPoints.reversed().take(rules.dropHighest.coerceAtMost(sortedByPoints.size))
                .forEach { toDrop.add(it.id) }
        }

        // Return assignments that weren't dropped
        return mutableAssignments.filter { it.id !in toDrop }
    }
}