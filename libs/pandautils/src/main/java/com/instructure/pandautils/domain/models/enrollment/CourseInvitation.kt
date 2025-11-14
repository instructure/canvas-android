package com.instructure.pandautils.domain.models.enrollment

data class CourseInvitation(
    val enrollmentId: Long,
    val courseId: Long,
    val courseName: String,
    val userId: Long
)