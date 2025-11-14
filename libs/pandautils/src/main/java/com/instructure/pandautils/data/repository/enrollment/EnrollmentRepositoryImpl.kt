package com.instructure.pandautils.data.repository.enrollment

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.DataResult
import javax.inject.Inject

class EnrollmentRepositoryImpl @Inject constructor(
    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface
) : EnrollmentRepository {

    override suspend fun getSelfEnrollments(
        types: List<String>?,
        states: List<String>?,
        forceRefresh: Boolean
    ): DataResult<List<Enrollment>> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return enrollmentApi.getFirstPageSelfEnrollments(types, states, params)
    }

    override suspend fun handleInvitation(
        courseId: Long,
        enrollmentId: Long,
        accept: Boolean
    ): DataResult<Unit> {
        val params = RestParams()
        val action = if (accept) "accept" else "reject"
        return enrollmentApi.handleInvite(courseId, enrollmentId, action, params)
    }
}