package com.instructure.pandautils.features.inbox.compose

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InboxComposeViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(InboxComposeUiState())
    val uiState = _uiState.asStateFlow()
}