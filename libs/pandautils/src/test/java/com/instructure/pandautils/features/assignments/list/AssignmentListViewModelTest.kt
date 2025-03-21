/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.assignments.list

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.compose.composables.GroupedListViewEvent
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterGroup
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterGroupType
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterOption
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterState
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterType
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListGroupByOption
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ScreenState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class AssignmentListViewModelTest {

    private val course: Course = mockk(relaxed = true)
    private val assignmentGroups: List<AssignmentGroup> = listOf(
        mockk(relaxed = true),
        mockk(relaxed = true),
        mockk(relaxed = true)
    )
    private val gradingPeriods: List<GradingPeriod> = listOf(
        mockk(relaxed = true),
        mockk(relaxed = true),
        mockk(relaxed = true)
    )

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val behavior: AssignmentListBehavior = mockk(relaxed = true)
    private val repository: AssignmentListRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        ContextKeeper.appContext = mockk(relaxed = true)

        every { savedStateHandle.get<Long>(Const.COURSE_ID) } returns 0L
        coEvery { repository.getAssignments(any(), any()) } returns DataResult.Success(assignmentGroups)
        coEvery { repository.getCourse(any(), any()) } returns DataResult.Success(course)
        coEvery { repository.getGradingPeriodsForCourse(any(), any()) } returns DataResult.Success(gradingPeriods)
        coEvery { repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), any(), any()) } returns DataResult.Success(assignmentGroups)
        coEvery { repository.getSelectedOptions(any(), any(), any(), any()) } returns null
        coEvery { repository.updateSelectedOptions(any(), any(), any(), any()) } just runs
        every { behavior.getAssignmentListFilterState(any(), any(), any()) } returns AssignmentListFilterState()
        every { behavior.getAssignmentGroupItemState(any()) } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Test error state on Fail`() = runTest {
        coEvery { repository.getAssignments(any(), any()) } returns DataResult.Fail()
        val viewModel = getViewModel()
        assertEquals(ScreenState.Error, viewModel.uiState.value.state)
    }

    @Test
    fun `Test empty state on Empty`() = runTest {
        coEvery { repository.getAssignments(any(), any()) } returns DataResult.Success(emptyList())
        val viewModel = getViewModel()
        assertEquals(ScreenState.Empty, viewModel.uiState.value.state)
    }

    @Test
    fun `Test List item click event handler`() = runTest {
        val assignmentId = 1L
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 1,
                        name = "Assignment 1",
                    ),
                )
            ),
        )
        val groupItem = AssignmentGroupItemState(assignmentGroups.first().assignments.first())
        every { behavior.getAssignmentGroupItemState(assignmentGroups.first().assignments.first()) } returns groupItem
        coEvery { repository.getAssignments(any(), any()) } returns DataResult.Success(assignmentGroups)
        val viewModel = getViewModel()

        val events = mutableListOf<AssignmentListFragmentEvent>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleListEvent(GroupedListViewEvent.ItemClicked(groupItem))

        assertEquals(AssignmentListFragmentEvent.NavigateToAssignment(viewModel.uiState.value.course, assignmentId), events.last())
    }

    @Test
    fun `Test List group click event handler`() = runTest {
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 1,
                        name = "Assignment 1",
                    ),
                )
            ),
        )
        val groupItem = AssignmentGroupItemState(assignmentGroups.first().assignments.first())
        every { behavior.getAssignmentGroupItemState(assignmentGroups.first().assignments.first()) } returns groupItem
        coEvery { repository.getAssignments(any(), any()) } returns DataResult.Success(assignmentGroups)
        val viewModel = getViewModel()

        var group = viewModel.uiState.value.listState.groups.first()
        assertEquals(true, group.isExpanded)

        viewModel.handleListEvent(GroupedListViewEvent.GroupClicked(group))

        group = viewModel.uiState.value.listState.groups.first()
        assertEquals(false, group.isExpanded)

        viewModel.handleListEvent(GroupedListViewEvent.GroupClicked(group))

        group = viewModel.uiState.value.listState.groups.first()
        assertEquals(true, group.isExpanded)
    }

    @Test
    fun `Test Navigate Back action handler`() = runTest {
        val viewModel = getViewModel()

        val events = mutableListOf<AssignmentListFragmentEvent>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(AssignmentListScreenEvent.NavigateBack)

        assertEquals(AssignmentListFragmentEvent.NavigateBack, events.last())
    }

    @Test
    fun `Test Update Filters action handler`() = runTest {
        val viewModel = getViewModel()
        val newFilters = AssignmentListFilterState(filterGroups = listOf(mockk(relaxed = true)))

        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilters))

        assertEquals(newFilters, viewModel.uiState.value.filterState)

        coVerify(exactly = 1) { repository.updateSelectedOptions(any(), any(), any(), any()) }
    }

    @Test
    fun `Test Open Filter screen action handler`() = runTest {
        val viewModel = getViewModel()

        assertEquals(AssignmentListScreenOption.List, viewModel.uiState.value.screenOption)
        viewModel.handleAction(AssignmentListScreenEvent.OpenFilterScreen)
        assertEquals(AssignmentListScreenOption.Filter, viewModel.uiState.value.screenOption)
    }

    @Test
    fun `Test Close Filter screen action handler`() = runTest {
        val viewModel = getViewModel()

        viewModel.handleAction(AssignmentListScreenEvent.OpenFilterScreen)
        assertEquals(AssignmentListScreenOption.Filter, viewModel.uiState.value.screenOption)
        viewModel.handleAction(AssignmentListScreenEvent.CloseFilterScreen)
        assertEquals(AssignmentListScreenOption.List, viewModel.uiState.value.screenOption)
    }

    @Test
    fun `Test Expand and Collapse search bar action handler`() = runTest {
        val viewModel = getViewModel()

        assertEquals(false, viewModel.uiState.value.searchBarExpanded)
        viewModel.handleAction(AssignmentListScreenEvent.ExpandCollapseSearchBar(true))
        assertEquals(true, viewModel.uiState.value.searchBarExpanded)
        viewModel.handleAction(AssignmentListScreenEvent.ExpandCollapseSearchBar(false))
        assertEquals(false, viewModel.uiState.value.searchBarExpanded)
    }

    @Test
    fun `Test Search content changed action handler`() = runTest {
        val viewModel = getViewModel()
        val query = "test"

        assertEquals("", viewModel.uiState.value.searchQuery)
        viewModel.handleAction(AssignmentListScreenEvent.SearchContentChanged(query))
        assertEquals(query, viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `Test Overflow menu state action handler`() = runTest {
        val viewModel = getViewModel()

        assertEquals(false, viewModel.uiState.value.overFlowItemsExpanded)
        viewModel.handleAction(AssignmentListScreenEvent.ChangeOverflowMenuState(true))
        assertEquals(true, viewModel.uiState.value.overFlowItemsExpanded)
        viewModel.handleAction(AssignmentListScreenEvent.ChangeOverflowMenuState(false))
        assertEquals(false, viewModel.uiState.value.overFlowItemsExpanded)
    }

    @Test
    fun `Test Refresh action handler`() = runTest {
        val viewModel = getViewModel()
        viewModel.handleAction(AssignmentListScreenEvent.Refresh)

        coVerify(exactly = 1) { repository.getCourse(any(), true) }
        coVerify(exactly = 1) { repository.getAssignments(any(), true) }
        coVerify(exactly = 1) { repository.getGradingPeriodsForCourse(any(), true) }
    }

    @Test
    fun `Test Published and Unpublished status filter`() = runTest {
        val assignment1 = Assignment(
            id = 1,
            name = "Assignment 1",
            published = true,
        )
        val assignment2 = Assignment(
            id = 2,
            name = "Assignment 2",
            published = false,
        )
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(assignment1, assignment2)
            ),
        )
        val groupItem1 = AssignmentGroupItemState(assignment1)
        val groupItem2 = AssignmentGroupItemState(assignment2)
        every { behavior.getAssignmentGroupItemState(assignment1) } returns groupItem1
        every { behavior.getAssignmentGroupItemState(assignment2) } returns groupItem2
        coEvery { repository.getAssignments(any(), any()) } returns DataResult.Success(assignmentGroups)
        val viewModel = getViewModel()

        var newFilter = AssignmentListFilterState(
            filterGroups = listOf(
                AssignmentListFilterGroup(
                    groupId = 0,
                    title = "Status",
                    options = listOf(
                        AssignmentListFilterOption.AllStatusAssignments(resources),
                        AssignmentListFilterOption.Published(resources),
                        AssignmentListFilterOption.Unpublished(resources),
                    ),
                    selectedOptionIndexes = listOf(0),
                    groupType = AssignmentListFilterGroupType.SingleChoice,
                    filterType = AssignmentListFilterType.Filter
                )
            )
        )
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(assignment1, assignment2), viewModel.uiState.value.listState.groups.first().items.map { it.assignment })

        newFilter = newFilter.copy(filterGroups = newFilter.filterGroups.map {
            it.copy(selectedOptionIndexes = listOf(1))
        })
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(assignment1), viewModel.uiState.value.listState.groups.first().items.map { it.assignment })

        newFilter = newFilter.copy(filterGroups = newFilter.filterGroups.map {
            it.copy(selectedOptionIndexes = listOf(2))
        })
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(assignment2), viewModel.uiState.value.listState.groups.first().items.map { it.assignment })
    }

    @Test
    fun `Test Needs grading and Not submitted Assignment filter`() = runTest {
        val assignment1 = Assignment(
            id = 1,
            name = "Assignment 1",
            unpublishable = false
        )
        val assignment2 = Assignment(
            id = 2,
            name = "Assignment 2",
            needsGradingCount = 2,
            unpublishable = true
        )
        val assignment3 = Assignment(
            id = 3,
            name = "Assignment 3",
            unpublishable = true
        )
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(assignment1, assignment2, assignment3)
            ),
        )
        val groupItem1 = AssignmentGroupItemState(assignment1)
        val groupItem2 = AssignmentGroupItemState(assignment2)
        val groupItem3 = AssignmentGroupItemState(assignment3)
        every { behavior.getAssignmentGroupItemState(assignment1) } returns groupItem1
        every { behavior.getAssignmentGroupItemState(assignment2) } returns groupItem2
        every { behavior.getAssignmentGroupItemState(assignment3) } returns groupItem3
        coEvery { repository.getAssignments(any(), any()) } returns DataResult.Success(assignmentGroups)
        val viewModel = getViewModel()

        var newFilter = AssignmentListFilterState(
            filterGroups = listOf(
                AssignmentListFilterGroup(
                    groupId = 0,
                    title = "Assignment Filter",
                    options = listOf(
                        AssignmentListFilterOption.AllFilterAssignments(resources),
                        AssignmentListFilterOption.NeedsGrading(resources),
                        AssignmentListFilterOption.NotSubmitted(resources),
                    ),
                    selectedOptionIndexes = listOf(0),
                    groupType = AssignmentListFilterGroupType.SingleChoice,
                    filterType = AssignmentListFilterType.Filter
                )
            )
        )
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(assignment1, assignment2, assignment3), viewModel.uiState.value.listState.groups.first().items.map { it.assignment })

        newFilter = newFilter.copy(filterGroups = newFilter.filterGroups.map {
            it.copy(selectedOptionIndexes = listOf(1))
        })
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(assignment2), viewModel.uiState.value.listState.groups.first().items.map { it.assignment })

        newFilter = newFilter.copy(filterGroups = newFilter.filterGroups.map {
            it.copy(selectedOptionIndexes = listOf(2))
        })
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(assignment2, assignment3), viewModel.uiState.value.listState.groups.first().items.map { it.assignment })
    }

    @Test
    fun `Test Grading Period filter`() = runTest {
        val assignment1 = Assignment(
            id = 1,
            name = "Assignment 1",
        )
        val assignment2 = Assignment(
            id = 2,
            name = "Assignment 2",
        )
        val assignment3 = Assignment(
            id = 3,
            name = "Assignment 3",
        )
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(assignment1, assignment2, assignment3)
            ),
        )
        val gradingPeriod1 = GradingPeriod(
            id = 1,
            title = "Grading Period 1",
        )
        val gradingPeriod2 = GradingPeriod(
            id = 2,
            title = "Grading Period 2",
        )
        val gradingPeriods = listOf(gradingPeriod1, gradingPeriod2)
        val groupItem1 = AssignmentGroupItemState(assignment1)
        val groupItem2 = AssignmentGroupItemState(assignment2)
        val groupItem3 = AssignmentGroupItemState(assignment3)
        every { behavior.getAssignmentGroupItemState(assignment1) } returns groupItem1
        every { behavior.getAssignmentGroupItemState(assignment2) } returns groupItem2
        every { behavior.getAssignmentGroupItemState(assignment3) } returns groupItem3
        coEvery { repository.getAssignments(any(), any()) } returns DataResult.Success(assignmentGroups)
        coEvery { repository.getGradingPeriodsForCourse(any(), any()) } returns DataResult.Success(gradingPeriods)
        coEvery { repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), 1, any()) } returns DataResult.Success(listOf(AssignmentGroup(id = 1, assignments = listOf(assignment1))))
        coEvery { repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), 2, any()) } returns DataResult.Success(listOf(AssignmentGroup(id = 2, assignments = listOf(assignment2))))
        val viewModel = getViewModel()

        val allGradingPeriod = AssignmentListFilterOption.GradingPeriod(null, resources)
        var newFilter = AssignmentListFilterState(
            filterGroups = listOf(
                AssignmentListFilterGroup(
                    groupId = 4,
                    title = "Grading periods",
                    options = listOf(allGradingPeriod) + gradingPeriods.map {
                        AssignmentListFilterOption.GradingPeriod(it, resources)
                    },
                    selectedOptionIndexes = listOf(0),
                    groupType = AssignmentListFilterGroupType.SingleChoice,
                    filterType = AssignmentListFilterType.Filter
                )
            )
        )
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(assignment1, assignment2, assignment3), viewModel.uiState.value.listState.groups.first().items.map { it.assignment })

        newFilter = newFilter.copy(filterGroups = newFilter.filterGroups.map {
            it.copy(selectedOptionIndexes = listOf(1))
        })
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(assignment1), viewModel.uiState.value.listState.groups.first().items.map { it.assignment })

        newFilter = newFilter.copy(filterGroups = newFilter.filterGroups.map {
            it.copy(selectedOptionIndexes = listOf(2))
        })
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(assignment2), viewModel.uiState.value.listState.groups.first().items.map { it.assignment })
    }

    @Test
    fun `Test Assignment status filter`() = runTest {
        val gradedAssignment = Assignment(
            id = 1,
            name = "Assignment 1",
            submission = Submission(grade = "A", workflowState = "graded", postedAt = Date())
        )
        val notGradedAssignment = Assignment(
            id = 2,
            name = "Assignment 2",
            submission = Submission(grade = null, workflowState = "submitted", postedAt = null)
        )
        val notSubmittedAssignment = Assignment(
            id = 3,
            name = "Assignment 3",
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString)
        )
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(gradedAssignment, notGradedAssignment, notSubmittedAssignment)
            ),
        )
        val groupItem1 = AssignmentGroupItemState(gradedAssignment)
        val groupItem2 = AssignmentGroupItemState(notGradedAssignment)
        val groupItem3 = AssignmentGroupItemState(notSubmittedAssignment)
        every { behavior.getAssignmentGroupItemState(gradedAssignment) } returns groupItem1
        every { behavior.getAssignmentGroupItemState(notGradedAssignment) } returns groupItem2
        every { behavior.getAssignmentGroupItemState(notSubmittedAssignment) } returns groupItem3
        coEvery { repository.getAssignments(any(), any()) } returns DataResult.Success(assignmentGroups)
        val viewModel = getViewModel()

        var newFilter = AssignmentListFilterState(
            filterGroups = listOf(
                AssignmentListFilterGroup(
                    groupId = 0,
                    title = "Assignment Filter",
                    options = listOf(
                        AssignmentListFilterOption.NotYetSubmitted(resources),
                        AssignmentListFilterOption.ToBeGraded(resources),
                        AssignmentListFilterOption.Graded(resources),
                        AssignmentListFilterOption.Other(resources),
                    ),
                    selectedOptionIndexes = (0..3).toList(),
                    groupType = AssignmentListFilterGroupType.MultiChoice,
                    filterType = AssignmentListFilterType.Filter
                )
            )
        )
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(notSubmittedAssignment, notGradedAssignment, gradedAssignment), viewModel.uiState.value.listState.groups.first().items.map { it.assignment })

        newFilter = newFilter.copy(filterGroups = newFilter.filterGroups.map {
            it.copy(selectedOptionIndexes = listOf(0))
        })
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(notSubmittedAssignment), viewModel.uiState.value.listState.groups.first().items.map { it.assignment })

        newFilter = newFilter.copy(filterGroups = newFilter.filterGroups.map {
            it.copy(selectedOptionIndexes = listOf(1))
        })
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(notGradedAssignment), viewModel.uiState.value.listState.groups.first().items.map { it.assignment })

        newFilter = newFilter.copy(filterGroups = newFilter.filterGroups.map {
            it.copy(selectedOptionIndexes = listOf(2))
        })
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(gradedAssignment), viewModel.uiState.value.listState.groups.first().items.map { it.assignment })
    }

    @Test
    fun `Test Grouping`() = runTest {
        val assignment1 = Assignment(
            id = 1,
            name = "Assignment 1",
            assignmentGroupId = 1,
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString)
        )
        val assignment2 = Assignment(
            id = 2,
            name = "Assignment 2",
            assignmentGroupId = 2,
            submissionTypesRaw = listOf(Assignment.SubmissionType.DISCUSSION_TOPIC.apiString),
            dueAt = (Calendar.getInstance().apply { add(Calendar.DATE, 2)}.time).toApiString()
        )
        val assignment3 = Assignment(
            id = 3,
            name = "Assignment 3",
            assignmentGroupId = 3,
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_QUIZ.apiString),
            dueAt = (Calendar.getInstance().apply { add(Calendar.DATE, -2)}.time).toApiString()
        )
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(assignment1)
            ),
            AssignmentGroup(
                id = 2,
                name = "Group 2",
                assignments = listOf(assignment2)
            ),
            AssignmentGroup(
                id = 3,
                name = "Group 3",
                assignments = listOf(assignment3)
            ),
        )
        val dueDateGroups = listOf(
            AssignmentGroup(
                id = 0,
                name = "Overdue Assignments",
                assignments = listOf(assignment3)
            ),
            AssignmentGroup(
                id = 1,
                name = "Upcoming Assignments",
                assignments = listOf(assignment2)
            ),
            AssignmentGroup(
                id = 2,
                name = "Undated Assignments",
                assignments = listOf(assignment1)
            ),
        )
        val typeGroups = listOf(
            AssignmentGroup(
                id = 0,
                name = "Assignments",
                assignments = listOf(assignment1)
            ),
            AssignmentGroup(
                id = 1,
                name = "Discussions",
                assignments = listOf(assignment2)
            ),
            AssignmentGroup(
                id = 2,
                name = "Quizzes",
                assignments = listOf(assignment3)
            ),
        )

        val groupItem1 = AssignmentGroupItemState(assignment1)
        val groupItem2 = AssignmentGroupItemState(assignment2)
        val groupItem3 = AssignmentGroupItemState(assignment3)
        every { behavior.getAssignmentGroupItemState(assignment1) } returns groupItem1
        every { behavior.getAssignmentGroupItemState(assignment2) } returns groupItem2
        every { behavior.getAssignmentGroupItemState(assignment3) } returns groupItem3
        coEvery { repository.getAssignments(any(), any()) } returns DataResult.Success(assignmentGroups)
        val viewModel = getViewModel()

        var newFilter = AssignmentListFilterState(
            filterGroups = listOf(
                AssignmentListFilterGroup(
                    groupId = 1,
                    title = "Group By",
                    options = listOf(
                        AssignmentListGroupByOption.AssignmentGroup(resources),
                        AssignmentListGroupByOption.DueDate(resources),
                        AssignmentListGroupByOption.AssignmentType(resources),
                    ),
                    selectedOptionIndexes = listOf(0),
                    groupType = AssignmentListFilterGroupType.SingleChoice,
                    filterType = AssignmentListFilterType.GroupBy
                )
            )
        )
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(assignmentGroups.map { it.assignments }, viewModel.uiState.value.listState.groups.map { it.items.map{ it.assignment } })

        newFilter = newFilter.copy(filterGroups = newFilter.filterGroups.map {
            it.copy(selectedOptionIndexes = listOf(1))
        })
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(dueDateGroups.map { it.assignments }, viewModel.uiState.value.listState.groups.map { it.items.map{ it.assignment } })

        newFilter = newFilter.copy(filterGroups = newFilter.filterGroups.map {
            it.copy(selectedOptionIndexes = listOf(2))
        })
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(typeGroups.map { it.assignments }, viewModel.uiState.value.listState.groups.map { it.items.map{ it.assignment } })
    }

    private fun getViewModel(): AssignmentListViewModel {
        return AssignmentListViewModel(savedStateHandle, apiPrefs, resources, repository, behavior)
    }
}