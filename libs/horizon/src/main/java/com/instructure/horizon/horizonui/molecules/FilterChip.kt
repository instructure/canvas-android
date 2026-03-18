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
package com.instructure.horizon.horizonui.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonTypography

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) HorizonColors.Surface.inversePrimary() else HorizonColors.Surface.cardPrimary()
    val textColor = if (selected) HorizonColors.Text.surfaceColored() else HorizonColors.Text.title()
    val horizontalPadding = if (selected) 8.dp else 12.dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .background(color = backgroundColor, shape = HorizonCornerRadius.level1)
            .border(HorizonBorder.level1(HorizonColors.Surface.inversePrimary()), shape = HorizonCornerRadius.level1)
            .clickable(onClick = onClick)
            .padding(horizontal = horizontalPadding, vertical = 4.dp)
    ) {
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.check),
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = label,
            style = HorizonTypography.p2,
            color = textColor
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun FilterChipSelectedPreview() {
    ContextKeeper.appContext = LocalContext.current
    FilterChip(label = "Most recent", selected = true, onClick = {})
}

@Composable
@Preview(showBackground = true)
private fun FilterChipUnselectedPreview() {
    ContextKeeper.appContext = LocalContext.current
    FilterChip(label = "Least recent", selected = false, onClick = {})
}