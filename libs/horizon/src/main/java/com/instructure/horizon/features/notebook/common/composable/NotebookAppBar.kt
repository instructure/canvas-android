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

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    navigateBack: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.notebookTitle),
                style = HorizonTypography.h3,
                color = HorizonColors.Text.title()
            )
        },
        navigationIcon = {
            if (navigateBack != null) {
                IconButton(
                    iconRes = R.drawable.arrow_back,
                    contentDescription = stringResource(R.string.a11yNavigateBack),
                    color = IconButtonColor.Inverse,
                    size = IconButtonSize.SMALL,
                    elevation = HorizonElevation.level4,
                    onClick = navigateBack,
                    modifier = Modifier.padding(4.dp)
                )
            }
        },
        actions = {
            if (onClose != null) {
                IconButton(
                    iconRes = R.drawable.close,
                    contentDescription = stringResource(R.string.a11yNavigateBack),
                    color = IconButtonColor.Inverse,
                    size = IconButtonSize.SMALL,
                    elevation = HorizonElevation.level4,
                    onClick = onClose,
                    modifier = Modifier.padding(4.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HorizonColors.Surface.pagePrimary(),
            titleContentColor = HorizonColors.Text.title(),
            navigationIconContentColor = HorizonColors.Icon.default()
        ),
        modifier = modifier.padding(horizontal = 24.dp)
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