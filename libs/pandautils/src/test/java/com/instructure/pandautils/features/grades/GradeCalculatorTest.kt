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

    // Kane & Kane Algorithm Specific Tests
    // These test scenarios where the binary search algorithm differs from simple percentage sorting

    @Test
    fun `calculates grade with unequal point values and both drop rules - Kane Kane advantage`() {
        // This tests the Kane & Kane algorithm's ability to handle complex drop scenarios
        // where assignments have very different point values
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                gradingRules = GradingRule(dropLowest = 1, dropHighest = 1),
                assignments = listOf(
                    createAssignment(1, "Quiz 1", 10.0, submission = createSubmission(8.0, posted = true)),  // 80%
                    createAssignment(2, "Essay", 100.0, submission = createSubmission(75.0, posted = true)),  // 75%
                    createAssignment(3, "Quiz 2", 10.0, submission = createSubmission(9.0, posted = true)),  // 90%
                    createAssignment(4, "Project", 100.0, submission = createSubmission(92.0, posted = true))  // 92%
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)

        // Kane & Kane finds the optimal combination:
        // Drop Essay (75%) as lowest percentage, then drop Project (92%) as highest
        // Keep: Quiz 1 (8/10) + Quiz 2 (9/10) = 17/20 = 85%
        // This is optimal because keeping the low-weight quizzes together maximizes the percentage
        assertEquals(85.0, result, 0.01)
    }

    @Test
    fun `calculates grade with extreme point differences and drop rules`() {
        // Tests scenario where one assignment is worth significantly more than others
        // This demonstrates Kane & Kane finding counterintuitive optimal solutions
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Work",
                gradingRules = GradingRule(dropLowest = 1),
                assignments = listOf(
                    createAssignment(1, "Participation", 5.0, submission = createSubmission(3.0, posted = true)),  // 60%
                    createAssignment(2, "Final Exam", 200.0, submission = createSubmission(170.0, posted = true)),  // 85%
                    createAssignment(3, "Quiz", 10.0, submission = createSubmission(7.0, posted = true))  // 70%
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)

        // Kane & Kane finds optimal: Drop Quiz (70%) instead of Participation (60%)!
        // Why? Keeping Participation (3/5) + Final (170/200) = 173/205 = 84.39%
        // vs dropping it: Quiz (7/10) + Final (170/200) = 177/210 = 84.29%
        // The lower-weight Participation helps the percentage more than the Quiz
        assertEquals(84.39, result, 0.01)
    }

    @Test
    fun `calculates grade with drop highest and lowest combined - order independence`() {
        // This tests that dropping happens correctly when both rules are present
        // The Kane & Kane algorithm handles this properly with two phases
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Tests",
                gradingRules = GradingRule(dropLowest = 2, dropHighest = 1),
                assignments = listOf(
                    createAssignment(1, "Test 1", 50.0, submission = createSubmission(35.0, posted = true)),  // 70%
                    createAssignment(2, "Test 2", 50.0, submission = createSubmission(40.0, posted = true)),  // 80%
                    createAssignment(3, "Test 3", 50.0, submission = createSubmission(45.0, posted = true)),  // 90%
                    createAssignment(4, "Test 4", 50.0, submission = createSubmission(42.0, posted = true)),  // 84%
                    createAssignment(5, "Test 5", 50.0, submission = createSubmission(48.0, posted = true))   // 96%
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)

        // Drop 2 lowest: Test 1 (70%) and Test 2 (80%)
        // Then drop 1 highest from remaining: Test 5 (96%)
        // Keep: Test 4 (84%) and Test 3 (90%)
        // Result: (42 + 45) / (50 + 50) = 87 / 100 = 87%
        assertEquals(87.0, result, 0.01)
    }

    @Test
    fun `calculates grade with weighted groups and complex drop rules - Kane Kane optimization`() {
        // Tests the algorithm with weighted groups where drop rules affect the overall grade significantly
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Homework",
                weight = 30.0,
                gradingRules = GradingRule(dropLowest = 1),
                assignments = listOf(
                    createAssignment(1, "HW1", 20.0, submission = createSubmission(16.0, posted = true)),  // 80%
                    createAssignment(2, "HW2", 50.0, submission = createSubmission(35.0, posted = true)),  // 70%
                    createAssignment(3, "HW3", 30.0, submission = createSubmission(27.0, posted = true))   // 90%
                )
            ),
            createAssignmentGroup(
                id = 2,
                name = "Exams",
                weight = 70.0,
                assignments = listOf(
                    createAssignment(4, "Midterm", 100.0, submission = createSubmission(85.0, posted = true)),
                    createAssignment(5, "Final", 100.0, submission = createSubmission(88.0, posted = true))
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = true, onlyGraded = true)

        // Homework: Drop HW2 (35/50 = 70%), keep HW1 (16/20) + HW3 (27/30) = 43/50 = 86%
        // Homework contribution: 86% * 30% = 25.8
        // Exams: (85 + 88) / 200 = 86.5%
        // Exams contribution: 86.5% * 70% = 60.55
        // Total: 25.8 + 60.55 = 86.35%
        assertEquals(86.35, result, 0.01)
    }

    @Test
    fun `calculates grade with never drop and drop rules combined - Kane Kane respects constraints`() {
        // Tests that never-drop assignments are properly excluded from dropping
        // even when Kane & Kane algorithm would otherwise consider them
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Work",
                gradingRules = GradingRule(dropLowest = 1, neverDrop = listOf(2)),
                assignments = listOf(
                    createAssignment(1, "Assignment 1", 50.0, submission = createSubmission(30.0, posted = true)),  // 60%
                    createAssignment(
                        2,
                        "Critical Assignment",
                        50.0,
                        submission = createSubmission(35.0, posted = true)
                    ),  // 70% - never drop
                    createAssignment(3, "Assignment 3", 50.0, submission = createSubmission(45.0, posted = true))   // 90%
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)

        // Must keep Assignment 2 (never drop)
        // Drop Assignment 1 (lowest eligible at 60%)
        // Keep: Assignment 2 (35/50) + Assignment 3 (45/50) = 80/100 = 80%
        assertEquals(80.0, result, 0.01)
    }

    @Test
    fun `calculates grade with what-if scores and drop rules - Kane Kane with hypotheticals`() {
        // Tests that what-if scores work correctly with the Kane & Kane algorithm
        val groups = listOf(
            createAssignmentGroup(
                id = 1,
                name = "Assignments",
                gradingRules = GradingRule(dropLowest = 1),
                assignments = listOf(
                    createAssignment(1, "A1", 100.0, submission = createSubmission(70.0, posted = true)),  // 70%
                    createAssignment(2, "A2", 100.0, submission = createSubmission(85.0, posted = true)),  // 85%
                    createAssignment(3, "A3", 100.0, submission = null)  // Not graded yet
                )
            )
        )

        // Student wants to see: "If I get 95 on A3, what will my grade be?"
        val whatIfScores = mapOf(3L to 95.0)

        val result = calculator.calculateGrade(groups, whatIfScores, applyGroupWeights = false, onlyGraded = true)

        // Drop A1 (70%), keep A2 (85%) and A3 with what-if (95%)
        // Result: (85 + 95) / 200 = 90%
        assertEquals(90.0, result, 0.01)
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

    @Test
    fun `Canvas LMS parity - zero-point assignments with weighted groups`() {
        // From CourseGradeCalculator1.test.js
        val groups = listOf(
            createAssignmentGroup(
                id = 301,
                name = "Group 1",
                weight = 50.0,
                assignments = listOf(
                    createAssignment(201, "Assignment 1", 0.0, submission = createSubmission(10.0, posted = true)),
                    createAssignment(202, "Assignment 2", 0.0, submission = createSubmission(5.0, posted = true))
                )
            ),
            createAssignmentGroup(
                id = 302,
                name = "Group 2",
                weight = 50.0,
                assignments = listOf(
                    createAssignment(203, "Assignment 3", 0.0, submission = createSubmission(20.0, posted = true)),
                    createAssignment(204, "Assignment 4", 0.0, submission = createSubmission(0.0, posted = true))
                )
            )
        )

        // With weighted groups, zero-point assignments should result in null score
        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = true, onlyGraded = true)

        // Canvas LMS returns null for this case, Android should return 0
        assertEquals(0.0, result, 0.01)
    }

    @Test
    fun `Canvas LMS parity - only ungraded submissions with weighted groups`() {
        // From CourseGradeCalculator1.test.js - only ungraded submissions
        val groups = listOf(
            createAssignmentGroup(
                id = 301,
                name = "Group",
                weight = 100.0,
                assignments = listOf(
                    createAssignment(201, "Assignment 1", 5.0, submission = null),
                    createAssignment(202, "Assignment 2", 10.0, submission = null),
                    createAssignment(203, "Assignment 3", 20.0, submission = null)
                )
            )
        )

        // Current grade (onlyGraded=true) should be 0 when no submissions exist
        val currentResult = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = true, onlyGraded = true)
        assertEquals(0.0, currentResult, 0.01)

        // Final grade (onlyGraded=false) should also be 0 (all ungraded count as 0)
        val finalResult = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = true, onlyGraded = false)
        assertEquals(0.0, finalResult, 0.01)
    }

    @Test
    fun `Canvas LMS parity - floating point precision with weighted groups`() {
        // From CourseGradeCalculator2.test.js - avoids floating point errors
        // 9.725 + 10 + 47.25 + 26.85 === 93.82499999999999 (should round to 93.83)
        val groups = listOf(
            createAssignmentGroup(
                id = 301,
                name = "Group 1",
                weight = 10.0,
                assignments = listOf(
                    createAssignment(201, "Assignment 1", 200.0, submission = createSubmission(194.5, posted = true))
                )
            ),
            createAssignmentGroup(
                id = 302,
                name = "Group 2",
                weight = 10.0,
                assignments = listOf(
                    createAssignment(202, "Assignment 2", 100.0, submission = createSubmission(100.0, posted = true))
                )
            ),
            createAssignmentGroup(
                id = 303,
                name = "Group 3",
                weight = 50.0,
                assignments = listOf(
                    createAssignment(203, "Assignment 3", 100.0, submission = createSubmission(94.5, posted = true))
                )
            ),
            createAssignmentGroup(
                id = 304,
                name = "Group 4",
                weight = 30.0,
                assignments = listOf(
                    createAssignment(204, "Assignment 4", 100.0, submission = createSubmission(89.5, posted = true))
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = true, onlyGraded = true)

        // Group 1: 194.5/200 = 97.25% * 10% = 9.725
        // Group 2: 100/100 = 100% * 10% = 10
        // Group 3: 94.5/100 = 94.5% * 50% = 47.25
        // Group 4: 89.5/100 = 89.5% * 30% = 26.85
        // Total: 93.82499999999999 should round to 93.83
        assertEquals(93.83, result, 0.01)
    }

    @Test
    fun `Canvas LMS parity - floating point precision without weights`() {
        // From CourseGradeCalculator2.test.js
        // 110.1 + 170.7 === 280.79999999999995 (should round to 280.8)
        val groups = listOf(
            createAssignmentGroup(
                id = 301,
                name = "Group 1",
                weight = 10.0,
                assignments = listOf(
                    createAssignment(201, "Assignment 1", 120.0, submission = createSubmission(110.1, posted = true))
                )
            ),
            createAssignmentGroup(
                id = 302,
                name = "Group 2",
                weight = 10.0,
                assignments = listOf(
                    createAssignment(202, "Assignment 2", 180.0, submission = createSubmission(170.7, posted = true))
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)

        // (110.1 + 170.7) / (120 + 180) = 280.8 / 300 = 93.6%
        assertEquals(93.6, result, 0.01)
    }

    @Test
    fun `Canvas LMS parity - empty course with no submissions`() {
        // From CourseGradeCalculator2.test.js - no submissions and no assignments
        val groups = listOf(
            createAssignmentGroup(
                id = 301,
                name = "Empty Group",
                weight = 100.0,
                assignments = emptyList()
            )
        )

        // Both current and final should be 0 for empty course
        val currentResult = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)
        assertEquals(0.0, currentResult, 0.01)

        val finalResult = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = false)
        assertEquals(0.0, finalResult, 0.01)
    }

    @Test
    fun `Canvas LMS parity - mixed graded and ungraded with points`() {
        // From CourseGradeCalculator2.test.js
        val groups = listOf(
            createAssignmentGroup(
                id = 301,
                name = "Group 1",
                weight = 50.0,
                assignments = listOf(
                    createAssignment(201, "Assignment 1", 100.0, submission = createSubmission(100.0, posted = true)),
                    createAssignment(202, "Assignment 2", 91.0, submission = createSubmission(42.0, posted = true))
                )
            ),
            createAssignmentGroup(
                id = 302,
                name = "Group 2",
                weight = 50.0,
                assignments = listOf(
                    createAssignment(203, "Assignment 3", 55.0, submission = createSubmission(14.0, posted = true)),
                    createAssignment(204, "Assignment 4", 38.0, submission = createSubmission(3.0, posted = true)),
                    createAssignment(205, "Assignment 5", 1000.0, submission = null)
                )
            )
        )

        // Current grade (only graded): (100 + 42 + 14 + 3) / (100 + 91 + 55 + 38) = 159 / 284 = 55.99%
        val currentResult = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = true)
        assertEquals(55.99, currentResult, 0.01)

        // Final grade (include ungraded as 0): (100 + 42 + 14 + 3 + 0) / (100 + 91 + 55 + 38 + 1000) = 159 / 1284 = 12.38%
        val finalResult = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = false, onlyGraded = false)
        assertEquals(12.38, finalResult, 0.01)
    }

    @Test
    fun `Canvas LMS parity - assignment group weight rounding`() {
        // From CourseGradeCalculator2.test.js - avoids floating point errors in weights
        val groups = listOf(
            createAssignmentGroup(
                id = 301,
                name = "Group 1",
                weight = 33.33,
                assignments = listOf(
                    createAssignment(201, "Assignment 1", 148.0, submission = createSubmission(124.46, posted = true))
                )
            ),
            createAssignmentGroup(
                id = 302,
                name = "Group 2",
                weight = 33.33,
                assignments = listOf(
                    createAssignment(202, "Assignment 2", 148.0, submission = createSubmission(144.53, posted = true))
                )
            ),
            createAssignmentGroup(
                id = 303,
                name = "Group 3",
                weight = 33.34,
                assignments = listOf(
                    createAssignment(203, "Assignment 3", 148.0, submission = createSubmission(135.0, posted = true))
                )
            )
        )

        val result = calculator.calculateGrade(groups, emptyMap(), applyGroupWeights = true, onlyGraded = true)

        // Group 1: 124.46/148 = 84.095% * 33.33% = 28.02
        // Group 2: 144.53/148 = 97.655% * 33.33% = 32.55
        // Group 3: 135/148 = 91.216% * 33.34% = 30.41
        // Total: ~90.98%
        assertEquals(90.98, result, 0.01)
    }
}
