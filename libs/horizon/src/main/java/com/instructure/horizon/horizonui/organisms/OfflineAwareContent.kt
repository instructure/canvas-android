/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize

sealed class OfflineContentState {
    data object Available : OfflineContentState()
    data object NotSynced : OfflineContentState()
    data object NotAvailableOffline : OfflineContentState()
}

@Composable
fun OfflineAwareContent(
    state: OfflineContentState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    when (state) {
        OfflineContentState.Available -> content()
        OfflineContentState.NotSynced -> OfflinePlaceholder(
            iconRes = R.drawable.cloud_off,
            iconTint = HorizonColors.Icon.default(),
            iconBackgroundColor = HorizonColors.Surface.cardPrimary(),
            titleRes = R.string.offline_notSyncedTitle,
            descriptionRes = R.string.offline_notSyncedDescription,
            modifier = modifier,
        )
        OfflineContentState.NotAvailableOffline -> OfflinePlaceholder(
            iconRes = R.drawable.cancel,
            iconTint = HorizonColors.Surface.error(),
            iconBackgroundColor = HorizonColors.PrimitivesRed.red12(),
            titleRes = R.string.offline_notAvailableTitle,
            descriptionRes = R.string.offline_notAvailableDescription,
            modifier = modifier,
        )
    }
}

@Composable
private fun OfflinePlaceholder(
    @DrawableRes iconRes: Int,
    iconTint: Color,
    iconBackgroundColor: Color,
    titleRes: Int,
    descriptionRes: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = HorizonColors.Surface.pageSecondary(),
                shape = HorizonCornerRadius.level5
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = iconBackgroundColor,
                    shape = HorizonCornerRadius.level1
                ),
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp),
            )
        }
        HorizonSpace(SpaceSize.SPACE_16)
        Text(
            text = stringResource(titleRes),
            style = HorizonTypography.h4,
            color = HorizonColors.Text.body(),
            textAlign = TextAlign.Center,
        )
        HorizonSpace(SpaceSize.SPACE_8)
        Text(
            text = stringResource(descriptionRes),
            style = HorizonTypography.p2,
            color = HorizonColors.Text.timestamp(),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F4F4)
@Composable
private fun OfflineAwareContentAvailablePreview() {
    OfflineAwareContent(state = OfflineContentState.Available) {
        Text("Content is available")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F4F4)
@Composable
private fun OfflineAwareContentNotSyncedPreview() {
    OfflineAwareContent(state = OfflineContentState.NotSynced) {}
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F4F4)
@Composable
private fun OfflineAwareContentNotAvailablePreview() {
    OfflineAwareContent(state = OfflineContentState.NotAvailableOffline) {}
}
