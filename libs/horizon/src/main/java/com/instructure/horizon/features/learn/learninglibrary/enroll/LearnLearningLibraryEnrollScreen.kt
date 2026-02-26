/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn.learninglibrary.enroll

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.horizonBorder
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.molecules.LoadingButton
import com.instructure.horizon.horizonui.organisms.scaffolds.EdgeToEdgeScaffold
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnLearningLibraryEnrollScreen(
    state: LearnLearningLibraryEnrollState,
    navController: NavHostController
) {
    LaunchedEffect(state.navigateToCourseId) {
        state.navigateToCourseId?.let {
            state.resetNavigateToCourseId()
            navController.popBackStack()
            navController.navigate(LearnRoute.LearnCourseDetailsScreen.route(it))
        }
    }
    EdgeToEdgeScaffold(
        statusBarColor = HorizonColors.Surface.pageSecondary(),
        navigationBarColor = HorizonColors.Surface.pageSecondary(),
        containerColor = HorizonColors.Surface.pageSecondary(),
        topBar = {
            LearnLearningLibraryEnrollScreenTopBar(
                state.syllabus,
                { navController.popBackStack() }
            )
        },
        bottomBar = {
            LearnLearningLibraryEnrollScreenBottomBar(
                state.isEnrollLoading,
                state.onEnrollClicked,
                { navController.popBackStack() }
            )
        }
    ) { contentPadding ->
        LoadingStateWrapper(
            state.loadingState,
            Modifier.padding(contentPadding),
            containerColor = HorizonColors.Surface.pageSecondary(),
        ) {
            LearnLearningLibraryEnrollScreenContent(state.syllabus)
        }
    }
}

@Composable
private fun LearnLearningLibraryEnrollScreenContent(
    syllabus: String?,
) {
    if (syllabus.isNullOrBlank()) {
        Text(
            text = stringResource(R.string.learnLearningLibraryEnrollDialogDetailsMessage),
            style = HorizonTypography.p1,
            color = HorizonColors.Text.body(),
            modifier = Modifier.padding(24.dp)
        )
    } else {
        ComposeCanvasWebViewWrapper(
            content = syllabus,
            modifier = Modifier.padding(24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LearnLearningLibraryEnrollScreenTopBar(
    syllabus: String?,
    onDismiss: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = if (syllabus.isNullOrBlank())
                    stringResource(R.string.learnLearningLibraryEnrollDialogReadyToJoinTitle)
                else
                    stringResource(R.string.learnLearningLibraryEnrollDialogOverviewTitle),
                style = HorizonTypography.h3,
                color = HorizonColors.Text.title()
            )
        },
        actions = {
            IconButton(
                iconRes = R.drawable.close,
                contentDescription = stringResource(R.string.close),
                onClick = onDismiss,
                size = IconButtonSize.SMALL,
                color = IconButtonColor.Ghost
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HorizonColors.Surface.pageSecondary(),
            titleContentColor = HorizonColors.Text.title(),
            navigationIconContentColor = HorizonColors.Icon.default()
        ),
        modifier = Modifier
            .horizonBorder(
                HorizonColors.LineAndBorder.lineStroke(),
                bottom = 1.dp
            )
            .background(HorizonColors.Surface.pageSecondary())
    )
}

@Composable
private fun LearnLearningLibraryEnrollScreenBottomBar(
    isEnrollButtonLoading: Boolean,
    onEnroll: () -> Unit,
    onDismiss: () -> Unit
) {
    BottomAppBar(
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
        containerColor = HorizonColors.Surface.pageSecondary(),
        modifier = Modifier
            .horizonBorder(
                HorizonColors.LineAndBorder.lineStroke(),
                top = 1.dp
            )
            .background(HorizonColors.Surface.pageSecondary())
    ) {
        Button(
            label = stringResource(R.string.learnLearningLibraryEnrollDialogNotNowLabel),
            width = ButtonWidth.RELATIVE,
            height = ButtonHeight.NORMAL,
            color = ButtonColor.Ghost,
            onClick = onDismiss
        )

        Spacer(Modifier.weight(1f))

        LoadingButton(
            loading = isEnrollButtonLoading,
            label = stringResource(R.string.learnLearningLibraryEnrollDialogEnrollLabel),
            width = ButtonWidth.RELATIVE,
            height = ButtonHeight.NORMAL,
            color = ButtonColor.Black,
            contentAlignment = Alignment.Center,
            fixedLoadingSize = true,
            onClick = onEnroll
        )
    }
}