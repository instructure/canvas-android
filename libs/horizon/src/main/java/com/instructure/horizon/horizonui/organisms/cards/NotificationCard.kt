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
package com.instructure.horizon.horizonui.organisms.cards

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.TextLink

@Composable
fun NotificationCard(
    message: String,
    actionButtonLabel: String,
    onActionButtonClick: () -> Unit,
) {
    Box(
       modifier = Modifier
           .border(
               1.dp,
               HorizonColors.LineAndBorder.lineStroke(),
               HorizonCornerRadius.level3)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Text(
                text = message,
                style = HorizonTypography.p2,
                color = HorizonColors.Text.timestamp()
            )

            HorizonSpace(SpaceSize.SPACE_16)

            TextLink(
                label = actionButtonLabel,
                onClick = onActionButtonClick,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD)
private fun NotificationCardPreview() {
    NotificationCard(
        message = "This is a notification message",
        actionButtonLabel = "Open link",
        onActionButtonClick = {}
    )
}