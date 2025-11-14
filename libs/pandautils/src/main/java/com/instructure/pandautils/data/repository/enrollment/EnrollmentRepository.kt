package com.instructure.pandautils.data.repository.enrollment

import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.DataResult

interface EnrollmentRepository {
    suspend fun getSelfEnrollments(
        types: List<String>?,
        states: List<String>?,
        forceRefresh: Boolean
    ): DataResult<List<Enrollment>>

    suspend fun handleInvitation(
        courseId: Long,
        enrollmentId: Long,
        accept: Boolean
    ): DataResult<Unit>
}