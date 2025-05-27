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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize

@Composable
fun NotebookTypeSelect(
    type: NotebookType,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) colorResource(type.color) else HorizonColors.LineAndBorder.containerStroke()
    val iconColor = if (isSelected) colorResource(type.color) else HorizonColors.Icon.default()
    val textColor = if (isSelected) colorResource(type.color) else HorizonColors.Text.body()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .border(
                HorizonBorder.level1(color = borderColor),
                HorizonCornerRadius.level2
            )
            .background(Color.White)
            .clickable {
                onSelect()
            },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                painter = painterResource(type.iconRes),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            HorizonSpace(SpaceSize.SPACE_8)

            Text(
                text = stringResource(type.labelRes),
                style = HorizonTypography.buttonTextLarge,
                color = textColor,
            )
        }
    }
}

@Composable
@Preview
private fun NotebookTypeSelectConfusingSelectedPreview() {
    ContextKeeper.appContext = LocalContext.current
    NotebookTypeSelect(
        type = NotebookType.Confusing,
        isSelected = true,
        onSelect = {}
    )
}

@Composable
@Preview
private fun NotebookTypeSelectConfusingNotSelectedPreview() {
    ContextKeeper.appContext = LocalContext.current
    NotebookTypeSelect(
        type = NotebookType.Confusing,
        isSelected = false,
        onSelect = {}
    )
}

@Composable
@Preview
private fun NotebookTypeSelectImportantSelectedPreview() {
    ContextKeeper.appContext = LocalContext.current
    NotebookTypeSelect(
        type = NotebookType.Important,
        isSelected = true,
        onSelect = {}
    )
}

@Composable
@Preview
private fun NotebookTypeSelectImportantNotSelectedPreview() {
    ContextKeeper.appContext = LocalContext.current
    NotebookTypeSelect(
        type = NotebookType.Important,
        isSelected = false,
        onSelect = {}
    )
}