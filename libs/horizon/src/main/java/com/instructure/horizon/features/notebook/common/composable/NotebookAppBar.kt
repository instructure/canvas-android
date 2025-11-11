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
package com.instructure.horizon.features.notebook.common.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookAppBar(
    modifier: Modifier = Modifier,
    navigateBack: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    centeredTitle: Boolean = false,
    containerColor: Color = HorizonColors.Surface.pagePrimary(),
) {
    CenterAlignedTopAppBar(
        title = {
            if (centeredTitle) {
                Text(
                    text = stringResource(R.string.notebookTitle),
                    style = HorizonTypography.h3,
                    color = HorizonColors.Text.title(),
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.edit_note),
                        contentDescription = null,
                        tint = HorizonColors.Icon.default(),
                        modifier = Modifier
                            .padding(start = 16.dp, end = 4.dp)
                            .size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.notebookTitle),
                        style = HorizonTypography.h4,
                        color = HorizonColors.Text.title()
                    )
                }
            }
        },
        navigationIcon = {
            if (navigateBack != null) {
                IconButton(
                    iconRes = R.drawable.arrow_back,
                    contentDescription = stringResource(R.string.a11yNavigateBack),
                    color = IconButtonColor.Ghost,
                    size = IconButtonSize.SMALL,
                    onClick = navigateBack,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        },
        actions = {
            if (onClose != null) {
                IconButton(
                    iconRes = R.drawable.close,
                    contentDescription = stringResource(R.string.a11y_close),
                    color = IconButtonColor.Inverse,
                    size = IconButtonSize.SMALL,
                    elevation = HorizonElevation.level4,
                    onClick = onClose,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = HorizonColors.Text.title(),
            navigationIconContentColor = HorizonColors.Icon.default()
        ),
        modifier = modifier
    )
}

@Composable
@Preview
private fun NotebookAppBarPreview() {
    ContextKeeper.appContext = LocalContext.current
    NotebookAppBar(
        navigateBack = {}
    )
}

@Composable
@Preview
private fun NotebookAppBarClosePreview() {
    ContextKeeper.appContext = LocalContext.current
    NotebookAppBar(
        onClose = {}
    )
}

@Composable
@Preview
private fun NotebookAppBarCenteredPreview() {
    ContextKeeper.appContext = LocalContext.current
    NotebookAppBar(
        navigateBack = {},
        centeredTitle = true
    )
}

@Composable
@Preview
private fun NotebookAppBarCenteredClosePreview() {
    ContextKeeper.appContext = LocalContext.current
    NotebookAppBar(
        onClose = {},
        centeredTitle = true
    )
}