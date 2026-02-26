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
package com.instructure.horizon.features.learn.learninglibrary.enrolldialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.horizonBorderShadow
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnLearningLibraryEnrollDialog(
    learningLibraryItemId: String,
    onDismiss: () -> Unit
) {
    val viewModel = hiltViewModel<LearnLearningLibraryEnrollDialogViewModel>()
    viewModel.loadData(learningLibraryItemId)
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = HorizonColors.Surface.pagePrimary(),
        topBar = { LearnLearningLibraryEnrollDialogTopBar(state.syllabus, onDismiss) },
        bottomBar = { LearnLearningLibraryEnrollDialogBottomBar(state.onEnrollClicked, onDismiss) }
    ) {
        LoadingStateWrapper(state.loadingState) {
            LearnLearningLibraryEnrollDialogContent(state.syllabus)
        }
    }
}

@Composable
private fun LearnLearningLibraryEnrollDialogContent(
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
private fun LearnLearningLibraryEnrollDialogTopBar(
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
        modifier = Modifier
            .background(HorizonColors.Surface.pagePrimary())
            .padding(vertical = 16.dp)
            .horizonBorderShadow(
                HorizonColors.LineAndBorder.lineStroke(),
                bottom = 1.dp
            )
            .padding(horizontal = 16.dp)
    )
}

@Composable
private fun LearnLearningLibraryEnrollDialogBottomBar(
    onEnroll: () -> Unit,
    onDismiss: () -> Unit
) {
    BottomAppBar(
        containerColor = HorizonColors.Surface.pagePrimary(),
        modifier = Modifier
            .background(HorizonColors.Surface.pagePrimary())
            .padding(vertical = 16.dp)
            .horizonBorderShadow(
                HorizonColors.LineAndBorder.lineStroke(),
                top = 1.dp
            )
            .padding(horizontal = 16.dp)
    ) {
        Button(
            label = stringResource(R.string.learnLearningLibraryEnrollDialogNotNowLabel),
            width = ButtonWidth.RELATIVE,
            height = ButtonHeight.NORMAL,
            color = ButtonColor.Ghost,
            onClick = onDismiss
        )

        Spacer(Modifier.weight(1f))

        Button(
            label = stringResource(R.string.learnLearningLibraryEnrollDialogEnrollLabel),
            width = ButtonWidth.RELATIVE,
            height = ButtonHeight.NORMAL,
            color = ButtonColor.Black,
            onClick = onEnroll
        )
    }
}