package com.instructure.horizon.features.notification

import com.instructure.horizon.horizonui.platform.LoadingState

data class NotificationUiState(
    val screenState: LoadingState,
    val notificationItems: List<NotificationItem> = emptyList(),
)

data class NotificationItem(
    val categoryLabel: String,
    val title: String,
    val date: String,
)