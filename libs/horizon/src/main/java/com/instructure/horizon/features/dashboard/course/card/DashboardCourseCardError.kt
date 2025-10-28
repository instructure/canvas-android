package com.instructure.horizon.features.dashboard.course.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardCard
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
    val context = LocalContext.current
    DashboardCard(
        modifier
            .semantics(mergeDescendants = true) {
                role = Role.Button
                onClick(context.getString(R.string.dashboardCourseCardRefreshLabel)) {
                    onRetry()
                    true
                }
                contentDescription =
                    context.getString(R.string.a11y_dashboardCoursesSectionTitle)
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp, horizontal = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.dashboardCourseCardErrorTitle),
                style = HorizonTypography.h4,
                color = HorizonColors.Text.title()
            )
            Text(
                text = stringResource(R.string.dashboardCourseCardErrorMessage),
                style = HorizonTypography.p2,
                color = HorizonColors.Text.timestamp()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                label = stringResource(R.string.dashboardCourseCardRefreshLabel),
                height = ButtonHeight.SMALL,
                width = ButtonWidth.RELATIVE,
                color = ButtonColor.WhiteWithOutline,
                iconPosition = ButtonIconPosition.End(R.drawable.restart_alt),
                onClick = onRetry,
                modifier = Modifier.clearAndSetSemantics { }
            )
        }
    }
}