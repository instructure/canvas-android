/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.dashboard.widget.skillhighlights.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetCard
import com.instructure.horizon.features.home.HomeNavigationRoute
import com.instructure.horizon.horizonui.animation.shimmerEffect
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Pill
import com.instructure.horizon.horizonui.molecules.PillCase
import com.instructure.horizon.horizonui.molecules.PillSize
import com.instructure.horizon.horizonui.molecules.PillStyle
import com.instructure.horizon.horizonui.molecules.PillType

@Composable
fun DashboardSkillHighlightsCardContent(
    state: DashboardSkillHighlightsCardState,
    homeNavController: NavHostController,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    DashboardWidgetCard(
        title = stringResource(R.string.dashboardSkillHighlightsTitle),
        iconRes = R.drawable.hub,
        widgetColor = HorizonColors.PrimitivesGreen.green12(),
        isLoading = isLoading,
        useMinWidth = false,
        modifier = modifier
    ) {
        if (state.skills.isEmpty()) {
            Column {
                HorizonSpace(SpaceSize.SPACE_8)
                Text(
                    text = stringResource(R.string.dashboardSkillHighlightsNoDataTitle),
                    style = HorizonTypography.h4,
                    color = HorizonColors.Text.title(),
                    modifier = Modifier.shimmerEffect(isLoading)
                )
                HorizonSpace(SpaceSize.SPACE_4)
                Text(
                    text = stringResource(R.string.dashboardSkillHighlightsNoDataMessage),
                    style = HorizonTypography.p2,
                    color = HorizonColors.Text.timestamp(),
                    modifier = Modifier.shimmerEffect(isLoading)
                )
            }
        } else {
            HorizonSpace(SpaceSize.SPACE_8)
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                state.skills.forEach { skill ->
                    SkillCard(
                        skill,
                        skill.proficiencyLevel.opacity(),
                        homeNavController,
                        modifier = Modifier.shimmerEffect(
                            isLoading,
                            backgroundColor = HorizonColors.PrimitivesGreen.green12().copy(alpha = 0.8f),
                            shimmerColor = HorizonColors.PrimitivesGreen.green12().copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillCard(
    skill: SkillHighlight,
    opacity: Float,
    homeNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(HorizonColors.PrimitivesGreen.green12().copy(alpha = opacity))
            .fillMaxWidth()
            .clickable {
                homeNavController.navigate(HomeNavigationRoute.Skillspace.route) {
                    popUpTo(homeNavController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            .padding(24.dp)
    ) {
        Text(
            text = skill.name,
            style = HorizonTypography.p2,
            color = HorizonColors.Text.body(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Pill(
            label = stringResource(skill.proficiencyLevel.skillProficiencyLevelRes),
            style = PillStyle.SOLID,
            type = PillType.INVERSE,
            case = PillCase.TITLE,
            size = PillSize.SMALL
        )
    }
}

private fun SkillHighlightProficiencyLevel.opacity(): Float {
    return when (this) {
        SkillHighlightProficiencyLevel.BEGINNER -> 0.4f
        SkillHighlightProficiencyLevel.PROFICIENT -> 0.6f
        SkillHighlightProficiencyLevel.ADVANCED -> 0.8f
        SkillHighlightProficiencyLevel.EXPERT -> 1f
    }
}

@Composable
@Preview
private fun DashboardSkillHighlightsCardContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    DashboardSkillHighlightsCardContent(
        state = DashboardSkillHighlightsCardState(
            skills = listOf(
                SkillHighlight("Dolor sit amet adipiscing elit do long skill name", SkillHighlightProficiencyLevel.ADVANCED),
                SkillHighlight("Dolor sit skill name", SkillHighlightProficiencyLevel.BEGINNER),
                SkillHighlight("Adipiscing elit skill name", SkillHighlightProficiencyLevel.PROFICIENT)
            )
        ),
        rememberNavController()
    )
}

@Composable
@Preview
private fun DashboardSkillHighlightsCardContentNoDataPreview() {
    ContextKeeper.appContext = LocalContext.current
    DashboardSkillHighlightsCardContent(
        state = DashboardSkillHighlightsCardState(skills = emptyList()),
        rememberNavController()
    )
}

@Composable
@Preview
private fun DashboardSkillHighlightsLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    DashboardSkillHighlightsCardContent(
        state = DashboardSkillHighlightsCardState(skills = emptyList()),
        rememberNavController(),
        isLoading = true
    )
}
