package com.instructure.pandautils.domain.usecase.enrollment

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.data.repository.enrollment.EnrollmentRepository
import com.instructure.pandautils.domain.models.enrollment.CourseInvitation
import com.instructure.pandautils.domain.usecase.BaseUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

data class LoadCourseInvitationsParams(
    val forceRefresh: Boolean = false
)

class LoadCourseInvitationsUseCase @Inject constructor(
    private val enrollmentRepository: EnrollmentRepository,
    private val courseRepository: CourseRepository
) : BaseUseCase<LoadCourseInvitationsParams, List<CourseInvitation>>() {

    override suspend fun execute(params: LoadCourseInvitationsParams): List<CourseInvitation> {
        val enrollments = enrollmentRepository.getSelfEnrollments(
            types = null,
            states = listOf(EnrollmentAPI.STATE_INVITED),
            forceRefresh = params.forceRefresh
        ).dataOrThrow

        return coroutineScope {
            enrollments.map { enrollment ->
                async {
                    val course = courseRepository.getCourse(enrollment.courseId, params.forceRefresh).dataOrThrow
                    CourseInvitation(
                        enrollmentId = enrollment.id,
                        courseId = enrollment.courseId,
                        courseName = course.name,
                        userId = enrollment.userId
                    )
                }
            }.awaitAll()
        }
    }
}