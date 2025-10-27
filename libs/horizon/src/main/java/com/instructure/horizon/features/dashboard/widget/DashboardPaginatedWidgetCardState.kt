package com.instructure.horizon.features.dashboard.widget

import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import java.util.Date

data class DashboardPaginatedWidgetCardState(
    val items: List<DashboardPaginatedWidgetCardItemState> = emptyList(),
    val isLoading: Boolean = false,
)

data class DashboardPaginatedWidgetCardItemState(
    val chipState: DashboardPaginatedWidgetCardChipState? = null,
    val source: String? = null,
    val date: Date? = null,
    val title: String? = null,
    val buttonState: DashboardPaginatedWidgetCardButtonState? = null
)

data class DashboardPaginatedWidgetCardChipState(
    val label: String,
    val color: StatusChipColor,
)

data class DashboardPaginatedWidgetCardButtonState(
    val label: String,
    val height: ButtonHeight,
    val width: ButtonWidth,
    val color: ButtonColor,
    val route: DashboardPaginatedWidgetCardButtonRoute,
)

sealed class DashboardPaginatedWidgetCardButtonRoute {
    data class MainRoute(val route: String) : DashboardPaginatedWidgetCardButtonRoute()
    data class HomeRoute(val route: String) : DashboardPaginatedWidgetCardButtonRoute()
}