package com.instructure.parentapp.features.alerts.details

import android.graphics.Color
import androidx.annotation.ColorInt
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader

data class AnnouncementDetailsUiState(
    val announcement: DiscussionTopicHeader? = null,
    val course: Course? = null,
    @ColorInt val studentColor: Int = Color.BLACK,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isRefreshing: Boolean = false
)

sealed class AnnouncementDetailsAction {
    data object Refresh : AnnouncementDetailsAction()
}