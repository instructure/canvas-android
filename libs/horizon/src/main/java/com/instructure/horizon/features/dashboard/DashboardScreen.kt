/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)

package com.instructure.horizon.features.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.home.HomeNavigationRoute
import com.instructure.horizon.features.moduleitemsequence.SHOULD_REFRESH_DASHBOARD
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Badge
import com.instructure.horizon.horizonui.molecules.BadgeContent
import com.instructure.horizon.horizonui.molecules.BadgeType
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.LoadingButton
import com.instructure.horizon.horizonui.molecules.ProgressBar
import com.instructure.horizon.horizonui.organisms.Alert
import com.instructure.horizon.horizonui.organisms.AlertType
import com.instructure.horizon.horizonui.organisms.cards.LearningObjectCard
import com.instructure.horizon.horizonui.organisms.cards.LearningObjectCardState
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.horizon.navigation.MainNavigationRoute
import com.instructure.pandautils.utils.ThemePrefs

@Composable
fun DashboardScreen(uiState: DashboardUiState, mainNavController: NavHostController, homeNavController: NavHostController) {

    val parentEntry = remember(mainNavController.currentBackStackEntry) { mainNavController.getBackStackEntry("home") }
    val savedStateHandle = parentEntry.savedStateHandle

    val refreshFlow = remember { savedStateHandle.getStateFlow(SHOULD_REFRESH_DASHBOARD, false) }

    val shouldRefresh by refreshFlow.collectAsState()

    NotificationPermissionRequest()

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            uiState.loadingState.onRefresh()
            savedStateHandle[SHOULD_REFRESH_DASHBOARD] = false
        }
    }

    Scaffold(containerColor = HorizonColors.Surface.pagePrimary()) { paddingValues ->
        val spinnerColor =
            if (ThemePrefs.isThemeApplied) HorizonColors.Surface.institution() else HorizonColors.Surface.inverseSecondary()
        LoadingStateWrapper(loadingState = uiState.loadingState, spinnerColor = spinnerColor, modifier = Modifier.padding(paddingValues)) {
            if (uiState.coursesUiState.isEmpty() && uiState.invitesUiState.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState())
                        .height(IntrinsicSize.Max)
                ) {
                    HomeScreenTopBar(uiState, mainNavController, modifier = Modifier.height(56.dp))
                    HorizonSpace(SpaceSize.SPACE_24)
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            stringResource(R.string.dashboard_emptyMessage),
                            style = HorizonTypography.h3,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp),
                    content = {
                        item {
                            HomeScreenTopBar(uiState, mainNavController, modifier = Modifier.height(56.dp))
                            HorizonSpace(SpaceSize.SPACE_24)
                        }
                        items(uiState.invitesUiState) { inviteItem ->
                            Alert(
                                stringResource(R.string.dashboard_courseInvite, inviteItem.courseName),
                                alertType = AlertType.Info,
                                buttons = {
                                    LoadingButton(
                                        label = stringResource(R.string.dashboard_courseInviteAccept),
                                        contentAlignment = Alignment.CenterStart,
                                        color = ButtonColor.Black,
                                        onClick = inviteItem.onAccept,
                                        loading = inviteItem.acceptLoading
                                    )
                                },
                                onDismiss = if (inviteItem.acceptLoading) null else inviteItem.onDismiss
                            )
                            HorizonSpace(SpaceSize.SPACE_16)
                        }
                        items(uiState.programsUiState) { program ->
                            DashboardProgramItem(program) {
                                homeNavController.navigate(HomeNavigationRoute.Learn.withProgram(program.id)) {
                                    popUpTo(homeNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            }
                        }
                        itemsIndexed(uiState.coursesUiState) { index, courseItem ->
                            DashboardCourseItem(courseItem, onClick = {
                                mainNavController.navigate(
                                    MainNavigationRoute.ModuleItemSequence(
                                        courseItem.courseId,
                                        courseItem.nextModuleItemId
                                    )
                                )
                            }, onCourseClick = {
                                homeNavController.navigate(HomeNavigationRoute.Learn.withCourse(courseItem.courseId)) {
                                    popUpTo(homeNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            }, onProgramClick = { programId ->
                                homeNavController.navigate(HomeNavigationRoute.Learn.withProgram(programId)) {
                                    popUpTo(homeNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            })
                            if (index < uiState.coursesUiState.size - 1) {
                                HorizonSpace(SpaceSize.SPACE_48)
                            }
                        }
                    })
            }
        }
    }
}

@Composable
private fun HomeScreenTopBar(uiState: DashboardUiState, mainNavController: NavController, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.Bottom, modifier = modifier) {
        GlideImage(
            model = uiState.logoUrl,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .weight(1f)
                .heightIn(max = 44.dp),
            contentDescription = stringResource(R.string.a11y_institutionLogoContentDescription),
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            iconRes = R.drawable.menu_book_notebook,
            onClick = {
                mainNavController.navigate(MainNavigationRoute.Notebook.route)
            },
            color = IconButtonColor.Inverse,
            elevation = HorizonElevation.level4,
        )
        HorizonSpace(SpaceSize.SPACE_8)
        IconButton(
            iconRes = R.drawable.notifications,
            onClick = {
                mainNavController.navigate(MainNavigationRoute.Notification.route)
            },
            elevation = HorizonElevation.level4,
            color = IconButtonColor.Inverse,
            badge = if (uiState.unreadCountState.unreadNotifications > 0) {
                {
                    Badge(
                        content = BadgeContent.Color,
                        type = BadgeType.Inverse
                    )
                }
            } else null
        )
        HorizonSpace(SpaceSize.SPACE_8)
        IconButton(
            iconRes = R.drawable.mail,
            onClick = { mainNavController.navigate(MainNavigationRoute.Inbox.route) },
            elevation = HorizonElevation.level4,
            color = IconButtonColor.Inverse,
            badge = if (uiState.unreadCountState.unreadConversations > 0) {
                {
                    Badge(
                        content = BadgeContent.Color,
                        type = BadgeType.Inverse
                    )
                }
            } else null
        )
    }
}

@Composable
private fun DashboardProgramItem(
    programUiState: DashboardProgramUiState,
    onProgramClick: () -> Unit
) {
    Column {
        Text(text = programUiState.name, style = HorizonTypography.h2)
        HorizonSpace(SpaceSize.SPACE_12)
        Text(text = stringResource(R.string.dashboard_viewProgram), style = HorizonTypography.p1)
        HorizonSpace(SpaceSize.SPACE_24)
        Button(label = stringResource(R.string.dashboard_viewProgramButton), color = ButtonColor.Institution, onClick = onProgramClick)
        HorizonSpace(SpaceSize.SPACE_24)
    }
}

@Composable
private fun DashboardCourseItem(
    courseItem: DashboardCourseUiState,
    onClick: () -> Unit,
    onCourseClick: () -> Unit,
    modifier: Modifier = Modifier,
    onProgramClick: (String) -> Unit = {}
) {
    Column(modifier) {
        Column(
            Modifier
                .clip(HorizonCornerRadius.level1_5)
                .clickable {
                    onCourseClick()
                }) {
            if (courseItem.parentPrograms.isNotEmpty()) {
                ProgramsText(programs = courseItem.parentPrograms, onProgramClick = onProgramClick)
                HorizonSpace(SpaceSize.SPACE_12)
            }
            Text(text = courseItem.courseName, style = HorizonTypography.h1)
            HorizonSpace(SpaceSize.SPACE_12)
            ProgressBar(progress = courseItem.courseProgress)
            HorizonSpace(SpaceSize.SPACE_36)
        }
        if (courseItem.completed) {
            Text(text = stringResource(R.string.dashboard_courseCompleted), style = HorizonTypography.h3)
            HorizonSpace(SpaceSize.SPACE_12)
            Text(text = stringResource(R.string.dashboard_courseCompletedDescription), style = HorizonTypography.p1)
        } else {
            Text(text = stringResource(R.string.dashboard_resumeLearning), style = HorizonTypography.h3)
            HorizonSpace(SpaceSize.SPACE_12)
            LearningObjectCard(
                LearningObjectCardState(
                    moduleTitle = courseItem.nextModuleName.orEmpty(),
                    learningObjectTitle = courseItem.nextModuleItemName.orEmpty(),
                    progressLabel = courseItem.progressLabel,
                    remainingTime = courseItem.remainingTime,
                    dueDate = courseItem.dueDate,
                    learningObjectType = courseItem.learningObjectType,
                    onClick = onClick
                )
            )
        }
        HorizonSpace(SpaceSize.SPACE_24)
    }
}

@Composable
private fun ProgramsText(
    programs: List<DashboardCourseProgram>,
    onProgramClick: (String) -> Unit
) {
    val programsAnnotated = buildAnnotatedString {
        programs.forEachIndexed { i, program ->
            if (i > 0) append(", ")
            withLink(
                LinkAnnotation.Clickable(
                    tag = program.programId,
                    styles = TextLinkStyles(
                        style = SpanStyle(textDecoration = TextDecoration.Underline)
                    ),
                    linkInteractionListener = { _ -> onProgramClick(program.programId) }
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

    Text(style = HorizonTypography.p1, text = fullText)
}

@Composable
private fun NotificationPermissionRequest() {
    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    var isPermissionRequested by rememberSaveable { mutableStateOf(false) }
    val permissionRequest = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { result ->
        hasNotificationPermission = result
    }

    LaunchedEffect(Unit) {
        if (!hasNotificationPermission && !isPermissionRequested && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isPermissionRequested = true
            permissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
@Preview
private fun DashboardScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    DashboardScreen(DashboardUiState(), mainNavController = rememberNavController(), homeNavController = rememberNavController())
}