package com.instructure.horizon.features.inbox

import kotlinx.serialization.Serializable

@Serializable
sealed class HorizonInboxItemType {
    data object Inbox: HorizonInboxItemType()
    data object AccountNotification: HorizonInboxItemType()
    data class CourseNotification(val courseId: String): HorizonInboxItemType()
}