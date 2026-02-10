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
        val Loading = DashboardPaginatedWidgetCardState(
            items = listOf(DashboardPaginatedWidgetCardItemState.Loading),
            isLoading = true
        )
    }
}

data class DashboardPaginatedWidgetCardItemState(
    val headerState: DashboardPaginatedWidgetCardHeaderState,
    val source: String? = null,
    val date: Date? = null,
    val title: String? = null,
    val route: String? = null
) {
    companion object {
        val Loading = DashboardPaginatedWidgetCardItemState(
            headerState = DashboardPaginatedWidgetCardHeaderState(
                label = "Announcement",
                color = HorizonColors.PrimitivesSky.sky12,
                iconRes = R.drawable.campaign
            ),
            title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Announcement title shown here.",
            source = "Institution or Course Name Here",
            date = Date(),
            route = null
        )
    }
}

data class DashboardPaginatedWidgetCardHeaderState(
    val label: String,
    val color: Color,
    @DrawableRes val iconRes: Int,
)