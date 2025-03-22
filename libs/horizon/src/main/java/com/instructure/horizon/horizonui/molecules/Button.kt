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
package com.instructure.horizon.horizonui.molecules

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors

@Composable
fun ButtonPrimary(@DrawableRes iconRes: Int, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(shape = RoundedCornerShape(50.dp), color = HorizonColors.Surface.pageSecondary())
            .size(48.dp)
            .clickable { onClick() }
    ) {
        Icon(
            painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = HorizonColors.Icon.default()
        )
    }
}

@Composable
fun ButtonSecondary(@DrawableRes iconRes: Int, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(shape = RoundedCornerShape(50.dp), color = HorizonColors.Surface.institution())
            .size(48.dp)
            .clickable { onClick() }
    ) {
        Icon(
            painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = HorizonColors.Icon.surfaceColored()
        )
    }
}

@Composable
@Preview
private fun ButtonPreview() {
    ButtonPrimary(iconRes = R.drawable.arrow_forward)
}

@Composable
@Preview
private fun ButtonSecondaryPreview() {
    ContextKeeper.appContext = LocalContext.current
    ButtonSecondary(iconRes = R.drawable.arrow_forward)
}