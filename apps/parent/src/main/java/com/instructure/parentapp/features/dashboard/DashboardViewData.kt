package com.instructure.parentapp.features.dashboard

import com.instructure.canvasapi2.models.User


data class DashboardViewData(
    val userViewData: UserViewData? = null,
    val studentSelectorExpanded: Boolean = false,
    val studentItems: List<StudentItemViewModel> = emptyList(),
    val selectedStudent: User? = null
)

data class StudentItemViewData(
    val studentId: Long,
    val studentName: String,
    val avatarUrl: String
)

data class UserViewData(
    val name: String?,
    val pronouns: String?,
    val shortName: String?,
    val avatarUrl: String?,
    val email: String?
)
