/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
import com.instructure.canvasapi2.enqueueMutation
import com.instructure.canvasapi2.enqueueQuery
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.journey.EnrollCourseMutation
import com.instructure.journey.EnrolledProgramsQuery
import com.instructure.journey.GetProgramByIdQuery
import com.instructure.journey.fragment.ProgramFields
import com.instructure.journey.type.ProgramProgressCourseEnrollmentStatus
import com.instructure.journey.type.ProgramVariantType
import java.util.Date
import javax.inject.Inject

data class Program(
    val id: String,
    val name: String,
    val description: String?,
    val startDate: Date?,
    val endDate: Date?,
    val variant: ProgramVariantType,
    val courseCompletionCount: Int? = null,
    val sortedRequirements: List<ProgramRequirement>,
)

data class ProgramRequirement(
    val id: String,
    val progressId: String,
    val courseId: Long,
    val required: Boolean,
    val progress: Double = 0.0,
    val enrollmentStatus: ProgramProgressCourseEnrollmentStatus? = null
)

interface JourneyApiManager {
    suspend fun getPrograms(forceNetwork: Boolean = false): List<Program>
    suspend fun getProgramById(programId: String, forceNetwork: Boolean = false): Program
    suspend fun enrollCourse(progressId: String): DataResult<Unit>
}

class JourneyApiManagerImpl @Inject constructor(
    private val journeyClient: ApolloClient
): JourneyApiManager {
    override suspend fun getPrograms(forceNetwork: Boolean): List<Program> {
        val query = EnrolledProgramsQuery()
        val result = journeyClient.enqueueQuery(query, forceNetwork = forceNetwork)
        return result.dataAssertNoErrors.enrolledPrograms.map {
            mapEnrolledProgram(it.programFields)
        }
    }

    override suspend fun getProgramById(programId: String, forceNetwork: Boolean): Program {
        val query = GetProgramByIdQuery(programId)
        val result = journeyClient.enqueueQuery(query, forceNetwork = forceNetwork)
        return mapEnrolledProgram(result.dataAssertNoErrors.program.programFields)
    }

    private fun mapEnrolledProgram(enrolledProgram: ProgramFields): Program {

        val sortedRequirements = sortRequirementsByDependency(enrolledProgram.requirements).map {
            mapRequirement(it, enrolledProgram.progresses)
        }

        return Program(
            id = enrolledProgram.id,
            name = enrolledProgram.name,
            description = enrolledProgram.description,
            startDate = enrolledProgram.startDate,
            endDate = enrolledProgram.endDate,
            variant = enrolledProgram.variant,
            courseCompletionCount = enrolledProgram.courseCompletionCount,
            sortedRequirements = sortedRequirements
        )
    }

    private fun sortRequirementsByDependency(requirements: List<ProgramFields.Requirement1>): List<ProgramFields.Requirement1> {
        if (requirements.isEmpty()) return emptyList()

        val dependencyMap = mutableMapOf<String, ProgramFields.Requirement1>()
        var startRequirement: ProgramFields.Requirement1? = null

        for (requirement in requirements) {
            if (requirement.dependency == null) {
                startRequirement = requirement
            }
            dependencyMap[requirement.dependent.id] = requirement
        }

        if (startRequirement == null) return emptyList()

        val sortedRequirements = mutableListOf(startRequirement)
        var currentReq = startRequirement

        while (currentReq?.dependent != null) {
            val nextDepId = currentReq.dependent.id
            val nextReq = requirements.find { it.dependency?.id == nextDepId }
            if (nextReq == null) break
            sortedRequirements.add(nextReq)
            currentReq = nextReq
        }

        return sortedRequirements
    }

    private fun mapRequirement(requirement: ProgramFields.Requirement1, progresses: List<ProgramFields.Progress>): ProgramRequirement {
        val progress = progresses.find { it.requirement.id == requirement.id }
        return ProgramRequirement(
            id = requirement.id,
            progressId = progress?.id ?: "",
            courseId = requirement.dependent.canvasCourseId.toLongOrNull() ?: -1L,
            required = requirement.isCompletionRequired,
            enrollmentStatus = progress?.courseEnrollmentStatus,
            progress = progress?.completionPercentage ?: 0.0
        )
    }

    override suspend fun enrollCourse(progressId: String): DataResult<Unit> {
        val mutation = EnrollCourseMutation(progressId)
        val result = journeyClient.enqueueMutation(mutation)
        return if (result.exception != null) {
            DataResult.Fail(Failure.Exception(result.exception!!))
        } else {
            DataResult.Success(Unit)
        }
    }
}