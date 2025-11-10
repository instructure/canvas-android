package com.instructure.horizon.features.notification

import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.platform.LoadingState
import java.util.Date

data class NotificationUiState(
    val screenState: LoadingState,
    val notificationItems: List<NotificationItem> = emptyList(),
)

data class NotificationItem(
    val category: NotificationItemCategory,
    val courseLabel: String?,
    val title: String,
    val date: Date?,
    val isRead: Boolean,
    val deepLink: String,
)

data class NotificationItemCategory(
    val label: String,
    val color: StatusChipColor,
)