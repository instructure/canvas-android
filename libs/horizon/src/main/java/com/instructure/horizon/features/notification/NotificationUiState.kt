package com.instructure.horizon.features.notification

import com.instructure.horizon.horizonui.platform.LoadingState

data class NotificationUiState(
    val screenState: LoadingState,
    val allNotificationItems: List<NotificationItem> = emptyList(),
    val pagedNotificationItems: List<List<NotificationItem>> = emptyList(),
    val currentPageIndex: Int = 0,
    val decreasePageIndex: () -> Unit = {},
    val increasePageIndex: () -> Unit = {},
)

data class NotificationItem(
    val categoryLabel: String,
    val title: String,
    val date: String,
)