package com.instructure.horizon.features.account.advanced

import com.instructure.horizon.horizonui.platform.LoadingState
import java.util.TimeZone

data class AccountAdvancedUiState(
    val screenState: LoadingState,
    val isButtonEnabled: Boolean = true,
    val timeZoneOptions: List<TimeZone> = emptyList(),
    val selectedTimeZone: TimeZone = TimeZone.getDefault(),
    val updateTimeZone: (TimeZone) -> Unit = {},
    val saveSelectedTimeZone: () -> Unit = {},
)
