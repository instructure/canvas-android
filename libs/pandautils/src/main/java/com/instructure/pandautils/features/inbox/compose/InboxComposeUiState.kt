package com.instructure.pandautils.features.inbox.compose

import com.instructure.canvasapi2.models.CanvasContext

data class InboxComposeUiState(
    var title: String = "New Message",
    var context: CanvasContext? = null,
    var recipients: List<String> = emptyList(),
    var course: String = "",
    var sendIndividual: Boolean = false,
    var subject: String = "",
    var body: String = "",
)