package com.instructure.horizon.features.dashboard.widget

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import java.util.Date

data class DashboardPaginatedWidgetCardState(
    val items: List<DashboardPaginatedWidgetCardItemState> = emptyList(),
    val isLoading: Boolean = false,
) {
    companion object {
        val Loading: DashboardPaginatedWidgetCardState
            get() = DashboardPaginatedWidgetCardState(
                items = listOf(
                    DashboardPaginatedWidgetCardItemState(
                        headerState = DashboardPaginatedWidgetCardHeaderState(
                            label = "Announcement",
                            color = HorizonColors.Surface.institution().copy(alpha = 0.1f),
                            iconRes = R.drawable.ic_announcement
                        ),
                        title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Announcement title shown here.",
                        source = "Institution or Course Name Here",
                        date = Date(),
                        route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
                    ),
                ),
                isLoading = true
            )
    }
}

data class DashboardPaginatedWidgetCardItemState(
    val headerState: DashboardPaginatedWidgetCardHeaderState,
    val source: String? = null,
    val date: Date? = null,
    val title: String? = null,
    val route: DashboardPaginatedWidgetCardButtonRoute? = null
)

data class DashboardPaginatedWidgetCardHeaderState(
    val label: String,
    val color: Color,
    @DrawableRes val iconRes: Int,
)

sealed class DashboardPaginatedWidgetCardButtonRoute {
    data class MainRoute(val route: String) : DashboardPaginatedWidgetCardButtonRoute()
    data class HomeRoute(val route: String) : DashboardPaginatedWidgetCardButtonRoute()
}