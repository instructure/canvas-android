/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.canvasapi2.managers.graphql

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.GradesAssignmentGroupsQuery
import com.instructure.canvasapi2.enqueueQuery
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.type.SubmissionState
import com.instructure.canvasapi2.type.SubmissionType
import com.instructure.canvasapi2.utils.ApiPrefs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

private const val QL_CLIENT_CONFIG_CLASS = "com.instructure.canvasapi2.QLClientConfigKt"

class GradesGraphQlManagerImplTest {

    private val apolloClient: ApolloClient = mockk()
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private lateinit var manager: GradesGraphQlManagerImpl

    @Before
    fun setup() {
        mockkStatic(QL_CLIENT_CONFIG_CLASS)
        every { apiPrefs.user } returns User(id = 465L)
        manager = GradesGraphQlManagerImpl(apolloClient, apiPrefs)
    }

    @After
    fun tearDown() {
        unmockkStatic(QL_CLIENT_CONFIG_CLASS)
    }

    @Test
    fun `excludes assignments whose submission belongs to a different grading period`() = runTest {
        // "Recognition" (49888) is returned by assignmentsConnection(gradingPeriodId=8) because its assignment
        // period is 8, but its graded submission actually lives in period 11 - web/REST scope by submission period
        // and drop it. Including it would inflate the period-8 total (the MBL-20202 bug).
        coEvery { apolloClient.enqueueQuery(any<GradesAssignmentGroupsQuery>(), any(), any()) } returns response(
            groups = listOf(groupNode(id = "4683", weight = 30.0)),
            assignments = listOf(
                assignmentNode(id = "43044", groupId = "4683", pointsPossible = 10.0, submission = submissionNode(id = "1", score = 2.0, grade = "2", gradingPeriodId = "8")),
                assignmentNode(id = "49888", groupId = "4683", pointsPossible = 4.0, submission = submissionNode(id = "2", score = 4.0, grade = "Leon", gradingPeriodId = "11"))
            )
        )

        val result = manager.getAssignmentGroupsWithAssignments(courseId = 2054L, gradingPeriodId = 8L, forceNetwork = true, paginateComments = false)

        val assignmentIds = result.flatMap { it.assignments }.map { it.id }
        assertTrue(43044L in assignmentIds)
        assertFalse("Assignment graded in period 11 must not count toward the period-8 grade", 49888L in assignmentIds)
    }

    @Test
    fun `keeps assignments without a submission grading period when a period is selected`() = runTest {
        // The server already scoped these to the requested period; a null submission grading period (e.g. no/placeholder
        // submission) is not positive evidence of another period, so keep it to match the REST assignment set.
        coEvery { apolloClient.enqueueQuery(any<GradesAssignmentGroupsQuery>(), any(), any()) } returns response(
            groups = listOf(groupNode(id = "4682", weight = 50.0)),
            assignments = listOf(
                assignmentNode(id = "50606", groupId = "4682", pointsPossible = 50.0, submission = submissionNode(id = "3", score = null, grade = null, gradingPeriodId = null))
            )
        )

        val result = manager.getAssignmentGroupsWithAssignments(courseId = 2054L, gradingPeriodId = 8L, forceNetwork = true, paginateComments = false)

        assertEquals(listOf(50606L), result.flatMap { it.assignments }.map { it.id })
    }

    @Test
    fun `does not filter by grading period when none is selected`() = runTest {
        coEvery { apolloClient.enqueueQuery(any<GradesAssignmentGroupsQuery>(), any(), any()) } returns response(
            groups = listOf(groupNode(id = "4683", weight = 30.0)),
            assignments = listOf(
                assignmentNode(id = "43044", groupId = "4683", pointsPossible = 10.0, submission = submissionNode(id = "1", score = 2.0, grade = "2", gradingPeriodId = "8")),
                assignmentNode(id = "49888", groupId = "4683", pointsPossible = 4.0, submission = submissionNode(id = "2", score = 4.0, grade = "Leon", gradingPeriodId = "11"))
            )
        )

        val result = manager.getAssignmentGroupsWithAssignments(courseId = 2054L, gradingPeriodId = null, forceNetwork = true, paginateComments = false)

        assertEquals(setOf(43044L, 49888L), result.flatMap { it.assignments }.map { it.id }.toSet())
    }

    private fun groupNode(id: String, weight: Double) = GradesAssignmentGroupsQuery.Node(
        _id = id,
        name = "Group $id",
        position = 1,
        groupWeight = weight,
        rules = null
    )

    private fun submissionNode(id: String, score: Double?, grade: String?, gradingPeriodId: String?) =
        GradesAssignmentGroupsQuery.Node4(
            _id = id,
            score = score,
            grade = grade,
            state = if (grade != null) SubmissionState.graded else SubmissionState.unsubmitted,
            submissionType = null,
            attempt = 1,
            late = false,
            missing = false,
            excused = false,
            submittedAt = null,
            postedAt = null,
            gradingPeriodId = gradingPeriodId,
            customGradeStatusId = null,
            unreadCommentCount = 0,
            commentsConnection = GradesAssignmentGroupsQuery.CommentsConnection(
                pageInfo = GradesAssignmentGroupsQuery.PageInfo2(hasNextPage = false, endCursor = null),
                nodes = emptyList()
            )
        )

    private fun assignmentNode(
        id: String,
        groupId: String,
        pointsPossible: Double,
        submission: GradesAssignmentGroupsQuery.Node4?
    ) = GradesAssignmentGroupsQuery.Node1(
        _id = id,
        name = "Assignment $id",
        position = 1,
        pointsPossible = pointsPossible,
        gradingType = null,
        submissionTypes = listOf(SubmissionType.online_text_entry),
        isNewQuiz = false,
        published = true,
        omitFromFinalGrade = false,
        assignmentGroupId = groupId,
        dueAt = null,
        lockInfo = null,
        checkpoints = emptyList(),
        discussion = null,
        mySubAssignmentSubmissionsConnection = null,
        assignmentOverrides = null,
        submissionsConnection = GradesAssignmentGroupsQuery.SubmissionsConnection(
            nodes = listOfNotNull(submission)
        )
    )

    private fun response(
        groups: List<GradesAssignmentGroupsQuery.Node>,
        assignments: List<GradesAssignmentGroupsQuery.Node1>
    ): ApolloResponse<GradesAssignmentGroupsQuery.Data> {
        val data = GradesAssignmentGroupsQuery.Data(
            course = GradesAssignmentGroupsQuery.Course(
                assignmentGroupsConnection = GradesAssignmentGroupsQuery.AssignmentGroupsConnection(
                    pageInfo = GradesAssignmentGroupsQuery.PageInfo(hasNextPage = false, endCursor = null),
                    nodes = groups
                ),
                assignmentsConnection = GradesAssignmentGroupsQuery.AssignmentsConnection(
                    pageInfo = GradesAssignmentGroupsQuery.PageInfo1(hasNextPage = false, endCursor = null),
                    nodes = assignments
                )
            )
        )
        return ApolloResponse.Builder(
            operation = GradesAssignmentGroupsQuery(
                courseId = "2054",
                userId = "465",
                gradingPeriodId = Optional.absent(),
                groupCursor = Optional.absent(),
                assignmentCursor = Optional.absent(),
                pageSize = 20
            ),
            requestUuid = UUID.randomUUID()
        ).data(data).build()
    }
}