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
package com.instructure.horizon.design.molecules

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.design.foundation.Colors

enum class PillStyle {
    OUTLINE,
    SOLID,
    INLINE
}

@Composable
fun Pill(style: PillStyle, label: String, modifier: Modifier = Modifier, @DrawableRes iconRes: Int? = null) {
    val finalModifier = when (style) {
        PillStyle.OUTLINE -> modifier
            .border(width = 1.dp, shape = RoundedCornerShape(32.dp), color = Colors.Surface.inversePrimary())
            .height(34.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
        PillStyle.SOLID -> modifier
            .background(shape = RoundedCornerShape(32.dp), color = Colors.Surface.inversePrimary())
            .height(34.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
        PillStyle.INLINE -> modifier
            .height(17.dp)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = finalModifier
    ) {
        if (iconRes != null) {
            Icon(
                painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(18.dp), // We need to check this because 18dp seems identical to 24dp
                tint = Colors.Icon.default(),
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(text = label, color = Colors.Text.body())
    }
}

@Composable
@Preview
private fun PillPreview() {
    Pill(PillStyle.OUTLINE, "Default", iconRes = R.drawable.book_2)
}