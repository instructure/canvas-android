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
 *
 */
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.GradeableStudent
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.ObserveeAssignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.postmodels.AssignmentPostBody
import com.instructure.canvasapi2.models.postmodels.AssignmentPostBodyWrapper
import com.instructure.canvasapi2.models.postmodels.QuizAssignmentPostBody
import com.instructure.canvasapi2.models.postmodels.QuizAssignmentPostBodyWrapper
import com.instructure.canvasapi2.utils.ExhaustiveListCallback
import com.instructure.canvasapi2.utils.weave.apiAsync

object AssignmentManager {

    fun getAssignment(assignmentId: Long, courseId: Long, forceNetwork: Boolean, callback: StatusCallback<Assignment>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        AssignmentAPI.getAssignment(courseId, assignmentId, adapter, callback, params)
    }

    fun getAssignmentIncludeObservees(assignmentId: Long, courseId: Long, forceNetwork: Boolean, callback: StatusCallback<ObserveeAssignment>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        AssignmentAPI.getAssignmentIncludeObservees(courseId, assignmentId, adapter, callback, params)
    }

    fun getAssignmentIncludeObserveesAsync(assignmentId: Long, courseId: Long, forceNetwork: Boolean) = apiAsync<ObserveeAssignment> {
        getAssignmentIncludeObservees(assignmentId, courseId, forceNetwork, it)
    }

    fun getAssignmentAsync(assignmentId: Long, courseId: Long, forceNetwork: Boolean)
            = apiAsync<Assignment> { getAssignment(assignmentId, courseId, forceNetwork, it) }

    fun getAssignmentWithHistoryAsync(assignmentId: Long, courseId: Long, forceNetwork: Boolean) = apiAsync<Assignment> {
        val adapter = RestBuilder(it)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        AssignmentAPI.getAssignmentWithHistory(courseId, assignmentId, adapter, it, params)
    }

    fun getExternalToolLaunchUrlAsync(courseId: Long, externalToolId: Long, assignmentId: Long) = apiAsync<LTITool> {
        AssignmentAPI.getExternalToolLaunchUrl(
            courseId,
            externalToolId,
            assignmentId,
            RestBuilder(it),
            it,
            RestParams(isForceReadFromNetwork = true)
        )
    }

    fun getAssignmentGroupsWithAssignments(
        courseId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<List<AssignmentGroup>>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val depaginatedCallback = object : ExhaustiveListCallback<AssignmentGroup>(callback) {
            override fun getNextPage(
                callback: StatusCallback<List<AssignmentGroup>>,
                nextUrl: String,
                isCached: Boolean
            ) {
                AssignmentAPI.getNextPageAssignmentGroupsWithAssignments(forceNetwork, nextUrl, adapter, callback)
            }
        }

        adapter.statusCallback = depaginatedCallback
        AssignmentAPI.getFirstPageAssignmentGroupsWithAssignments(courseId, adapter, depaginatedCallback, params)
    }

    fun getAssignmentGroupsWithAssignmentsForGradingPeriod(
        courseId: Long,
        gradingPeriodId: Long,
        scopeToStudent: Boolean,
        forceNetwork: Boolean,
        callback: StatusCallback<List<AssignmentGroup>>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val depaginatedCallback = object : ExhaustiveListCallback<AssignmentGroup>(callback) {
            override fun getNextPage(
                callback: StatusCallback<List<AssignmentGroup>>,
                nextUrl: String,
                isCached: Boolean
            ) {
                AssignmentAPI.getNextPageAssignmentGroupsWithAssignmentsForGradingPeriod(
                    forceNetwork,
                    nextUrl,
                    adapter,
                    callback
                )
            }
        }

        adapter.statusCallback = depaginatedCallback
        AssignmentAPI.getFirstPageAssignmentGroupsWithAssignmentsForGradingPeriod(
            courseId,
            gradingPeriodId,
            scopeToStudent,
            adapter,
            depaginatedCallback,
            params
        )
    }

    fun getAssignmentGroupsWithAssignmentsForGradingPeriod(
        courseId: Long,
        gradingPeriodId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<List<AssignmentGroup>>
    ) {
        getAssignmentGroupsWithAssignmentsForGradingPeriod(courseId, gradingPeriodId, false, forceNetwork, callback)
    }

    fun getAssignmentGroup(
        courseId: Long,
        assignmentGroupId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<AssignmentGroup>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        AssignmentAPI.getAssignmentGroup(courseId, assignmentGroupId, adapter, callback, params)
    }

    fun editAssignment(
        courseId: Long,
        assignmentId: Long,
        body: AssignmentPostBody,
        callback: StatusCallback<Assignment>,
        serializeNulls: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()

        val bodyWrapper = AssignmentPostBodyWrapper()
        bodyWrapper.assignment = body
        AssignmentAPI.editAssignment(courseId, assignmentId, bodyWrapper, adapter, callback, params, serializeNulls)
    }

    fun editQuizAssignment(
        courseId: Long,
        assignmentId: Long,
        body: QuizAssignmentPostBody,
        callback: StatusCallback<Assignment>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()

        val bodyWrapper = QuizAssignmentPostBodyWrapper()
        bodyWrapper.assignment = body
        AssignmentAPI.editQuizAssignment(courseId, assignmentId, bodyWrapper, adapter, callback, params)
    }

    fun getAllGradeableStudentsForAssignment(
        courseId: Long,
        assignmentId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<List<GradeableStudent>>
    ) {
        val adapter = RestBuilder(callback)
        val depaginatedCallback = object : ExhaustiveListCallback<GradeableStudent>(callback) {
            override fun getNextPage(
                callback: StatusCallback<List<GradeableStudent>>,
                nextUrl: String,
                isCached: Boolean
            ) {
                AssignmentAPI.getNextPageGradeableStudents(forceNetwork, nextUrl, adapter, callback)
            }
        }
        adapter.statusCallback = depaginatedCallback
        AssignmentAPI.getFirstPageGradeableStudentsForAssignment(courseId, assignmentId, adapter, depaginatedCallback)
    }

    fun getAllSubmissionsForAssignment(
        courseId: Long,
        assignmentId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<List<Submission>>
    ) {
        val adapter = RestBuilder(callback)
        val depaginatedCallback = object : ExhaustiveListCallback<Submission>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Submission>>, nextUrl: String, isCached: Boolean) {
                AssignmentAPI.getNextPageSubmissions(nextUrl, adapter, forceNetwork, callback)
            }
        }
        adapter.statusCallback = depaginatedCallback
        AssignmentAPI.getFirstPageSubmissionsForAssignment(
            courseId,
            assignmentId,
            forceNetwork,
            adapter,
            depaginatedCallback
        )
    }

    fun getAllSubmissionsForAssignmentAsync(courseId: Long, assignmentId: Long, forceNetwork: Boolean) = apiAsync<List<Submission>> {
        getAllSubmissionsForAssignment(courseId, assignmentId, forceNetwork, it)
    }

    fun getAllAssignments(courseId: Long, forceNetwork: Boolean, callback: StatusCallback<List<Assignment>>) {
        val adapter = RestBuilder(callback)
        val depaginatedCallback = object : ExhaustiveListCallback<Assignment>(callback) {
            override fun getNextPage(callback: StatusCallback<List<Assignment>>, nextUrl: String, isCached: Boolean) {
                AssignmentAPI.getNextPageAssignments(nextUrl, adapter, forceNetwork, callback)
            }
        }
        adapter.statusCallback = depaginatedCallback
        AssignmentAPI.getFirstPageAssignments(courseId, forceNetwork, adapter, depaginatedCallback)
    }

    fun getAllAssignmentsAsync(courseId: Long, forceNetwork: Boolean) = apiAsync<List<Assignment>> { getAllAssignments(courseId, forceNetwork, it) }

}
