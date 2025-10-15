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
package com.instructure.pandautils.features.speedgrader

import com.instructure.canvasapi2.CustomGradeStatusesQuery
import com.instructure.canvasapi2.DifferentiationTagsQuery
import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.SectionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.managers.graphql.DifferentiationTagsManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.GradeableStudentSubmission
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.GroupAssignee
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.StudentAssignee
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.canvasapi2.utils.intersectBy
import java.util.Locale

class AssignmentSubmissionRepository(
    private val assignmentApi: AssignmentAPI.AssignmentInterface,
private val enrollmentApi: EnrollmentAPI.EnrollmentInterface,
    private val courseApi: CourseAPI.CoursesInterface,
    private val sectionApi: SectionAPI.SectionsInterface,
    private val customGradeStatusesManager: CustomGradeStatusesManager,
    private val differentiationTagsManager: DifferentiationTagsManager
) {

    suspend fun getGradeableStudentSubmissions(
        assignmentId: Long,
        courseId: Long,
        forceNetwork: Boolean
    ): List<GradeableStudentSubmission> {
        val assignment = assignmentApi.getAssignment(
            assignmentId = assignmentId,
            courseId = courseId,
            params = RestParams(isForceReadFromNetwork = forceNetwork)
        ).dataOrThrow
        return getGradeableStudentSubmissions(
            assignment = assignment,
            courseId = courseId,
            forceNetwork = forceNetwork
        )
    }

    suspend fun getAssignment(
        assignmentId: Long,
        courseId: Long,
        forceNetwork: Boolean
    ): Assignment {
        return assignmentApi.getAssignment(
            assignmentId = assignmentId,
            courseId = courseId,
            params = RestParams(isForceReadFromNetwork = forceNetwork)
        ).dataOrThrow
    }

    suspend fun getGradeableStudentSubmissions(
        assignment: Assignment,
        courseId: Long,
        forceNetwork: Boolean
    ): List<GradeableStudentSubmission> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)
        val gradeableStudents = assignmentApi.getFirstPageGradeableStudentsForAssignment(
            courseId,
            assignment.id,
            params
        ).depaginate {
            assignmentApi.getNextPageGradeableStudents(it, params)
        }.dataOrThrow

        val enrollments =
            enrollmentApi.getFirstPageEnrollmentsForCourse(courseId, null, params).depaginate {
                enrollmentApi.getNextPage(it, params)
            }.dataOrThrow

        val submissions =
            assignmentApi.getFirstPageSubmissionsForAssignment(courseId, assignment.id, params)
                .depaginate {
                    assignmentApi.getNextPageSubmissions(it, params)
                }.dataOrThrow

        val enrollmentMap = enrollments.associateBy { it.user?.id }
        val students = gradeableStudents.distinctBy { it.id }.map {
            var user = enrollmentMap[it.id]?.user
            user = user?.copy(
                enrollments = user.enrollments + enrollments.filter { enrollment -> enrollment.userId == user?.id },
                isFakeStudent = it.isFakeStudent
            )
            user?.enrollments?.forEach { it.user = null }
            user
        }.filterNotNull()

        val allSubmissions =
            if (assignment.groupCategoryId > 0 && !assignment.isGradeGroupsIndividually) {
                val groups = courseApi.getFirstPageGroups(assignment.courseId, params)
                    .depaginate { courseApi.getNextPageGroups(it, params) }.dataOrThrow
                    .filter { it.groupCategoryId == assignment.groupCategoryId }
                makeGroupSubmissions(students, groups, submissions)
            } else {
                val submissionMap = submissions.associateBy { it.userId }
                students.map {
                    GradeableStudentSubmission(StudentAssignee(it), submissionMap[it.id])
                }
            }.sortedBy {
                (it.assignee as? StudentAssignee)?.student?.sortableName?.lowercase(
                    Locale.getDefault()
                )
            }

        return allSubmissions
    }

    private fun makeGroupSubmissions(
        students: List<User>,
        groups: List<Group>,
        submissions: List<Submission>
    ): List<GradeableStudentSubmission> {
        val userMap = students.associateBy { it.id }
        val groupAssignees = groups.map { GroupAssignee(it, it.users.map { userMap[it.id] ?: it }) }

        val individualIds =
            students.map { it.id } - groupAssignees.flatMap { it.students.map { it.id } }
        val individualAssignees = individualIds
            .map { userMap[it] }
            .filterNotNull()
            .map { StudentAssignee(it) }

        val (groupedSubmissions, individualSubmissions) = submissions.partition {
            (it.group?.id ?: 0L) != 0L
        }
        val studentSubmissionMap = individualSubmissions.associateBy { it.userId }
        val groupSubmissionMap = groupedSubmissions
            .groupBy { it.group!!.id }
            .mapValues {
                it.value.reduce { acc, submission ->
                    acc.submissionComments =
                        acc.submissionComments.intersectBy(submission.submissionComments) {
                            "${it.authorId}|${it.comment}|${it.createdAt?.time}"
                        }
                    acc
                }
            }
            .toMap()

        val groupSubs =
            groupAssignees.map { GradeableStudentSubmission(it, groupSubmissionMap[it.group.id]) }
        val individualSubs = individualAssignees.map {
            GradeableStudentSubmission(
                it,
                studentSubmissionMap[it.student.id]
            )
        }

        return groupSubs + individualSubs
    }

    suspend fun getSections(courseId: Long, forceNetwork: Boolean): List<Section> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return sectionApi.getFirstPageSectionsList(courseId, params).depaginate {
            sectionApi.getNextPageSectionsList(it, params)
        }.dataOrNull ?: emptyList()
    }

    suspend fun getCustomGradeStatuses(
        courseId: Long,
        forceNetwork: Boolean
    ): List<CustomGradeStatusesQuery.Node> {
        return customGradeStatusesManager
            .getCustomGradeStatuses(courseId, forceNetwork)
            ?.course
            ?.customGradeStatusesConnection
            ?.nodes
            ?.filterNotNull()
            .orEmpty()
    }

    suspend fun getDifferentiationTags(
        courseId: Long,
        forceNetwork: Boolean
    ): List<Pair<DifferentiationTagsQuery.Group, String?>> {
        return differentiationTagsManager
            .getDifferentiationTags(courseId, forceNetwork)
            ?.course
            ?.groupSets
            ?.filter { it.nonCollaborative == true }
            ?.flatMap { groupSet ->
                val groups = groupSet.groups
                    ?.filter { group -> group.nonCollaborative == true }
                    .orEmpty()

                if (groups.isEmpty()) {
                    listOf(Pair(
                        DifferentiationTagsQuery.Group(
                            _id = groupSet._id,
                            name = groupSet.name,
                            nonCollaborative = false,
                            membersConnection = null
                        ),
                        null
                    ))
                } else {
                    groups.map { group -> Pair(group, groupSet.name) }
                }
            }
            .orEmpty()
    }
}
