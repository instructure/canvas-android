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
package com.instructure.horizon.horizonui.molecules

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize

@Composable
fun TextLink(
    label: String,
    @DrawableRes iconRes: Int? = R.drawable.open_in_new,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            style = HorizonTypography.buttonTextLarge,
            textDecoration = TextDecoration.Underline,
            color = HorizonColors.Text.body(),
        )

        iconRes?.let {
            HorizonSpace(SpaceSize.SPACE_4)

            Icon(
                painter = painterResource(it),
                contentDescription = null,
                tint = HorizonColors.Icon.default(),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD)
private fun TextLinkWithIconPreview() {
    TextLink(
        label = "Text Link",
        iconRes = R.drawable.open_in_new,
        onClick = {}
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD)
private fun TextLinkWithoutIconPreview() {
    TextLink(
        label = "Text Link",
        iconRes = null,
        onClick = {}
    )
}