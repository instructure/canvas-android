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
import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.GradesAssignmentGroupsQuery
import com.instructure.canvasapi2.GradesSubmissionCommentsQuery
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.enqueueQuery
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.AssignmentOverride
import com.instructure.canvasapi2.models.Checkpoint
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.ExternalToolAttributes
import com.instructure.canvasapi2.models.GradingRule
import com.instructure.canvasapi2.models.SubAssignmentSubmission
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.toApiString
import java.util.Date

class GradesGraphQlManagerImpl(
    private val apolloClient: ApolloClient,
    private val apiPrefs: ApiPrefs
) : GradesGraphQlManager {

    private data class GroupMeta(
        val id: Long,
        val name: String?,
        val position: Int,
        val groupWeight: Double,
        val rules: GradingRule
    )

    // The assignmentsConnection is always filtered by userId (the viewing student). When gradingPeriodId is
    // null we fetch every assignment — like the REST assignment_groups endpoint, including ones with no
    // grading period (e.g. future/undated). When a gradingPeriodId is given, assignments are scoped to that
    // period (the "filter by grading period" view).
    override suspend fun getAssignmentGroupsWithAssignments(
        courseId: Long,
        gradingPeriodId: Long?,
        forceNetwork: Boolean,
        paginateComments: Boolean
    ): List<AssignmentGroup> {
        val userId = apiPrefs.user?.id?.toString().orEmpty()
        val gradingPeriodFilter = Optional.presentIfNotNull(gradingPeriodId?.toString())

        val groupMetas = mutableListOf<GroupMeta>()
        val assignmentsByGroup = mutableMapOf<Long, MutableList<Assignment>>()

        // Groups and assignments are independent sibling connections, so depaginate them separately:
        // keep advancing each one's cursor until it runs out of pages. A finished connection is no
        // longer read (its next page would just be empty), so it contributes no duplicates.
        var groupCursor: Optional<String?> = Optional.absent()
        var assignmentCursor: Optional<String?> = Optional.absent()
        var hasMoreGroups = true
        var hasMoreAssignments = true
        while (hasMoreGroups || hasMoreAssignments) {
            val course = fetchAssignmentGroups(courseId, userId, gradingPeriodFilter, groupCursor, assignmentCursor, forceNetwork)

            if (hasMoreGroups) {
                val groups = course?.assignmentGroupsConnection
                groupMetas += groups?.nodes.orEmpty().filterNotNull().map(::mapGroupMeta)
                hasMoreGroups = groups?.pageInfo?.hasNextPage == true
                groupCursor = Optional.presentIfNotNull(groups?.pageInfo?.endCursor)
            }

            if (hasMoreAssignments) {
                val assignments = course?.assignmentsConnection
                assignments?.nodes.orEmpty().filterNotNull().forEach { node ->
                    val assignment = mapAssignment(node, paginateComments, forceNetwork)
                    assignmentsByGroup.getOrPut(assignment.assignmentGroupId) { mutableListOf() }.add(assignment)
                }
                hasMoreAssignments = assignments?.pageInfo?.hasNextPage == true
                assignmentCursor = Optional.presentIfNotNull(assignments?.pageInfo?.endCursor)
            }
        }

        return groupMetas.map { meta ->
            AssignmentGroup(
                id = meta.id,
                name = meta.name,
                position = meta.position,
                groupWeight = meta.groupWeight,
                rules = meta.rules,
                assignments = assignmentsByGroup[meta.id].orEmpty()
            )
        }
    }

    private suspend fun fetchAssignmentGroups(
        courseId: Long,
        userId: String,
        gradingPeriodId: Optional<String?>,
        groupCursor: Optional<String?>,
        assignmentCursor: Optional<String?>,
        forceNetwork: Boolean
    ): GradesAssignmentGroupsQuery.Course? {
        val query = GradesAssignmentGroupsQuery(
            courseId = courseId.toString(),
            userId = userId,
            gradingPeriodId = gradingPeriodId,
            groupCursor = groupCursor,
            assignmentCursor = assignmentCursor,
            pageSize = QLClientConfig.GRAPHQL_PAGE_SIZE
        )
        return apolloClient.enqueueQuery(query, forceNetwork).dataAssertNoErrors.course
    }

    private fun mapGroupMeta(group: GradesAssignmentGroupsQuery.Node) = GroupMeta(
        id = group._id.toLongOrNull() ?: 0L,
        name = group.name,
        position = group.position ?: 0,
        groupWeight = group.groupWeight ?: 0.0,
        rules = GradingRule(
            dropLowest = group.rules?.dropLowest ?: 0,
            dropHighest = group.rules?.dropHighest ?: 0,
            neverDrop = group.rules?.neverDrop?.mapNotNull { it._id.toLongOrNull() } ?: emptyList()
        )
    )

    private suspend fun mapAssignment(
        node: GradesAssignmentGroupsQuery.Node1,
        paginateComments: Boolean,
        forceNetwork: Boolean
    ): Assignment {
        val assignmentId = node._id.toLongOrNull() ?: 0L
        val subAssignmentSubmissions = mapSubAssignmentSubmissions(node)
        val overrides = mapOverrides(node, assignmentId)
        return Assignment(
            id = assignmentId,
            name = node.name,
            pointsPossible = node.pointsPossible ?: 0.0,
            position = node.position ?: 0,
            gradingType = node.gradingType?.rawValue,
            submissionTypesRaw = node.submissionTypes.orEmpty().map { it.rawValue },
            // GraphQL doesn't expose the external tool URL that ltiToolType()/getAssignmentIcon use to
            // detect New Quizzes, so synthesize one with the quiz-lti marker when isNewQuiz is set.
            externalToolAttributes = if (node.isNewQuiz) ExternalToolAttributes(url = NEW_QUIZ_LTI_URL) else null,
            dueAt = node.dueAt?.toApiString(),
            published = node.published ?: false,
            omitFromFinalGrade = node.omitFromFinalGrade ?: false,
            assignmentGroupId = node.assignmentGroupId?.toLongOrNull() ?: 0L,
            submission = mapSubmission(node, assignmentId, subAssignmentSubmissions, paginateComments, forceNetwork),
            lockedForUser = node.lockInfo?.isLocked ?: false,
            hasOverrides = overrides.isNotEmpty(),
            overrides = overrides,
            checkpoints = mapCheckpoints(node),
            discussionTopicHeader = node.discussion?.let {
                DiscussionTopicHeader(replyRequiredCount = it.replyToEntryRequiredCount)
            }
        )
    }

    // Checkpoint (sub-assignment) submissions live on their own connection and are present even when the parent
    // discussion has no submission yet (e.g. missing checkpoints), so use it as the source.
    private fun mapSubAssignmentSubmissions(node: GradesAssignmentGroupsQuery.Node1) = ArrayList(
        node.mySubAssignmentSubmissionsConnection?.nodes.orEmpty().filterNotNull().map { ss ->
            SubAssignmentSubmission(
                grade = ss.grade,
                score = ss.score ?: 0.0,
                late = ss.late ?: false,
                excused = ss.excused ?: false,
                missing = ss.missing ?: false,
                customGradeStatusId = ss.customGradeStatusId?.toLongOrNull(),
                subAssignmentTag = ss.subAssignmentTag,
                submittedAt = ss.submittedAt
            )
        }
    )

    private suspend fun mapSubmission(
        node: GradesAssignmentGroupsQuery.Node1,
        assignmentId: Long,
        subAssignmentSubmissions: ArrayList<SubAssignmentSubmission>,
        paginateComments: Boolean,
        forceNetwork: Boolean
    ): Submission? {
        val submissionNode = node.submissionsConnection?.nodes.orEmpty().filterNotNull().firstOrNull()
        return when {
            submissionNode != null -> Submission(
                id = submissionNode._id.toLongOrNull() ?: 0L,
                grade = submissionNode.grade,
                score = submissionNode.score ?: 0.0,
                attempt = submissionNode.attempt?.toLong() ?: 0L,
                submittedAt = submissionNode.submittedAt,
                workflowState = submissionNode.state.rawValue,
                submissionType = submissionNode.submissionType?.rawValue,
                late = submissionNode.late ?: false,
                excused = submissionNode.excused ?: false,
                missing = submissionNode.missing ?: false,
                assignmentId = assignmentId,
                postedAt = submissionNode.postedAt,
                gradingPeriodId = submissionNode.gradingPeriodId?.toLongOrNull(),
                customGradeStatusId = submissionNode.customGradeStatusId?.toLongOrNull(),
                unreadCommentCount = submissionNode.unreadCommentCount ?: 0,
                submissionComments = buildSubmissionComments(submissionNode, paginateComments, forceNetwork),
                subAssignmentSubmissions = subAssignmentSubmissions
            )
            // No parent submission, but carry the checkpoint submissions so their state still renders.
            subAssignmentSubmissions.isNotEmpty() -> Submission(
                assignmentId = assignmentId,
                subAssignmentSubmissions = subAssignmentSubmissions
            )
            else -> null
        }
    }

    // The main query fetches the first page of comments inline. When paginateComments is set (NGC, which
    // shows a comment pill) and the submission has more, fetch the remaining pages via the lightweight
    // GradesSubmissionCommentsQuery instead of re-running the whole assignment-groups query. In the
    // non-NGC case the comments aren't shown, so we keep just the first page and skip the extra calls.
    private suspend fun buildSubmissionComments(
        submissionNode: GradesAssignmentGroupsQuery.Node4,
        paginateComments: Boolean,
        forceNetwork: Boolean
    ): List<SubmissionComment> {
        val connection = submissionNode.commentsConnection
        val comments = connection?.nodes.orEmpty().filterNotNull().map {
            mapComment(it._id, it.attempt, it.author?._id, it.createdAt)
        }
        if (!paginateComments || connection?.pageInfo?.hasNextPage != true) return comments
        return comments + fetchRemainingComments(submissionNode._id, connection.pageInfo.endCursor, forceNetwork)
    }

    private suspend fun fetchRemainingComments(
        submissionId: String,
        startCursor: String?,
        forceNetwork: Boolean
    ): List<SubmissionComment> {
        val comments = mutableListOf<SubmissionComment>()
        var cursor = startCursor
        var hasNextPage = true
        while (hasNextPage && cursor != null) {
            val query = GradesSubmissionCommentsQuery(
                submissionId = submissionId,
                cursor = Optional.present(cursor),
                pageSize = QLClientConfig.GRAPHQL_PAGE_SIZE
            )
            val connection = apolloClient.enqueueQuery(query, forceNetwork).dataAssertNoErrors
                .submission
                ?.commentsConnection
            connection?.nodes.orEmpty().filterNotNull().forEach {
                comments += mapComment(it._id, it.attempt, it.author?._id, it.createdAt)
            }
            hasNextPage = connection?.pageInfo?.hasNextPage == true
            cursor = connection?.pageInfo?.endCursor
        }
        return comments
    }

    private fun mapComment(id: String?, attempt: Int?, authorId: String?, createdAt: Date?) = SubmissionComment(
        id = id?.toLongOrNull() ?: 0L,
        authorId = authorId?.toLongOrNull() ?: 0L,
        attempt = attempt?.toLong(),
        createdAt = createdAt
    )

    private fun mapOverrides(node: GradesAssignmentGroupsQuery.Node1, assignmentId: Long) =
        node.assignmentOverrides?.nodes.orEmpty().filterNotNull().map { o ->
            AssignmentOverride(
                id = o._id?.toLongOrNull() ?: 0L,
                assignmentId = assignmentId,
                dueAt = o.dueAt,
                unlockAt = o.unlockAt,
                lockAt = o.lockAt,
                courseSectionId = o.set?.onSection?._id?.toLongOrNull() ?: 0L
            )
        }

    private fun mapCheckpoints(node: GradesAssignmentGroupsQuery.Node1) =
        node.checkpoints.orEmpty().map { c ->
            Checkpoint(
                name = c.name,
                tag = c.tag,
                pointsPossible = c.pointsPossible,
                dueAt = c.dueAt?.toApiString()
            )
        }

    companion object {
        // Contains LtiType.NEW_QUIZZES_LTI.id ("quiz-lti") so ltiToolType() resolves to the quiz icon.
        private const val NEW_QUIZ_LTI_URL = "quiz-lti"
    }
}
