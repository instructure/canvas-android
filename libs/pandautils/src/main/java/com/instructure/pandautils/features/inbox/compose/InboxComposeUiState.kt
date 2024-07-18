package com.instructure.pandautils.features.inbox.compose

data class InboxComposeUiState(
    var title: String = "New Message",
    var course: String = "",
    var sendIndividual: Boolean = false,
    var subject: String = "",
    var body: String = "",
)