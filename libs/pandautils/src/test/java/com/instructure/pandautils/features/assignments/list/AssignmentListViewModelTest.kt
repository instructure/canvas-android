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
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.GroupedListViewEvent
import com.instructure.pandautils.features.assignments.list.filter.AssignmentFilter
import com.instructure.pandautils.features.assignments.list.filter.AssignmentGroupByOption
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterData
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterType
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListSelectedFilters
import com.instructure.pandautils.features.assignments.list.filter.AssignmentStatusFilterOption
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
        coEvery { repository.getAssignments(any(), any()) } returns assignmentGroups
        coEvery { repository.getCourse(any(), any()) } returns course
        coEvery { repository.getGradingPeriodsForCourse(any(), any()) } returns gradingPeriods
        coEvery { repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), any(), any()) } returns assignmentGroups
        coEvery { repository.getSelectedOptions(any(), any(), any()) } returns null
        coEvery { repository.updateSelectedOptions(any()) } just runs
        every { behavior.getAssignmentFilters() } returns AssignmentListFilterData(emptyList(), AssignmentListFilterType.SingleChoice)
        every { behavior.getAssignmentGroupItemState(any(), any(), any()) } returns mockk(relaxed = true)

        every { resources.getString(R.string.overdueAssignments) } returns "Overdue Assignments"
        every { resources.getString(R.string.upcomingAssignments) } returns "Upcoming Assignments"
        every { resources.getString(R.string.undatedAssignments) } returns "Undated Assignments"
        every { resources.getString(R.string.assignments) } returns "Assignments"
        every { resources.getString(R.string.discussion) } returns "Discussions"
        every { resources.getString(R.string.quizzes) } returns "Quizzes"
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Test empty state on Empty`() = runTest {
        coEvery { repository.getAssignments(any(), any()) } returns emptyList()
        val viewModel = getViewModel()
        assertEquals(ScreenState.Empty, viewModel.uiState.value.state)
    }

    @Test
    fun `Test List item click event handler`() = runTest {
        val assignment = Assignment(
            id = 1,
            name = "Assignment 1",
        )
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    assignment,
                )
            ),
        )
        val groupItem = AssignmentGroupItemState(course, assignmentGroups.first().assignments.first(), emptyList())
        every { behavior.getAssignmentGroupItemState(course, assignmentGroups.first().assignments.first(), emptyList()) } returns groupItem
        coEvery { repository.getAssignments(any(), any()) } returns assignmentGroups
        val viewModel = getViewModel()

        val events = mutableListOf<AssignmentListFragmentEvent>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleListEvent(GroupedListViewEvent.ItemClicked(groupItem))

        assertEquals(AssignmentListFragmentEvent.NavigateToAssignment(viewModel.uiState.value.course, assignment), events.last())
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
        val newFilters = AssignmentListSelectedFilters()

        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilters))

        assertEquals(newFilters, viewModel.uiState.value.selectedFilterData)

        coVerify(exactly = 1) { repository.updateSelectedOptions(any()) }
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
        val groupItem1 = AssignmentGroupItemState(course, assignment1, emptyList())
        val groupItem2 = AssignmentGroupItemState(course, assignment2, emptyList())
        every { behavior.getAssignmentGroupItemState(course, assignment1, emptyList()) } returns groupItem1
        every { behavior.getAssignmentGroupItemState(course, assignment2, emptyList()) } returns groupItem2
        coEvery { repository.getAssignments(any(), any()) } returns assignmentGroups
        val viewModel = getViewModel()

        var newFilter = AssignmentListSelectedFilters(selectedAssignmentStatusFilter = AssignmentStatusFilterOption.All)
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(assignment1, assignment2), viewModel.uiState.value.listState.values.first().map { it.assignment })

        newFilter = newFilter.copy(selectedAssignmentStatusFilter = AssignmentStatusFilterOption.Published)
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(assignment1), viewModel.uiState.value.listState.values.first().map { it.assignment })

        newFilter = newFilter.copy(selectedAssignmentStatusFilter = AssignmentStatusFilterOption.Unpublished)
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(assignment2), viewModel.uiState.value.listState.values.first().map { it.assignment })
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
        val groupItem1 = AssignmentGroupItemState(course, assignment1, emptyList())
        val groupItem2 = AssignmentGroupItemState(course, assignment2, emptyList())
        val groupItem3 = AssignmentGroupItemState(course, assignment3, emptyList())
        every { behavior.getAssignmentGroupItemState(course, assignment1, emptyList()) } returns groupItem1
        every { behavior.getAssignmentGroupItemState(course, assignment2, emptyList()) } returns groupItem2
        every { behavior.getAssignmentGroupItemState(course, assignment3, emptyList()) } returns groupItem3
        coEvery { repository.getAssignments(any(), any()) } returns assignmentGroups
        val viewModel = getViewModel()

        var newFilter = AssignmentListSelectedFilters(selectedAssignmentFilters = listOf(
            AssignmentFilter.All,
            AssignmentFilter.NeedsGrading,
            AssignmentFilter.NotSubmitted
        ))
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(assignment1, assignment2, assignment3), viewModel.uiState.value.listState.values.first().map { it.assignment })

        newFilter = newFilter.copy(selectedAssignmentFilters = listOf(AssignmentFilter.NeedsGrading))
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(assignment2), viewModel.uiState.value.listState.values.first().map { it.assignment })

        newFilter = newFilter.copy(selectedAssignmentFilters = listOf(AssignmentFilter.NotSubmitted))
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(assignment2, assignment3), viewModel.uiState.value.listState.values.first().map { it.assignment })
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
        val groupItem1 = AssignmentGroupItemState(course, assignment1, emptyList())
        val groupItem2 = AssignmentGroupItemState(course, assignment2, emptyList())
        val groupItem3 = AssignmentGroupItemState(course, assignment3, emptyList())
        every { behavior.getAssignmentGroupItemState(course, assignment1, emptyList()) } returns groupItem1
        every { behavior.getAssignmentGroupItemState(course, assignment2, emptyList()) } returns groupItem2
        every { behavior.getAssignmentGroupItemState(course, assignment3, emptyList()) } returns groupItem3
        coEvery { repository.getAssignments(any(), any()) } returns assignmentGroups
        coEvery { repository.getGradingPeriodsForCourse(any(), any()) } returns gradingPeriods
        coEvery { repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), 1, any()) } returns listOf(AssignmentGroup(id = 1, assignments = listOf(assignment1)))
        coEvery { repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), 2, any()) } returns listOf(AssignmentGroup(id = 2, assignments = listOf(assignment2)))
        val viewModel = getViewModel()

        var newFilter = AssignmentListSelectedFilters(selectedGradingPeriodFilter = null)
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(assignment1, assignment2, assignment3), viewModel.uiState.value.listState.values.first().map { it.assignment })

        newFilter = newFilter.copy(selectedGradingPeriodFilter = gradingPeriod1)
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(assignment1), viewModel.uiState.value.listState.values.first().map { it.assignment })

        newFilter = newFilter.copy(selectedGradingPeriodFilter = gradingPeriod2)
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(assignment2), viewModel.uiState.value.listState.values.first().map { it.assignment })
    }

    @Test
    fun `Test Assignment status filter`() = runTest {
        val gradedAssignment = Assignment(
            id = 1,
            assignmentGroupId = 1,
            name = "Assignment 1",
            submission = Submission(grade = "A", workflowState = "graded", submittedAt = Date(), postedAt = Date()),
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString)
        )
        val notGradedAssignment = Assignment(
            id = 2,
            name = "Assignment 2",
            assignmentGroupId = 1,
            submission = Submission(grade = null, workflowState = "submitted", submittedAt = Date(), postedAt = null),
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString)
        )
        val notSubmittedAssignment = Assignment(
            id = 3,
            name = "Assignment 3",
            assignmentGroupId = 1,
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString)
        )
        val customStatusAssignment = Assignment(
            id = 4,
            assignmentGroupId = 1,
            name = "Assignment 4",
            submission = Submission(customGradeStatusId = 1, submittedAt = Date(), postedAt = Date()),
            submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString)
        )
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(gradedAssignment, notGradedAssignment, notSubmittedAssignment, customStatusAssignment)
            ),
        )
        val groupItem1 = AssignmentGroupItemState(course, gradedAssignment, emptyList())
        val groupItem2 = AssignmentGroupItemState(course, notGradedAssignment, emptyList())
        val groupItem3 = AssignmentGroupItemState(course, notSubmittedAssignment, emptyList())
        val groupItem4 = AssignmentGroupItemState(course, customStatusAssignment, emptyList())
        every { behavior.getAssignmentGroupItemState(course, gradedAssignment, emptyList()) } returns groupItem1
        every { behavior.getAssignmentGroupItemState(course, notGradedAssignment, emptyList()) } returns groupItem2
        every { behavior.getAssignmentGroupItemState(course, notSubmittedAssignment, emptyList()) } returns groupItem3
        every { behavior.getAssignmentGroupItemState(course, customStatusAssignment, emptyList()) } returns groupItem4
        coEvery { repository.getAssignments(any(), any()) } returns assignmentGroups
        val viewModel = getViewModel()

        var newFilter = AssignmentListSelectedFilters(selectedAssignmentFilters = listOf(
            AssignmentFilter.NotYetSubmitted,
            AssignmentFilter.ToBeGraded,
            AssignmentFilter.Graded,
            AssignmentFilter.Other,
        ))
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(gradedAssignment, notGradedAssignment, notSubmittedAssignment, customStatusAssignment), viewModel.uiState.value.listState.values.first().map { it.assignment })

        newFilter = newFilter.copy(selectedAssignmentFilters = listOf(AssignmentFilter.NotYetSubmitted))
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(notSubmittedAssignment), viewModel.uiState.value.listState.values.first().map { it.assignment })

        newFilter = newFilter.copy(selectedAssignmentFilters = listOf(AssignmentFilter.ToBeGraded))
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(notGradedAssignment), viewModel.uiState.value.listState.values.first().map { it.assignment })

        newFilter = newFilter.copy(selectedAssignmentFilters = listOf(AssignmentFilter.Graded))
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(listOf(gradedAssignment, customStatusAssignment), viewModel.uiState.value.listState.values.first().map { it.assignment })
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

        val groupItem1 = AssignmentGroupItemState(course, assignment1, emptyList())
        val groupItem2 = AssignmentGroupItemState(course, assignment2, emptyList())
        val groupItem3 = AssignmentGroupItemState(course, assignment3, emptyList())
        every { behavior.getAssignmentGroupItemState(course, assignment1, emptyList()) } returns groupItem1
        every { behavior.getAssignmentGroupItemState(course, assignment2, emptyList()) } returns groupItem2
        every { behavior.getAssignmentGroupItemState(course, assignment3, emptyList()) } returns groupItem3
        coEvery { repository.getAssignments(any(), any()) } returns assignmentGroups
        val viewModel = getViewModel()

        var newFilter = AssignmentListSelectedFilters(selectedGroupByOption = AssignmentGroupByOption.AssignmentGroup)
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(assignmentGroups.map { it.assignments }, viewModel.uiState.value.listState.values.map { it.map { it.assignment } } )

        newFilter = newFilter.copy(selectedGroupByOption = AssignmentGroupByOption.DueDate)
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(dueDateGroups.map { it.assignments }, viewModel.uiState.value.listState.values.map { it.map { it.assignment } } )

        newFilter = newFilter.copy(selectedGroupByOption = AssignmentGroupByOption.AssignmentType)
        viewModel.handleAction(AssignmentListScreenEvent.UpdateFilterState(newFilter))
        assertEquals(typeGroups.map { it.assignments }, viewModel.uiState.value.listState.values.map { it.map{ it.assignment } } )
    }

    @Test
    fun `Test custom statuses getting fetched`() = runTest {
        every { savedStateHandle.get<Long>(Const.COURSE_ID) } returns 1

        getViewModel()

        coVerify(exactly = 1) { repository.getCustomGradeStatuses(1, false) }
    }

    private fun getViewModel(): AssignmentListViewModel {
        return AssignmentListViewModel(savedStateHandle, apiPrefs, resources, repository, behavior)
    }
}