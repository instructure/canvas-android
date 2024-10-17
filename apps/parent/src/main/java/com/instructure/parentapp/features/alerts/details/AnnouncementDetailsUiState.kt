package com.instructure.parentapp.features.alerts.details

import android.graphics.Color
import androidx.annotation.ColorInt
import com.instructure.canvasapi2.models.Attachment
import java.util.Date

data class AnnouncementDetailsUiState(
    val pageTitle: String? = null,
    val announcementTitle: String? = null,
    val postedDate: Date? = null,
    val message: String? = null,
    val attachment: Attachment? = null,
    @ColorInt val studentColor: Int = Color.BLACK,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isRefreshing: Boolean = false,
    val showErrorSnack: Boolean = false
)

sealed class AnnouncementDetailsAction {
    data object Refresh : AnnouncementDetailsAction()
    data class OpenAttachment(val attachment: Attachment) : AnnouncementDetailsAction()
    data object SnackbarDismissed :AnnouncementDetailsAction()
}
