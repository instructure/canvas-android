package com.instructure.pandautils.domain.usecase.enrollment

import com.instructure.pandautils.data.repository.enrollment.EnrollmentRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

data class HandleCourseInvitationParams(
    val courseId: Long,
    val enrollmentId: Long,
    val accept: Boolean
)

class HandleCourseInvitationUseCase @Inject constructor(
    private val enrollmentRepository: EnrollmentRepository
) : BaseUseCase<HandleCourseInvitationParams, Unit>() {

    override suspend fun execute(params: HandleCourseInvitationParams) {
        enrollmentRepository.handleInvitation(
            courseId = params.courseId,
            enrollmentId = params.enrollmentId,
            accept = params.accept
        ).dataOrThrow
    }
}