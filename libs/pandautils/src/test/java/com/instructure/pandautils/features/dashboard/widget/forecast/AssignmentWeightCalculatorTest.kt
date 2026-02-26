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
import com.instructure.canvasapi2.models.Submission
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AssignmentWeightCalculatorTest {

    private lateinit var calculator: AssignmentWeightCalculator

    @Before
    fun setUp() {
        calculator = AssignmentWeightCalculator()
    }

    @Test
    fun `calculateWeight returns null when course does not use weighted assignment groups`() {
        val course = Course(id = 1, isApplyAssignmentGroupWeights = false)
        val assignment = Assignment(id = 1, assignmentGroupId = 1, pointsPossible = 100.0)
        val assignmentGroups = listOf(
            AssignmentGroup(id = 1, groupWeight = 50.0, assignments = arrayListOf(assignment))
        )

        val result = calculator.calculateWeight(assignment, course, assignmentGroups)

        assertNull(result)
    }

    @Test
    fun `calculateWeight returns null when assignment group not found`() {
        val course = Course(id = 1, isApplyAssignmentGroupWeights = true)
        val assignment = Assignment(id = 1, assignmentGroupId = 1, pointsPossible = 100.0)
        val assignmentGroups = listOf(
            AssignmentGroup(id = 2, groupWeight = 50.0, assignments = arrayListOf())
        )

        val result = calculator.calculateWeight(assignment, course, assignmentGroups)

        assertNull(result)
    }

    @Test
    fun `calculateWeight returns null when assignment is omitted from final grade`() {
        val course = Course(id = 1, isApplyAssignmentGroupWeights = true)
        val assignment = Assignment(id = 1, assignmentGroupId = 1, pointsPossible = 100.0, omitFromFinalGrade = true)
        val assignmentGroups = listOf(
            AssignmentGroup(id = 1, groupWeight = 50.0, assignments = arrayListOf(assignment))
        )

        val result = calculator.calculateWeight(assignment, course, assignmentGroups)

        assertNull(result)
    }

    @Test
    fun `calculateWeight returns null when group weight is zero`() {
        val course = Course(id = 1, isApplyAssignmentGroupWeights = true)
        val assignment = Assignment(id = 1, assignmentGroupId = 1, pointsPossible = 100.0)
        val assignmentGroups = listOf(
            AssignmentGroup(id = 1, groupWeight = 0.0, assignments = arrayListOf(assignment))
        )

        val result = calculator.calculateWeight(assignment, course, assignmentGroups)

        assertNull(result)
    }

    @Test
    fun `calculateWeight returns null when no valid assignments in group`() {
        val course = Course(id = 1, isApplyAssignmentGroupWeights = true)
        val assignment = Assignment(id = 1, assignmentGroupId = 1, pointsPossible = 100.0)
        val assignmentGroups = listOf(
            AssignmentGroup(id = 1, groupWeight = 50.0, assignments = arrayListOf())
        )

        val result = calculator.calculateWeight(assignment, course, assignmentGroups)

        assertNull(result)
    }

    @Test
    fun `calculateWeight calculates correct weight for single ungraded assignment`() {
        val course = Course(id = 1, isApplyAssignmentGroupWeights = true)
        val assignment = Assignment(id = 1, assignmentGroupId = 1, pointsPossible = 100.0)
        val assignmentGroups = listOf(
            AssignmentGroup(id = 1, groupWeight = 40.0, assignments = arrayListOf(assignment))
        )

        val result = calculator.calculateWeight(assignment, course, assignmentGroups)

        assertEquals(40.0, result!!, 0.001)
    }

    @Test
    fun `calculateWeight calculates correct weight for multiple ungraded assignments`() {
        val course = Course(id = 1, isApplyAssignmentGroupWeights = true)
        val assignment1 = Assignment(id = 1, assignmentGroupId = 1, pointsPossible = 100.0)
        val assignment2 = Assignment(id = 2, assignmentGroupId = 1, pointsPossible = 50.0)
        val assignment3 = Assignment(id = 3, assignmentGroupId = 1, pointsPossible = 50.0)
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                groupWeight = 60.0,
                assignments = arrayListOf(assignment1, assignment2, assignment3)
            )
        )

        val result = calculator.calculateWeight(assignment1, course, assignmentGroups)

        // assignment1 has 100 out of 200 total points = 0.5
        // 0.5 * 60.0 = 30.0
        assertEquals(30.0, result!!, 0.001)
    }

    @Test
    fun `calculateWeight applies drop rules only to graded assignments`() {
        val course = Course(id = 1, isApplyAssignmentGroupWeights = true)

        // Graded assignments
        val graded1 = Assignment(
            id = 1,
            assignmentGroupId = 1,
            pointsPossible = 100.0,
            submission = Submission(grade = "50", score = 50.0)
        )
        val graded2 = Assignment(
            id = 2,
            assignmentGroupId = 1,
            pointsPossible = 100.0,
            submission = Submission(grade = "90", score = 90.0)
        )

        // Ungraded assignments
        val ungraded = Assignment(id = 3, assignmentGroupId = 1, pointsPossible = 100.0)

        val rules = GradingRule(dropLowest = 1)
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                groupWeight = 50.0,
                rules = rules,
                assignments = arrayListOf(graded1, graded2, ungraded)
            )
        )

        val result = calculator.calculateWeight(ungraded, course, assignmentGroups)

        // graded1 (50%) should be dropped, leaving graded2 + ungraded
        // ungraded has 100 out of 200 total points = 0.5
        // 0.5 * 50.0 = 25.0
        assertEquals(25.0, result!!, 0.001)
    }

    @Test
    fun `calculateWeight applies drop lowest correctly`() {
        val course = Course(id = 1, isApplyAssignmentGroupWeights = true)

        val graded1 = Assignment(
            id = 1,
            assignmentGroupId = 1,
            pointsPossible = 100.0,
            submission = Submission(grade = "50", score = 50.0)
        )
        val graded2 = Assignment(
            id = 2,
            assignmentGroupId = 1,
            pointsPossible = 100.0,
            submission = Submission(grade = "90", score = 90.0)
        )
        val graded3 = Assignment(
            id = 3,
            assignmentGroupId = 1,
            pointsPossible = 100.0,
            submission = Submission(grade = "70", score = 70.0)
        )

        val rules = GradingRule(dropLowest = 2)
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                groupWeight = 30.0,
                rules = rules,
                assignments = arrayListOf(graded1, graded2, graded3)
            )
        )

        val result = calculator.calculateWeight(graded2, course, assignmentGroups)

        // Drop graded1 (50%) and graded3 (70%), keep only graded2
        // graded2 has 100 out of 100 total points = 1.0
        // 1.0 * 30.0 = 30.0
        assertEquals(30.0, result!!, 0.001)
    }

    @Test
    fun `calculateWeight applies drop highest correctly`() {
        val course = Course(id = 1, isApplyAssignmentGroupWeights = true)

        val graded1 = Assignment(
            id = 1,
            assignmentGroupId = 1,
            pointsPossible = 100.0,
            submission = Submission(grade = "50.0", score = 50.0)
        )
        val graded2 = Assignment(
            id = 2,
            assignmentGroupId = 1,
            pointsPossible = 100.0,
            submission = Submission(grade = "90.0", score = 90.0)
        )
        val graded3 = Assignment(
            id = 3,
            assignmentGroupId = 1,
            pointsPossible = 100.0,
            submission = Submission(grade = "70.0", score = 70.0)
        )

        val rules = GradingRule(dropHighest = 1)
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                groupWeight = 50.0,
                rules = rules,
                assignments = arrayListOf(graded1, graded2, graded3)
            )
        )

        val result = calculator.calculateWeight(graded1, course, assignmentGroups)

        // Drop graded2 (90%), keep graded1 and graded3
        // graded1 has 100 out of 200 total points = 0.5
        // 0.5 * 50.0 = 25.0
        assertEquals(25.0, result!!, 0.001)
    }

    @Test
    fun `calculateWeight respects never drop rule`() {
        val course = Course(id = 1, isApplyAssignmentGroupWeights = true)

        val graded1 = Assignment(
            id = 1,
            assignmentGroupId = 1,
            pointsPossible = 100.0,
            submission = Submission(grade = "30.0", score = 30.0)
        )
        val graded2 = Assignment(
            id = 2,
            assignmentGroupId = 1,
            pointsPossible = 100.0,
            submission = Submission(grade = "90.0", score = 90.0)
        )
        val graded3 = Assignment(
            id = 3,
            assignmentGroupId = 1,
            pointsPossible = 100.0,
            submission = Submission(grade = "60.0", score = 60.0)
        )

        val rules = GradingRule(dropLowest = 1, neverDrop = listOf(3))
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                groupWeight = 40.0,
                rules = rules,
                assignments = arrayListOf(graded1, graded2, graded3)
            )
        )

        val result = calculator.calculateWeight(graded2, course, assignmentGroups)

        // graded3 (60%) is in never-drop, so graded1 (30%) should be dropped
        // Keep graded2 and graded3: 200 total points
        // graded2 has 100 out of 200 = 0.5
        // 0.5 * 40.0 = 20.0
        assertEquals(20.0, result!!, 0.001)
    }

    @Test
    fun `calculateWeight handles drop rules with both drop lowest and drop highest`() {
        val course = Course(id = 1, isApplyAssignmentGroupWeights = true)

        val graded1 = Assignment(
            id = 1,
            assignmentGroupId = 1,
            pointsPossible = 100.0,
            submission = Submission(grade = "40.0", score = 40.0)
        )
        val graded2 = Assignment(
            id = 2,
            assignmentGroupId = 1,
            pointsPossible = 100.0,
            submission = Submission(grade = "60.0", score = 60.0)
        )
        val graded3 = Assignment(
            id = 3,
            assignmentGroupId = 1,
            pointsPossible = 100.0,
            submission = Submission(grade = "80.0", score = 80.0)
        )
        val graded4 = Assignment(
            id = 4,
            assignmentGroupId = 1,
            pointsPossible = 100.0,
            submission = Submission(grade = "95.0", score = 95.0)
        )

        val rules = GradingRule(dropLowest = 1, dropHighest = 1)
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                groupWeight = 50.0,
                rules = rules,
                assignments = arrayListOf(graded1, graded2, graded3, graded4)
            )
        )

        val result = calculator.calculateWeight(graded2, course, assignmentGroups)

        // Drop graded1 (40%) and graded4 (95%), keep graded2 and graded3
        // graded2 has 100 out of 200 = 0.5
        // 0.5 * 50.0 = 25.0
        assertEquals(25.0, result!!, 0.001)
    }

    @Test
    fun `calculateWeight filters out assignments with zero or null points possible`() {
        val course = Course(id = 1, isApplyAssignmentGroupWeights = true)

        val assignment1 = Assignment(id = 1, assignmentGroupId = 1, pointsPossible = 100.0)
        val assignment2 = Assignment(id = 2, assignmentGroupId = 1, pointsPossible = 0.0)
        val assignment3 = Assignment(id = 3, assignmentGroupId = 1)

        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                groupWeight = 50.0,
                assignments = arrayListOf(assignment1, assignment2, assignment3)
            )
        )

        val result = calculator.calculateWeight(assignment1, course, assignmentGroups)

        // Only assignment1 counts (100 points)
        // 100 / 100 = 1.0
        // 1.0 * 50.0 = 50.0
        assertEquals(50.0, result!!, 0.001)
    }

    @Test
    fun `calculateWeight filters out assignments omitted from final grade`() {
        val course = Course(id = 1, isApplyAssignmentGroupWeights = true)

        val assignment1 = Assignment(id = 1, assignmentGroupId = 1, pointsPossible = 100.0)
        val assignment2 = Assignment(id = 2, assignmentGroupId = 1, pointsPossible = 100.0, omitFromFinalGrade = true)

        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                groupWeight = 60.0,
                assignments = arrayListOf(assignment1, assignment2)
            )
        )

        val result = calculator.calculateWeight(assignment1, course, assignmentGroups)

        // Only assignment1 counts (100 points)
        // 100 / 100 = 1.0
        // 1.0 * 60.0 = 60.0
        assertEquals(60.0, result!!, 0.001)
    }

    @Test
    fun `calculateWeight returns null when total points possible is zero after filtering`() {
        val course = Course(id = 1, isApplyAssignmentGroupWeights = true)

        // Both assignments are graded, and drop lowest = 2 means all are dropped
        val assignment1 = Assignment(
            id = 1,
            assignmentGroupId = 1,
            pointsPossible = 100.0,
            submission = Submission(grade = "50.0", score = 50.0)
        )
        val assignment2 = Assignment(
            id = 2,
            assignmentGroupId = 1,
            pointsPossible = 100.0,
            submission = Submission(grade = "60.0", score = 60.0)
        )

        val rules = GradingRule(dropLowest = 2)
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                groupWeight = 50.0,
                rules = rules,
                assignments = arrayListOf(assignment1, assignment2)
            )
        )

        val result = calculator.calculateWeight(assignment1, course, assignmentGroups)

        assertNull(result)
    }

    @Test
    fun `calculateWeight handles mix of graded and ungraded assignments with no graded ones`() {
        val course = Course(id = 1, isApplyAssignmentGroupWeights = true)

        val ungraded1 = Assignment(id = 1, assignmentGroupId = 1, pointsPossible = 100.0)
        val ungraded2 = Assignment(id = 2, assignmentGroupId = 1, pointsPossible = 50.0)

        val rules = GradingRule(dropLowest = 1)
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                groupWeight = 40.0,
                rules = rules,
                assignments = arrayListOf(ungraded1, ungraded2)
            )
        )

        val result = calculator.calculateWeight(ungraded1, course, assignmentGroups)

        // No graded assignments, so drop rules don't apply
        // All ungraded assignments count: 150 total points
        // ungraded1 has 100 out of 150 = 0.667
        // 0.667 * 40.0 = 26.667
        assertEquals(26.667, result!!, 0.001)
    }
}