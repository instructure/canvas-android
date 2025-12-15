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
import com.instructure.canvasapi2.utils.toDate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GradeCalculatorTest {

    private lateinit var calculator: GradeCalculator

    @Before
    fun setup() {
        calculator = GradeCalculator()
    }

    // Basic grade calculation tests

    @Test
    fun calculateGrade_simplePercentage_returnsCorrectGrade() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = createSubmission(80.0, posted = true)),
                    createAssignment(2, "Assignment 2", 100.0, submission = createSubmission(90.0, posted = true))
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)

        assertEquals(85.0, result, 0.01)
    }

    @Test
    fun calculateGrade_emptyGroups_returnsZero() {
        val result = calculator.calculateGrade(emptyList(), emptyMap(), applyGroupWeights = false, onlyGraded = true)

        assertEquals(0.0, result, 0.01)
    }

    @Test
    fun calculateGrade_noGradedAssignments_returnsZero() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = null),
                    createAssignment(2, "Assignment 2", 100.0, submission = null)
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)

        assertEquals(0.0, result, 0.01)
    }

    // Assignment group weights tests

    @Test
    fun calculateGrade_withGroupWeights_appliesWeightsCorrectly() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Homework",
                weight = 60.0,
                assignments = listOf(
                    createAssignment(1, "HW 1", 100.0, submission = createSubmission(80.0, posted = true))
                )
            ),
            createAssignmentGroup(
                id = 2,
                name = "Exams",
                weight = 40.0,
                assignments = listOf(
                    createAssignment(2, "Exam 1", 100.0, submission = createSubmission(90.0, posted = true))
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = true, onlyGraded = true)

        // (80 * 0.6) + (90 * 0.4) = 48 + 36 = 84
        assertEquals(84.0, result, 0.01)
    }

    @Test
    fun calculateGrade_withGroupWeights_normalizesMissingWeights() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Homework",
                weight = 60.0,
                assignments = listOf(
                    createAssignment(1, "HW 1", 100.0, submission = createSubmission(100.0, posted = true))
                )
            ),
            createAssignmentGroup(
                id = 2,
                name = "Exams",
                weight = 0.0,
                assignments = listOf(
                    createAssignment(2, "Exam 1", 100.0, submission = createSubmission(50.0, posted = true))
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = true, onlyGraded = true)

        // Exams has 0 weight, so only Homework counts (100%)
        assertEquals(100.0, result, 0.01)
    }

    // Drop rules tests

    @Test
    fun calculateGrade_dropLowest_dropsLowestScore() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                gradingRules = GradingRule(dropLowest = 1),
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = createSubmission(60.0, posted = true)),
                    createAssignment(2, "Assignment 2", 100.0, submission = createSubmission(80.0, posted = true)),
                    createAssignment(3, "Assignment 3", 100.0, submission = createSubmission(90.0, posted = true))
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)

        // Drop 60, keep 80 and 90: (80 + 90) / 200 = 85%
        assertEquals(85.0, result, 0.01)
    }

    @Test
    fun calculateGrade_dropHighest_dropsHighestScore() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                gradingRules = GradingRule(dropHighest = 1),
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = createSubmission(60.0, posted = true)),
                    createAssignment(2, "Assignment 2", 100.0, submission = createSubmission(80.0, posted = true)),
                    createAssignment(3, "Assignment 3", 100.0, submission = createSubmission(90.0, posted = true))
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)

        // Drop 90, keep 60 and 80: (60 + 80) / 200 = 70%
        assertEquals(70.0, result, 0.01)
    }

    @Test
    fun calculateGrade_dropLowestAndHighest_dropsCorrectly() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                gradingRules = GradingRule(dropLowest = 1, dropHighest = 1),
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = createSubmission(50.0, posted = true)),
                    createAssignment(2, "Assignment 2", 100.0, submission = createSubmission(70.0, posted = true)),
                    createAssignment(3, "Assignment 3", 100.0, submission = createSubmission(80.0, posted = true)),
                    createAssignment(4, "Assignment 4", 100.0, submission = createSubmission(95.0, posted = true))
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)

        // Drop 50 and 95, keep 70 and 80: (70 + 80) / 200 = 75%
        assertEquals(75.0, result, 0.01)
    }

    @Test
    fun calculateGrade_neverDrop_preventsDropping() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                gradingRules = GradingRule(dropLowest = 1, neverDrop = listOf(1L)),
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = createSubmission(60.0, posted = true)),
                    createAssignment(2, "Assignment 2", 100.0, submission = createSubmission(80.0, posted = true)),
                    createAssignment(3, "Assignment 3", 100.0, submission = createSubmission(90.0, posted = true))
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)

        // Assignment 1 is never-drop, so drop Assignment 2 (80) instead
        // Keep 60 and 90: (60 + 90) / 200 = 75%
        assertEquals(75.0, result, 0.01)
    }

    @Test
    fun calculateGrade_dropMoreThanAvailable_dropsAllEligible() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                gradingRules = GradingRule(dropLowest = 5),
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = createSubmission(80.0, posted = true)),
                    createAssignment(2, "Assignment 2", 100.0, submission = createSubmission(90.0, posted = true))
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)

        // Can't drop more than available, drops 1 (the lowest)
        assertEquals(90.0, result, 0.01)
    }

    // What-if scores tests

    @Test
    fun calculateGrade_whatIfScore_usesWhatIfInsteadOfActual() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = createSubmission(70.0, posted = true)),
                    createAssignment(2, "Assignment 2", 100.0, submission = createSubmission(80.0, posted = true))
                )
            )
        )

        val whatIfScores = mapOf(1L to 95.0)

        val result = calculator.calculateGrade(groups, whatIfScores, applyGroupWeights = false, onlyGraded = true)

        // Use what-if score 95 for assignment 1: (95 + 80) / 200 = 87.5%
        assertEquals(87.5, result, 0.01)
    }

    @Test
    fun calculateGrade_whatIfScoreForUnsubmitted_countsAsSubmitted() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = createSubmission(80.0, posted = true)),
                    createAssignment(2, "Assignment 2", 100.0, submission = null)
                )
            )
        )

        val whatIfScores = mapOf(2L to 90.0)

        val result = calculator.calculateGrade(groups, whatIfScores, applyGroupWeights = false, onlyGraded = true)

        // What-if on unsubmitted counts as submitted: (80 + 90) / 200 = 85%
        assertEquals(85.0, result, 0.01)
    }

    @Test
    fun calculateGrade_whatIfWithDropRules_appliesDropToWhatIf() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                gradingRules = GradingRule(dropLowest = 1),
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = createSubmission(60.0, posted = true)),
                    createAssignment(2, "Assignment 2", 100.0, submission = createSubmission(80.0, posted = true)),
                    createAssignment(3, "Assignment 3", 100.0, submission = createSubmission(90.0, posted = true))
                )
            )
        )

        val whatIfScores = mapOf(1L to 95.0)

        val result = calculator.calculateGrade(groups, whatIfScores, applyGroupWeights = false, onlyGraded = true)

        // What-if changes 60 to 95. Now drop 80 (new lowest): (95 + 90) / 200 = 92.5%
        assertEquals(92.5, result, 0.01)
    }

    // Excused assignments tests

    @Test
    fun calculateGrade_excusedSubmission_notIncluded() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = createSubmission(80.0, posted = true)),
                    createAssignment(2, "Assignment 2", 100.0, submission = createSubmission(0.0, posted = true, excused = true)),
                    createAssignment(3, "Assignment 3", 100.0, submission = createSubmission(90.0, posted = true))
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)

        // Excused assignment 2 not included: (80 + 90) / 200 = 85%
        assertEquals(85.0, result, 0.01)
    }

    @Test
    fun calculateGrade_excusedWithWhatIfScore_stillExcluded() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = createSubmission(80.0, posted = true)),
                    createAssignment(2, "Assignment 2", 100.0, submission = createSubmission(0.0, posted = true, excused = true)),
                    createAssignment(3, "Assignment 3", 100.0, submission = createSubmission(90.0, posted = true))
                )
            )
        )

        val whatIfScores = mapOf(2L to 100.0)

        val result = calculator.calculateGrade(groups, whatIfScores, applyGroupWeights = false, onlyGraded = true)

        // Excused assignments stay excused even with what-if: (80 + 90) / 200 = 85%
        assertEquals(85.0, result, 0.01)
    }

    // Unposted submissions tests

    @Test
    fun calculateGrade_unpostedSubmission_notIncludedInCurrentGrade() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = createSubmission(80.0, posted = true)),
                    createAssignment(2, "Assignment 2", 100.0, submission = createSubmission(90.0, posted = false)),
                    createAssignment(3, "Assignment 3", 100.0, submission = createSubmission(70.0, posted = true))
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)

        // Unposted assignment 2 not included: (80 + 70) / 200 = 75%
        assertEquals(75.0, result, 0.01)
    }

    @Test
    fun calculateGrade_unpostedSubmission_includedInFinalGrade() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = createSubmission(80.0, posted = true)),
                    createAssignment(2, "Assignment 2", 100.0, submission = createSubmission(90.0, posted = false)),
                    createAssignment(3, "Assignment 3", 100.0, submission = null)
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = false)

        // Include all assignments: (80 + 90 + 0) / 300 = 56.67%
        assertEquals(56.67, result, 0.01)
    }

    @Test
    fun calculateGrade_unpostedWithWhatIfScore_includedInCurrentGrade() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = createSubmission(80.0, posted = true)),
                    createAssignment(2, "Assignment 2", 100.0, submission = createSubmission(90.0, posted = false))
                )
            )
        )

        val whatIfScores = mapOf(2L to 95.0)

        val result = calculator.calculateGrade(groups, whatIfScores, applyGroupWeights = false, onlyGraded = true)

        // What-if score makes unposted assignment count: (80 + 95) / 200 = 87.5%
        assertEquals(87.5, result, 0.01)
    }

    // Include ungraded tests

    @Test
    fun calculateGrade_includeUngraded_countsUnsubmittedAsZero() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = createSubmission(80.0, posted = true)),
                    createAssignment(2, "Assignment 2", 100.0, submission = createSubmission(90.0, posted = true)),
                    createAssignment(3, "Assignment 3", 100.0, submission = null),
                    createAssignment(4, "Assignment 4", 100.0, submission = null)
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = false)

        // Include all 4 assignments: (80 + 90 + 0 + 0) / 400 = 42.5%
        assertEquals(42.5, result, 0.01)
    }

    @Test
    fun calculateGrade_includeUngradedFalse_excludesUnsubmitted() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = createSubmission(80.0, posted = true)),
                    createAssignment(2, "Assignment 2", 100.0, submission = createSubmission(90.0, posted = true)),
                    createAssignment(3, "Assignment 3", 100.0, submission = null)
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)

        // Only graded assignments: (80 + 90) / 200 = 85%
        assertEquals(85.0, result, 0.01)
    }

    // Edge cases

    @Test
    fun calculateGrade_allExcused_returnsZero() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = createSubmission(0.0, posted = true, excused = true)),
                    createAssignment(2, "Assignment 2", 100.0, submission = createSubmission(0.0, posted = true, excused = true))
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)

        assertEquals(0.0, result, 0.01)
    }

    @Test
    fun calculateGrade_zeroPointAssignments_notIncludedInCalculation() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = createSubmission(80.0, posted = true)),
                    createAssignment(2, "Assignment 2", 0.0, submission = createSubmission(0.0, posted = true)),
                    createAssignment(3, "Assignment 3", 100.0, submission = createSubmission(90.0, posted = true))
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)

        // Zero-point assignment 2 excluded: (80 + 90) / 200 = 85%
        assertEquals(85.0, result, 0.01)
    }

    @Test
    fun calculateGrade_decimalPoints_handledCorrectly() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 50.0, submission = createSubmission(42.5, posted = true)),
                    createAssignment(2, "Assignment 2", 75.0, submission = createSubmission(63.75, posted = true))
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)

        // (42.5 + 63.75) / 125 = 85%
        assertEquals(85.0, result, 0.01)
    }

    @Test
    fun calculateGrade_perfectScore_returns100() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 100.0, submission = createSubmission(100.0, posted = true)),
                    createAssignment(2, "Assignment 2", 100.0, submission = createSubmission(100.0, posted = true))
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)

        assertEquals(100.0, result, 0.01)
    }

    // Complex scenario tests

    @Test
    fun calculateGrade_complexScenario_handlesCorrectly() {
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Homework",
                weight = 40.0,
                gradingRules = GradingRule(dropLowest = 1),
                assignments = listOf(
                    createAssignment(1, "HW 1", 100.0, submission = createSubmission(70.0, posted = true)),
                    createAssignment(2, "HW 2", 100.0, submission = createSubmission(80.0, posted = true)),
                    createAssignment(3, "HW 3", 100.0, submission = createSubmission(90.0, posted = true))
                )
            ),
            createAssignmentGroup(
                id = 2,
                name = "Exams",
                weight = 60.0,
                assignments = listOf(
                    createAssignment(4, "Exam 1", 200.0, submission = createSubmission(160.0, posted = true)),
                    createAssignment(5, "Exam 2", 200.0, submission = null)
                )
            )
        )

        val whatIfScores = mapOf(5L to 180.0)

        val result = calculator.calculateGrade(groups, whatIfScores, applyGroupWeights = true, onlyGraded = true)

        // Homework: Drop 70, keep 80 and 90 = (170/200) = 85%
        // Exams: (160 + 180) / 400 = 85%
        // Final: (85 * 0.4) + (85 * 0.6) = 34 + 51 = 85%
        assertEquals(85.0, result, 0.01)
    }

    // Helper methods

    private fun createAssignmentGroup(
        id: Long,
        name: String,
        weight: Double = 0.0,
        gradingRules: GradingRule? = null,
        assignments: List<Assignment>
    ): AssignmentGroup {
        return AssignmentGroup(
            id = id,
            name = name,
            groupWeight = weight,
            rules = gradingRules,
            assignments = assignments
        )
    }

    private fun createAssignment(
        id: Long,
        name: String,
        pointsPossible: Double,
        submission: Submission? = null
    ): Assignment {
        return Assignment(
            id = id,
            name = name,
            pointsPossible = pointsPossible,
            submission = submission,
            published = true,
            submissionTypesRaw = listOf("online_text_entry")
        )
    }

    private fun createSubmission(
        score: Double,
        posted: Boolean,
        excused: Boolean = false
    ): Submission {
        return Submission(
            score = score,
            grade = score.toString(),
            postedAt = if (posted) "2024-01-01T00:00:00Z".toDate() else null,
            excused = excused
        )
    }
}
