package com.instructure.horizon.features.dashboard.widget

import com.instructure.horizon.horizonui.molecules.StatusChipColor
import java.util.Date

data class DashboardPaginatedWidgetCardState(
    val items: List<DashboardPaginatedWidgetCardItemState> = emptyList(),
    val isLoading: Boolean = false,
) {
    companion object {
        val Loading = DashboardPaginatedWidgetCardState(
            items = listOf(DashboardPaginatedWidgetCardItemState.Loading),
            isLoading = true
        )
    }
}

data class DashboardPaginatedWidgetCardItemState(
    val chipState: DashboardPaginatedWidgetCardChipState? = null,
    val pageState: String? = null,
    val source: String? = null,
    val date: Date? = null,
    val title: String? = null,
    val route: DashboardPaginatedWidgetCardButtonRoute? = null
) {
    companion object {
        val Loading = DashboardPaginatedWidgetCardItemState(
            chipState = DashboardPaginatedWidgetCardChipState(
                label = "Announcement",
                color = StatusChipColor.Sky
            ),
            title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Announcement title shown here.",
            source = "Institution or Course Name Here",
            date = Date(),
            route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
        )
    }
}

data class DashboardPaginatedWidgetCardChipState(
    val label: String,
    val color: StatusChipColor,
)

sealed class DashboardPaginatedWidgetCardButtonRoute {
    data class MainRoute(val route: String) : DashboardPaginatedWidgetCardButtonRoute()
    data class HomeRoute(val route: String) : DashboardPaginatedWidgetCardButtonRoute()
}