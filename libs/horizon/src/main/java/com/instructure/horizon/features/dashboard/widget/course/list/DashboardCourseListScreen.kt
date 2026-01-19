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
package com.instructure.horizon.features.dashboard.widget.course.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardCard
import com.instructure.horizon.features.home.HomeNavigationRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.molecules.ProgressBarSmall
import com.instructure.horizon.horizonui.molecules.ProgressBarStyle
import com.instructure.horizon.horizonui.organisms.CollapsableScaffold
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelect
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectInputSize
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectState
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCourseListScreen(
    state: DashboardCourseListUiState,
    navController: NavHostController,
) {
    LoadingStateWrapper(state.loadingState) {
        CollapsableScaffold(
            containerColor = HorizonColors.Surface.pagePrimary(),
            topBar = { DashboardCourseListTopBar(navController) },
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
            ) {
                stickyHeader {
                    DashboardCourseListHeader(state)
                }
                if (state.courses.isEmpty()) {
                    item {
                        EmptyCoursesMessage()
                    }
                } else {
                    val visibleCourses = state.courses.take(state.visibleCourseCount)
                    items(visibleCourses) {
                        CourseItemCard(it, navController)
                    }

                    if (state.courses.size > state.visibleCourseCount) {
                        item {
                            Button(
                                label = stringResource(R.string.dashboardCourseListShowMore),
                                width = ButtonWidth.FILL,
                                color = ButtonColor.WhiteWithOutline,
                                onClick = state.onShowMoreCourses,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardCourseListHeader(state: DashboardCourseListUiState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(HorizonColors.Surface.pagePrimary())
            .padding(16.dp)
    ) {
        val context = LocalContext.current
        var isMenuOpen by remember { mutableStateOf(false) }

        SingleSelect(
            SingleSelectState(
                isMenuOpen = isMenuOpen,
                onMenuOpenChanged = { isMenuOpen = it },
                options = state.filterOptions.map { stringResource(it.labelRes) },
                selectedOption = stringResource(state.selectedFilterOption.labelRes),
                onOptionSelected = {
                    state.onFilterOptionSelected(DashboardCourseListFilterOption.fromLabel(context, it))
                },
                size = SingleSelectInputSize.Small,
                isFullWidth = false
            ),
            Modifier.width(IntrinsicSize.Max)
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = state.courses.size.toString(),
            style = HorizonTypography.p1,
            color = HorizonColors.Text.dataPoint(),
        )
    }
}

@Composable
private fun CourseItemCard(
    courseState: DashboardCourseListCourseState,
    navController: NavHostController
) {
    DashboardCard(
        modifier = Modifier.padding(horizontal = 16.dp),
        onClick = {
            navController.navigate(HomeNavigationRoute.Learn.route)
        }
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            if (courseState.parentPrograms.isNotEmpty()) {
                ProgramsText(courseState.parentPrograms) {
                    navController.navigate(HomeNavigationRoute.Learn.route)
                }
                HorizonSpace(SpaceSize.SPACE_16)
            }

            Text(
                courseState.name,
                style = HorizonTypography.labelLargeBold,
                color = HorizonColors.Text.title(),
            )

            HorizonSpace(SpaceSize.SPACE_12)

            CourseProgress(courseState.progress)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardCourseListTopBar(navController: NavHostController) {
    CenterAlignedTopAppBar(
       colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HorizonColors.Surface.pagePrimary(),
            titleContentColor = HorizonColors.Text.title(),
            navigationIconContentColor = HorizonColors.Icon.default()
        ),
        title = {
            Text(
                stringResource(R.string.dashboardCourseListTitle),
                style = HorizonTypography.h3,
                color = HorizonColors.Text.title()
            )
        },
        navigationIcon = {
            IconButton(
                iconRes = R.drawable.arrow_back,
                contentDescription = stringResource(R.string.a11yNavigateBack),
                color = IconButtonColor.Inverse,
                size = IconButtonSize.SMALL,
                elevation = HorizonElevation.level4,
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        },
    )
}

@Composable
private fun ProgramsText(
    programs: List<DashboardCourseListParentProgramState>,
    onProgramClicked: (String) -> Unit,
) {
    val programsAnnotated = buildAnnotatedString {
        programs.forEachIndexed { i, program ->
            if (i > 0) append(", ")
            withLink(
                LinkAnnotation.Clickable(
                    tag = program.programId,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = HorizonColors.Text.body(),
                            fontStyle = HorizonTypography.labelMediumBold.fontStyle,
                            textDecoration = TextDecoration.Underline,
                        )
                    ),
                    linkInteractionListener = { _ -> onProgramClicked(program.programId) }
                )
            ) {
                append(program.programName)
            }
        }
    }

    // String resource can't work with annotated string so we need a temporary placeholder
    val template = stringResource(R.string.learnScreen_partOfProgram, "__PROGRAMS__")

    val fullText = buildAnnotatedString {
        val parts = template.split("__PROGRAMS__")
        append(parts[0])
        append(programsAnnotated)
        if (parts.size > 1) append(parts[1])
    }

    Text(
        text = fullText,
        style = HorizonTypography.labelMediumBold,
        color = HorizonColors.Text.timestamp(),
        modifier = Modifier
            .semantics(mergeDescendants = true) {}
    )
}

@Composable
private fun CourseProgress(
    progress: Double,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProgressBarSmall(
            progress = progress,
            style = ProgressBarStyle.Institution,
            showLabels = false,
            modifier = Modifier.weight(1f)
        )

        HorizonSpace(SpaceSize.SPACE_8)

        Text(
            text = stringResource(R.string.progressBar_percent, progress.roundToInt()),
            style = HorizonTypography.p2,
            color = HorizonColors.Surface.institution(),
        )
    }
}

@Composable
private fun EmptyCoursesMessage() {
    Column {
        Text(
            stringResource(R.string.dashboardCourseListEmptyTitle),
            style = HorizonTypography.h2,
            color = HorizonColors.Text.body()
        )

        HorizonSpace(SpaceSize.SPACE_8)

        Text(
            stringResource(R.string.dashboardCourseListEmptyMessage),
            style = HorizonTypography.p1,
            color = HorizonColors.Text.body()
        )
    }
}