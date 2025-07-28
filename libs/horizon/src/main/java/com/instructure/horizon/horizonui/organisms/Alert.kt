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
package com.instructure.horizon.horizonui.organisms

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize

sealed class AlertType(@DrawableRes val iconRes: Int, val color: Color) {
    data object Info : AlertType(R.drawable.info, HorizonColors.Surface.attention())
    data object Success : AlertType(R.drawable.check, HorizonColors.Surface.success())
    data object Warning : AlertType(R.drawable.warning, HorizonColors.Surface.warning())
    data object Error : AlertType(R.drawable.error, HorizonColors.Surface.error())
}

@Composable
fun Alert(
    text: String,
    alertType: AlertType,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    buttons: @Composable (RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .clip(HorizonCornerRadius.level2)
            .height(IntrinsicSize.Min)
            .border(HorizonBorder.level2(color = alertType.color), HorizonCornerRadius.level2)
            .background(HorizonColors.Surface.pageSecondary())
    ) {
        Box(
            modifier = Modifier
                .width(52.dp)
                .fillMaxHeight()
                .background(color = alertType.color), contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(alertType.iconRes), tint = HorizonColors.Icon.surfaceColored(), contentDescription = null)
        }
        HorizonSpace(SpaceSize.SPACE_16)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = text, style = HorizonTypography.p1, color = HorizonColors.Text.body())
            if (buttons != null) {
                HorizonSpace(SpaceSize.SPACE_16)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    buttons()
                }
            }
        }
        if (onDismiss != null) {
            HorizonSpace(SpaceSize.SPACE_8)
            IconButton(
                iconRes = R.drawable.close,
                size = IconButtonSize.SMALL,
                color = IconButtonColor.Ghost,
                modifier = Modifier.padding(vertical = 16.dp),
                onClick = onDismiss
            )
        } else {
            // If no onDismiss is provided, we still need to add some space to keep the layout consistent
            HorizonSpace(SpaceSize.SPACE_40)
        }
        HorizonSpace(SpaceSize.SPACE_16)
    }
}

@Preview
@Composable
fun AlertPreview() {
    ContextKeeper.appContext = LocalContext.current
    Alert(
        text = "Nunc ut lacus ac libero ultrices vestibulum. Integer elementum urna, vel iaculis leo volutpat ut. Donec non sagittis nulla, vestibulum convallis arcu. Sed at leo magna. Nunc sit amet velit faucibus, tristique orci ut, posuere odio. ",
        alertType = AlertType.Info
    )
}

@Preview
@Composable
fun AlertDismissablePreview() {
    ContextKeeper.appContext = LocalContext.current
    Alert(
        text = "Dismissable alert",
        alertType = AlertType.Info,
        onDismiss = {}
    )
}

@Preview
@Composable
fun AlertSuccessPreview() {
    ContextKeeper.appContext = LocalContext.current
    Alert(
        text = "Success alert",
        alertType = AlertType.Success,
    )
}

@Preview
@Composable
fun AlertWarningPreview() {
    ContextKeeper.appContext = LocalContext.current
    Alert(
        text = "Warning alert",
        alertType = AlertType.Warning,
    )
}

@Preview
@Composable
fun AlertErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    Alert(
        text = "Error alert",
        alertType = AlertType.Error,
    )
}