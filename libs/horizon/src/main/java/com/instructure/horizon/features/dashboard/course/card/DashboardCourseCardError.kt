package com.instructure.horizon.features.dashboard.course.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonIconPosition
import com.instructure.horizon.horizonui.molecules.ButtonWidth

@Composable
fun DashboardCourseCardError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    DashboardCourseCard(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp, horizontal = 24.dp)
        ) {
            Text(
                text = "We weren’t able to load this content.",
                style = HorizonTypography.h4,
                color = HorizonColors.Text.title()
            )
            Text(
                text = "Please try again.",
                style = HorizonTypography.p2,
                color = HorizonColors.Text.timestamp()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                label = "Retry",
                height = ButtonHeight.SMALL,
                width = ButtonWidth.RELATIVE,
                color = ButtonColor.WhiteWithOutline,
                iconPosition = ButtonIconPosition.End(R.drawable.restart_alt),
                onClick = onRetry
            )
        }
    }
}