package com.instructure.horizon.features.notification

import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.platform.LoadingState
import java.util.Date

data class NotificationUiState(
    val screenState: LoadingState,
    val notificationItems: List<NotificationItem> = emptyList(),
    val unreadCount: Int = 0,
)

data class NotificationItem(
    val category: NotificationItemCategory,
    val courseLabel: String?,
    val title: String,
    val date: Date?,
    val isRead: Boolean,
    val route: NotificationRoute,
)

data class NotificationItemCategory(
    val label: String,
    val color: StatusChipColor,
)

sealed class NotificationRoute {
    data class DeepLink(val deepLink: String): NotificationRoute()
    data class ExplicitRoute(val route: String): NotificationRoute()
}