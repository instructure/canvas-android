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
package com.instructure.horizon.features.aiassistant.common.composable

import androidx.compose.foundation.layout.Row
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
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistToolbar(
    onDismissPressed: () -> Unit,
    modifier: Modifier = Modifier,
    onBackPressed: (() -> Unit)? = null,
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors().copy(
            containerColor = Color.Transparent
        ),
        modifier = modifier,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ai),
                    contentDescription = null,
                    tint = HorizonColors.Icon.surfaceColored(),
                    modifier = Modifier
                        .size(24.dp)
                )

                HorizonSpace(SpaceSize.SPACE_4)

                Text(
                    text = stringResource(R.string.aiAssistToolbarTitle),
                    style = HorizonTypography.h3,
                    color = HorizonColors.Text.surfaceColored()
                )
            }
        },
        navigationIcon = {
            if (onBackPressed != null) {
                IconButton(
                    iconRes = R.drawable.arrow_back,
                    contentDescription = stringResource(R.string.a11yNavigateBack),
                    size = IconButtonSize.SMALL,
                    color = IconButtonColor.Inverse,
                    onClick = onBackPressed,
                )
            }
        },
        actions = {
            IconButton(
                iconRes = R.drawable.close,
                contentDescription = stringResource(R.string.aiAssistDismissContentDescription),
                size = IconButtonSize.SMALL,
                color = IconButtonColor.Inverse,
                onClick = onDismissPressed,
            )
        }
    )
}

@Composable
@Preview
private fun AiAssistantToolbarPreview() {
    ContextKeeper.appContext = LocalContext.current
    AiAssistToolbar(
        onDismissPressed = {},
        onBackPressed = {}
    )
}