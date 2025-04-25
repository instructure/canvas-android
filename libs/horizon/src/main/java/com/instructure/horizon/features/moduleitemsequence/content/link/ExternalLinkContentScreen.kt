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
package com.instructure.horizon.features.moduleitemsequence.content.link

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.launchCustomTab

@Composable
fun ExternalLinkContentScreen(uiState: ExternalLinkUiState, modifier: Modifier = Modifier) {
    val activity = LocalContext.current.getActivityOrNull()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(24.dp)
            .clip(HorizonCornerRadius.level2)
            .clickable {
                activity?.launchCustomTab(uiState.linkUrl, ThemePrefs.brandColor)
            }
            .border(HorizonBorder.level1(), HorizonCornerRadius.level2)
            .padding(vertical = 24.dp, horizontal = 16.dp)
    ) {
        Icon(painterResource(R.drawable.link), contentDescription = null, tint = HorizonColors.Surface.institution())
        HorizonSpace(SpaceSize.SPACE_12)
        Text(text = uiState.linkTitle, style = HorizonTypography.p1, modifier = Modifier.weight(1f))
        HorizonSpace(SpaceSize.SPACE_12)
        Icon(painterResource(R.drawable.open_in_new), contentDescription = null, tint = HorizonColors.Icon.default())
    }
}

@Composable
@Preview(showBackground = true, widthDp = 320)
fun ExternalLinkContentScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    ExternalLinkContentScreen(
        uiState = ExternalLinkUiState("Title", "https://www.instructure.com"),
        modifier = Modifier
    )
}