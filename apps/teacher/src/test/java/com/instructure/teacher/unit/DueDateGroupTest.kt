/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.teacher.unit

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentDueDate
import com.instructure.canvasapi2.models.AssignmentOverride
import com.instructure.canvasapi2.models.postmodels.AssignmentPostBody
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import com.instructure.teacher.utils.coreDates
import com.instructure.teacher.utils.groupedDueDates
import com.instructure.teacher.utils.setGroupedDueDates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.*

@Suppress("IllegalIdentifier")
class DueDateGroupTest {

    private lateinit var postData: AssignmentPostBody
    private lateinit var assignmentWithoutOverrides: Assignment
    private lateinit var assignmentWithSimilarOverrides: Assignment
    private lateinit var assignmentWithDifferentOverrides: Assignment
    private lateinit var assignmentWithoutBaseDate: Assignment

    private val randy = Random()

    private val sampleDateStrings = Array(15) { Date(Math.abs(randy.nextLong())).toApiString() }

    private val sampleDates = sampleDateStrings.map { it.toDate() }

    @Before
    fun setup() {
        postData = AssignmentPostBody()

        assignmentWithoutOverrides = Assignment(
                dueAt = sampleDateStrings[0],
                lockAt = sampleDateStrings[0],
                unlockAt = sampleDateStrings[0],
                allDates = listOf(AssignmentDueDate(
                        isBase = true,
                        dueAt = sampleDateStrings[0],
                        lockAt = sampleDateStrings[0],
                        unlockAt = sampleDateStrings[0])
                ))

        val allDueDates = arrayListOf(
                AssignmentDueDate(
                        isBase = true,
                        dueAt = sampleDateStrings[0],
                        lockAt = sampleDateStrings[0],
                        unlockAt = sampleDateStrings[0]
                ))

        allDueDates += (1..3).map {
            AssignmentDueDate(
                    id = it.toLong(),
                    dueAt = sampleDateStrings[0],
                    lockAt = sampleDateStrings[0],
                    unlockAt = sampleDateStrings[0]
            )
        }


        assignmentWithSimilarOverrides = Assignment(
                dueAt = sampleDateStrings[0],
                lockAt = sampleDateStrings[0],
                unlockAt = sampleDateStrings[0],

                overrides = (1..3).map {
                    val ao = AssignmentOverride(
                            id = it.toLong(),
                            dueAt = sampleDates[0],
                            lockAt = sampleDates[0],
                            unlockAt = sampleDates[0])
                    when (it) {
                        1 -> ao.copy(courseSectionId = Math.abs(randy.nextLong()))
                        2 -> ao.copy(groupId = Math.abs(randy.nextLong()))
                        3 -> ao.copy(studentIds = LongArray(randy.nextInt(5) + 1) { Math.abs(randy.nextLong()) }.toList())
                        else -> ao
                    }
                },
                allDates = allDueDates
        )


        assignmentWithoutBaseDate = Assignment(
                onlyVisibleToOverrides = true,

                overrides = (1..3).map {
                    val ao = AssignmentOverride(
                            id = it.toLong(),
                            dueAt = sampleDates[0],
                            lockAt = sampleDates[0],
                            unlockAt = sampleDates[0])
                    when (it) {
                        1 -> ao.copy(courseSectionId = Math.abs(randy.nextLong()))
                        2 -> ao.copy(groupId = Math.abs(randy.nextLong()))
                        3 -> ao.copy(studentIds = LongArray(randy.nextInt(5) + 1) { Math.abs(randy.nextLong()) }.toList())
                        else -> ao
                    }
                },


                allDates = (1..3).map {
                    AssignmentDueDate(
                            id = it.toLong(),
                            dueAt = sampleDateStrings[0],
                            lockAt = sampleDateStrings[0],
                            unlockAt = sampleDateStrings[0])
                })




        assignmentWithDifferentOverrides = Assignment(
                dueAt = sampleDateStrings[0],
                lockAt = sampleDateStrings[0],
                unlockAt = sampleDateStrings[0],
                overrides = (1 until sampleDateStrings.size).map {
                    val ao = AssignmentOverride(
                            id = it.toLong(),
                            dueAt = sampleDates[it],
                            lockAt = sampleDates[it],
                            unlockAt = sampleDates[it])
                    when (it % 3) {
                        0 -> ao.copy(courseSectionId = Math.abs(randy.nextLong()))
                        1 -> ao.copy(groupId = Math.abs(randy.nextLong()))
                        2 -> ao.copy(studentIds = LongArray(randy.nextInt(5) + 1) { Math.abs(randy.nextLong()) }.toList())
                        else -> ao
                    }
                },
                allDates = (0 until sampleDateStrings.size).map {
                    AssignmentDueDate(
                            id = if (it != 0) it.toLong() else 0,
                            isBase = it == 0,
                            dueAt = sampleDateStrings[it],
                            lockAt = sampleDateStrings[it],
                            unlockAt = sampleDateStrings[it]
                    )
                }
        )
    }

    @Test
    fun `Has correct date contents for assignment without overrides`() {
        val dates = assignmentWithoutOverrides.groupedDueDates
        assertEquals(1, dates.size)
        with(dates.first()) {
            assertEquals(true, this.isEveryone)
            assertEquals(sampleDates[0], coreDates.dueDate)
            assertEquals(sampleDates[0], coreDates.lockDate)
            assertEquals(sampleDates[0], coreDates.lockDate)
            assertEquals(0, sectionIds.size)
            assertEquals(0, groupIds.size)
            assertEquals(0, studentIds.size)
        }
    }

    @Test
    fun `Has correct date contents for assignment with similar overrides`() {
        val dates = assignmentWithSimilarOverrides.groupedDueDates
        assertEquals(1, dates.size)
        with(dates.first()) {
            assertEquals(true, this.isEveryone)
            assertEquals(sampleDates[0], coreDates.dueDate)
            assertEquals(sampleDates[0], coreDates.lockDate)
            assertEquals(sampleDates[0], coreDates.lockDate)
            assertEquals(1, sectionIds.size)
            assertEquals(1, groupIds.size)
            assertEquals(false, studentIds.isEmpty())
        }
    }

    @Test
    fun `Has correct date contents for assignment without base date`() {
        val dates = assignmentWithoutBaseDate.groupedDueDates
        assertEquals(1, dates.size)
        with(dates.first()) {
            assertEquals(false, this.isEveryone)
            assertEquals(sampleDates[0], coreDates.dueDate)
            assertEquals(sampleDates[0], coreDates.lockDate)
            assertEquals(sampleDates[0], coreDates.lockDate)
            assertEquals(1, sectionIds.size)
            assertEquals(1, groupIds.size)
            assertEquals(false, studentIds.isEmpty())
        }
    }

    @Test
    fun `Has correct date contents for assignment with different overrides`() {
        val dates = assignmentWithDifferentOverrides.groupedDueDates
        assertEquals(sampleDates.size, dates.size)
        for (i in 0 until sampleDates.size) {
            with(dates.first { it.coreDates.dueDate == sampleDates[i] }) {
                assertEquals(i == 0, isEveryone)
                assertEquals(sampleDates[i], coreDates.dueDate)
                assertEquals(sampleDates[i], coreDates.lockDate)
                assertEquals(sampleDates[i], coreDates.lockDate)
                if (i > 0) when (i % 3) {
                    0 -> assertEquals(1, sectionIds.size)
                    1 -> assertEquals(1, groupIds.size)
                    2 -> assertEquals(false, studentIds.isEmpty())
                }
            }
        }
    }

    @Test
    fun `Correctly saves dates for assignment without overrides`() {
        postData.setGroupedDueDates(assignmentWithoutOverrides.groupedDueDates)
        assertUpdatedForSave(assignmentWithoutOverrides, postData)
    }

    @Test
    fun `Correctly saves dates for assignment with similar overrides`() {
        postData.setGroupedDueDates(assignmentWithSimilarOverrides.groupedDueDates)
        assertUpdatedForSave(assignmentWithSimilarOverrides, postData)
    }

    @Test
    fun `Correctly saves dates for assignment without base date`() {
        postData.setGroupedDueDates(assignmentWithoutBaseDate.groupedDueDates)
        assertUpdatedForSave(assignmentWithoutBaseDate, postData)
    }

    @Test
    fun `Correctly saves dates for assignment different overrides`() {
        postData.setGroupedDueDates(assignmentWithDifferentOverrides.groupedDueDates)
        assertUpdatedForSave(assignmentWithDifferentOverrides, postData)
    }

    private fun assertUpdatedForSave(original: Assignment, saved: AssignmentPostBody) {
        val noBase = original.allDates.none { it.isBase }

        if (noBase) {
            assertNull(saved.dueAt)
            assertNull(saved.lockAt)
            assertNull(saved.unlockAt)
            assertEquals(original.allDates.size > 1, saved.isOnlyVisibleToOverrides)
        }

        val originalSections = original.overrides?.filter { it.courseSectionId > 0 }?.map { it.courseSectionId }?.sorted() ?: emptyList()
        val savedSections = saved.assignmentOverrides?.filter { it.courseSectionId != null }?.map { it.courseSectionId!! }?.sorted() ?: emptyList()
        assertEquals(originalSections, savedSections)

        val originalGroups = original.overrides?.filter { it.groupId > 0 }?.map { it.groupId }?.sorted() ?: emptyList()
        val savedGroups = saved.assignmentOverrides?.filter { it.groupId != null }?.map { it.groupId!! }?.sorted() ?: emptyList()
        assertEquals(originalGroups, savedGroups)

        val originalStudents = original.overrides?.flatMap { it.studentIds }?.sorted() ?: emptyList()
        val savedStudents = saved.assignmentOverrides?.flatMap { it.studentIds?.asList() ?: emptyList() }?.sorted() ?: emptyList()
        assertEquals(originalStudents, savedStudents)
        assertEquals(original.overrides?.size ?: 0, saved.assignmentOverrides?.size ?: 0)
        assertEquals(original.coreDates, saved.coreDates)
    }

}